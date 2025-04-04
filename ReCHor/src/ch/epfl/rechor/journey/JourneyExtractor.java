package ch.epfl.rechor.journey;

import ch.epfl.rechor.Bits32_24_8;
import ch.epfl.rechor.timetable.Connections;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.Trips;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Classe utilitaire permettant d'extraire des voyages à partir d'un profil.
 * Ce fichier a été simplifié pour clarifier la logique et réduire la complexité
 * en extrayant des méthodes auxiliaires et en regroupant le décodage du critère.
 *
 * La logique d'extraction ne change pas ; seules des améliorations de lisibilité
 * et de modularisation ont été apportées.
 *
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */
public final class JourneyExtractor {

    private static final int INVALID_ID = -1;
    private static final int MAX_INTERMEDIATE_STOPS = 100;

    /**
     * Enregistrement interne permettant de regrouper les informations décodées
     * d'un critère empaqueté.
     */
    private record CriteriaData(int connectionId, int interStops, int changes,
                                int endMins, int depMins) {}

    /**
     * Décodage d'un critère empaqueté en ses composantes.
     *
     * @param criteria le critère empaqueté
     * @return un objet CriteriaData contenant l'id de connexion, le nombre d'arrêts intermédiaires,
     *         le nombre de changements, l'heure d'arrivée cible et l'heure de départ.
     */
    private static CriteriaData decodeCriteria(long criteria) {
        int connectionId = Bits32_24_8.unpack24(PackedCriteria.payload(criteria));
        int interStops = Bits32_24_8.unpack8(PackedCriteria.payload(criteria));
        int changes = PackedCriteria.changes(criteria);
        int endMins = PackedCriteria.arrMins(criteria);
        int depMins = PackedCriteria.depMins(criteria);
        return new CriteriaData(connectionId, interStops, changes, endMins, depMins);
    }

    /**
     * Constructeur privé pour empêcher l'instanciation de cette classe utilitaire.
     */
    private JourneyExtractor() {
    }

    /**
     * Extrait tous les voyages optimaux permettant d'aller de la gare de départ donnée
     * à la destination indiquée dans le profil.
     *
     * @param profile      le profil contenant les frontières de Pareto
     * @param depStationId l'indice de la gare de départ
     * @return une liste de voyages triés par heure de départ puis par heure d'arrivée
     */
    public static List<Journey> journeys(Profile profile, int depStationId) {
        List<Journey> journeys = new ArrayList<>();
        // Récupère la frontière de Pareto pour la station de départ
        ParetoFront pf = profile.forStation(depStationId);

        // Pour chaque critère de la frontière, tenter d'extraire un voyage
        pf.forEach(criteria -> {
            try {
                List<Journey.Leg> legs = new ArrayList<>();
                extractLegs(profile, depStationId, criteria, legs);
                if (!legs.isEmpty()) {
                    journeys.add(new Journey(legs));
                }
            } catch (Exception e) {
                // En cas d'erreur, on ignore ce critère et on passe au suivant
            }
        });

        // Tri des voyages par heure de départ puis par heure d'arrivée
        journeys.sort(Comparator
                .comparing(Journey::depTime)
                .thenComparing(Journey::arrTime));

        return journeys;
    }

    /**
     * Extrait récursivement les étapes (legs) d'un voyage à partir d'un critère empaqueté.
     *
     * @param profile      le profil contenant les données (horaire, connexions, etc.)
     * @param depStationId l'identifiant de la gare de départ
     * @param criteria     le critère empaqueté pour le voyage actuel
     * @param legs         la liste des étapes du voyage en cours de construction
     */
    private static void extractLegs(Profile profile, int depStationId, long criteria,
                                    List<Journey.Leg> legs) {
        // Obtenir les objets nécessaires
        TimeTable timeTable = profile.timeTable();
        Connections connections = profile.connections();

        // Décodage du critère en ses composantes
        CriteriaData crit = decodeCriteria(criteria);
        if (crit.connectionId() >= connections.size()) {
            return;
        }

        // Gérer une éventuelle étape initiale à pied
        // si la gare de départ diffère de l'arrêt de départ
        handleInitialFootLeg(timeTable, depStationId, connections.depStopId(crit.connectionId()),
                profile.date(), crit.depMins(), legs);

        // Variables locales pour le traitement itératif
        int connectionId = crit.connectionId();
        int interStops = crit.interStops();
        int changes = crit.changes();
        int endMins = crit.endMins();

        // Boucle sur les étapes de transport en fonction du nombre de changements
        for (int i = 0; i <= changes && connectionId < connections.size(); i++) {
            try {
                long newCriteria = processTransportLeg(profile, connectionId, interStops,
                        changes - i, endMins, i == changes, legs);
                // Mettre à jour les valeurs de critère pour la prochaine itération
                CriteriaData newCrit = decodeCriteria(newCriteria);
                connectionId = newCrit.connectionId();
                interStops = newCrit.interStops();

                if (connectionId < 0) {
                    break;
                }
            } catch (Exception e) {
                // En cas d'erreur dans le traitement, interrompre l'extraction
                break;
            }
        }
    }

    /**
     * Traite une étape de transport : collecte les arrêts intermédiaires, crée le leg de transport,
     * et gère la transition vers l'étape suivante.
     *
     * @param profile          le profil contenant les données
     * @param connectionId     l'identifiant de la connexion actuelle
     * @param interStops       le nombre d'arrêts intermédiaires à ignorer
     * @param remainingChanges le nombre de changements restants pour atteindre la destination
     * @param endMins          l'heure d'arrivée cible (en minutes)
     * @param isLastLeg        vrai si c'est la dernière étape de transport
     * @param legs             la liste des legs à compléter
     * @return le nouveau critère empaqueté pour la prochaine étape,
     * ou INVALID_ID si le voyage est terminé
     */
    private static long processTransportLeg(Profile profile, int connectionId, int interStops,
                                            int remainingChanges, int endMins, boolean isLastLeg,
                                            List<Journey.Leg> legs) {
        TimeTable timeTable = profile.timeTable();
        Connections connections = profile.connections();

        // Récupérer les informations de la connexion courante
        int depStopId = connections.depStopId(connectionId);
        int arrStopId = connections.arrStopId(connectionId);
        int tripId = connections.tripId(connectionId);
        int depTime = connections.depMins(connectionId);

        // Collecter les arrêts intermédiaires si nécessaire
        List<Journey.Leg.IntermediateStop> intermediateStops = new ArrayList<>();
        if (interStops > 0) {
            int nextConnectionId = collectIntermediateStops(connections, timeTable, profile.date(),
                    connectionId, interStops, intermediateStops);
            if (nextConnectionId != connectionId && nextConnectionId < connections.size()) {
                connectionId = nextConnectionId;
                arrStopId = connections.arrStopId(connectionId);
            }
        }

        // Créer et ajouter le leg de transport
        createTransportLeg(profile, depStopId, arrStopId, connectionId, depTime, tripId,
                intermediateStops, legs);

        // Déterminer la station atteinte à la fin de l'étape
        int currentStationId = timeTable.stationId(arrStopId);

        // Gérer la transition vers l'étape suivante ou ajouter une étape finale à pied
        if (!isLastLeg) {
            return handleNextLeg(profile, connectionId, currentStationId, endMins,
                    remainingChanges, legs);
        } else if (currentStationId != profile.arrStationId()) {
            addFinalFootLegIfNeeded(timeTable, currentStationId, profile.arrStationId(),
                    createStop(timeTable, arrStopId),
                    createDateTime(profile.date(), connections.arrMins(connectionId)),
                    legs);
        }
        return INVALID_ID;
    }

    /**
     * Gère la transition après une étape de transport.
     * Récupère le prochain critère via la frontière de Pareto
     * et ajoute une étape à pied de transfert.
     *
     * @param profile          le profil contenant les données
     * @param connectionId     l'identifiant de la connexion précédente
     * @param currentStationId l'identifiant de la station atteinte après l'étape de transport
     * @param endMins          l'heure d'arrivée cible (en minutes) pour le prochain critère
     * @param remainingChanges le nombre de changements restants
     * @param legs             la liste des legs à compléter
     * @return le nouveau critère empaqueté pour poursuivre l'extraction,
     * ou INVALID_ID si le voyage est terminé
     */
    private static long handleNextLeg(Profile profile, int connectionId, int currentStationId,
                                      int endMins, int remainingChanges, List<Journey.Leg> legs) {
        TimeTable timeTable = profile.timeTable();
        Connections connections = profile.connections();

        try {
            // Récupérer le critère suivant depuis la frontière de Pareto de la station actuelle
            ParetoFront nextFront = profile.forStation(currentStationId);
            long nextCriteria = nextFront.get(endMins, remainingChanges - 1);
            int nextConnectionId = Bits32_24_8.unpack24(PackedCriteria.payload(nextCriteria));

            // Si le prochain identifiant est invalide, ajouter une étape finale à pied
            if (nextConnectionId >= connections.size()) {
                addFinalFootLegIfNeeded(timeTable, currentStationId, profile.arrStationId(),
                        createStop(timeTable, connections.arrStopId(connectionId)),
                        createDateTime(profile.date(), connections.arrMins(connectionId)),
                        legs);
                return INVALID_ID;
            }
            // Ajouter une étape à pied pour le transfert vers la prochaine connexion
            addFootLegForChange(profile, connectionId, nextConnectionId, legs);
            return nextCriteria;
        } catch (NoSuchElementException e) {
            // En cas d'absence de critère suivant, ajouter une étape finale à pied pour terminer le voyage
            addFinalFootLegIfNeeded(timeTable, currentStationId, profile.arrStationId(),
                    createStop(timeTable, connections.arrStopId(connectionId)),
                    createDateTime(profile.date(), connections.arrMins(connectionId)),
                    legs);
            return INVALID_ID;
        }
    }

    /**
     * Ajoute une étape à pied pour le transfert entre deux connexions.
     *
     * @param profile             le profil contenant les données
     * @param currentConnectionId l'identifiant de la dernière connexion de l'étape de transport
     * @param nextConnectionId    l'identifiant de la prochaine connexion
     * @param legs                la liste des legs à compléter
     */
    private static void addFootLegForChange(Profile profile, int currentConnectionId,
                                            int nextConnectionId, List<Journey.Leg> legs) {
        TimeTable timeTable = profile.timeTable();
        Connections connections = profile.connections();

        int arrStopId = connections.arrStopId(currentConnectionId);
        int nextDepStopId = connections.depStopId(nextConnectionId);

        Stop arrStop = createStop(timeTable, arrStopId);
        Stop nextDepStop = createStop(timeTable, nextDepStopId);

        // Récupérer l'heure d'arrivée de la connexion courante
        LocalDateTime arrDateTime
                = createDateTime(profile.date(), connections.arrMins(currentConnectionId));

        // Calculer le temps de transfert entre les arrêts
        int transferMinutes = timeTable.transfers().minutesBetween(
                timeTable.stationId(arrStopId), timeTable.stationId(nextDepStopId));
        LocalDateTime nextDepDateTime = arrDateTime.plusMinutes(transferMinutes);

        // Ajouter le leg à pied
        legs.add(new Journey.Leg.Foot(arrStop, arrDateTime, nextDepStop, nextDepDateTime));
    }

    /**
     * Crée et ajoute un leg de transport à la liste des legs.
     *
     * @param profile           le profil contenant les données
     * @param depStopId         l'identifiant de l'arrêt de départ
     * @param arrStopId         l'identifiant de l'arrêt d'arrivée
     * @param connectionId      l'identifiant de la connexion en cours
     * @param depTime           l'heure de départ en minutes
     * @param tripId            l'identifiant de la course associée
     * @param intermediateStops la liste des arrêts intermédiaires collectés
     * @param legs              la liste des legs à compléter
     */
    private static void createTransportLeg(Profile profile, int depStopId, int arrStopId,
                                           int connectionId, int depTime, int tripId,
                                           List<Journey.Leg.IntermediateStop> intermediateStops,
                                           List<Journey.Leg> legs) {
        TimeTable timeTable = profile.timeTable();
        Connections connections = profile.connections();
        Trips trips = profile.trips();

        Stop depStop = createStop(timeTable, depStopId);
        Stop arrStop = createStop(timeTable, arrStopId);

        LocalDateTime depDateTime = createDateTime(profile.date(), depTime);
        LocalDateTime arrDateTime
                = createDateTime(profile.date(), connections.arrMins(connectionId));

        int routeId = trips.routeId(tripId);
        Vehicle vehicle = timeTable.routes().vehicle(routeId);
        String routeName = timeTable.routes().name(routeId);
        String destination = trips.destination(tripId);

        // Ajouter le leg de transport construit à la liste
        legs.add(new Journey.Leg.Transport(
                depStop, depDateTime, arrStop, arrDateTime,
                intermediateStops, vehicle, routeName, destination));
    }

    /**
     * Gère l'ajout d'une étape à pied initiale
     * si la gare de départ ne correspond pas à l'arrêt de départ.
     *
     * @param timeTable    l'horaire contenant les données
     * @param depStationId l'identifiant de la gare de départ
     * @param depStopId    l'identifiant de l'arrêt de départ de la première connexion
     * @param date         la date du voyage
     * @param depMinutes   l'heure de départ en minutes
     * @param legs         la liste des legs à compléter
     */
    private static void handleInitialFootLeg(TimeTable timeTable, int depStationId, int depStopId,
                                             LocalDate date, int depMinutes,
                                             List<Journey.Leg> legs) {
        // Si l'arrêt de départ ne correspond pas à la gare de départ, ajouter une étape à pied
        if (timeTable.stationId(depStopId) != depStationId) {
            try {
                Stop depStation = createStationStop(timeTable, depStationId);
                Stop arrStop = createStop(timeTable, depStopId);
                LocalDateTime depDateTime = createDateTime(date, depMinutes);
                int walkTime = timeTable.transfers()
                        .minutesBetween(depStationId, timeTable.stationId(depStopId));
                LocalDateTime arrDateTime = depDateTime.plusMinutes(walkTime);
                legs.add(new Journey.Leg.Foot(depStation, depDateTime, arrStop, arrDateTime));
            } catch (Exception e) {
                // En cas d'erreur, ignorer l'ajout de l'étape initiale
            }
        }
    }

    /**
     * Collecte les arrêts intermédiaires pour une étape de transport.
     *
     * @param connections  les connexions disponibles
     * @param timeTable    l'horaire contenant les données
     * @param date         la date du voyage
     * @param connectionId l'identifiant de la première connexion de l'étape
     * @param count        le nombre d'arrêts intermédiaires à collecter
     * @param stops        la liste dans laquelle ajouter les arrêts intermédiaires
     * @return l'identifiant de la connexion suivante après la collecte
     */
    private static int collectIntermediateStops(Connections connections, TimeTable timeTable,
                                                LocalDate date, int connectionId, int count,
                                                List<Journey.Leg.IntermediateStop> stops) {
        int currentConnId = connectionId;
        // Boucle de collecte limitée par count et MAX_INTERMEDIATE_STOPS
        for (int j = 0; j < Math.min(count, MAX_INTERMEDIATE_STOPS); j++) {
            try {
                int arrStopId = connections.arrStopId(currentConnId);
                int arrMinutes = connections.arrMins(currentConnId);
                currentConnId = connections.nextConnectionId(currentConnId);
                if (currentConnId >= connections.size()) {
                    break;
                }
                Stop interStop = createStop(timeTable, arrStopId);
                LocalDateTime arrDateTime = createDateTime(date, arrMinutes);
                LocalDateTime depDateTime
                        = createDateTime(date, connections.depMins(currentConnId));
                stops.add(new Journey.Leg.IntermediateStop(interStop, arrDateTime, depDateTime));
            } catch (Exception e) {
                break;
            }
        }
        return currentConnId;
    }

    /**
     * Ajoute une étape à pied finale si la station actuelle n'est pas la destination.
     *
     * @param timeTable        l'horaire contenant les données
     * @param currentStationId l'identifiant de la station actuelle
     * @param destStationId    l'identifiant de la gare de destination
     * @param arrStop          l'arrêt d'arrivée de la dernière étape de transport
     * @param arrDateTime      la date/heure d'arrivée de la dernière étape de transport
     * @param legs             la liste des legs à compléter
     */
    private static void addFinalFootLegIfNeeded(TimeTable timeTable, int currentStationId,
                                                int destStationId, Stop arrStop,
                                                LocalDateTime arrDateTime, List<Journey.Leg> legs) {
        try {
            if (currentStationId != destStationId) {
                Stop finalStop = createStationStop(timeTable, destStationId);
                int transferTime = timeTable.transfers()
                        .minutesBetween(currentStationId, destStationId);
                LocalDateTime finalArrDateTime = arrDateTime.plusMinutes(transferTime);
                legs.add(new Journey.Leg.Foot(arrStop, arrDateTime, finalStop, finalArrDateTime));
            }
        } catch (Exception e) {
            // En cas d'erreur, ignorer l'ajout de l'étape finale
        }
    }

    /**
     * Crée un objet LocalDateTime à partir d'une date et d'un nombre de minutes depuis minuit.
     *
     * @param date    la date de l'évènement
     * @param minutes le nombre de minutes après minuit
     * @return un objet LocalDateTime correspondant à la date et l'heure calculée
     */
    private static LocalDateTime createDateTime(LocalDate date, int minutes) {
        return LocalDateTime.of(date, LocalTime.MIDNIGHT).plusMinutes(minutes);
    }

    /**
     * Crée un objet Stop à partir d'un identifiant d'arrêt.
     *
     * @param timeTable l'horaire contenant les données
     * @param stopId    l'identifiant de l'arrêt à créer
     * @return l'objet Stop correspondant
     * @throws IllegalArgumentException si stopId est invalide
     */
    private static Stop createStop(TimeTable timeTable, int stopId) {
        if (stopId < 0) {
            throw new IllegalArgumentException("Identifiant d'arrêt négatif");
        }
        if (timeTable.isStationId(stopId)) {
            return createStationStop(timeTable, stopId);
        } else {
            int stationsSize = timeTable.stations().size();
            if (stopId < stationsSize) {
                return createStationStop(timeTable, stopId);
            }

            int platformId = stopId - stationsSize;
            if (platformId >= timeTable.platforms().size()) {
                throw new IllegalArgumentException("Identifiant de plateforme invalide");
            }

            int stationId = timeTable.platforms().stationId(platformId);
            return new Stop(
                    timeTable.stations().name(stationId),
                    timeTable.platforms().name(platformId),
                    timeTable.stations().longitude(stationId),
                    timeTable.stations().latitude(stationId)
            );
        }
    }

    /**
     * Crée un objet Stop représentant une gare.
     *
     * @param timeTable l'horaire contenant les données
     * @param stationId l'identifiant de la gare
     * @return l'objet Stop correspondant à la gare
     * @throws IllegalArgumentException si stationId est invalide
     */
    private static Stop createStationStop(TimeTable timeTable, int stationId) {
        if (stationId < 0 || stationId >= timeTable.stations().size()) {
            throw new IllegalArgumentException("Identifiant de gare invalide");
        }
        return new Stop(
                timeTable.stations().name(stationId),
                null,
                timeTable.stations().longitude(stationId),
                timeTable.stations().latitude(stationId)
        );
    }
}