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
 * Optimisée pour la concision et l'efficacité en réduisant les appels redondants
 * et en utilisant des méthodes auxiliaires bien définies.
 *
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */
public final class JourneyExtractor {

    private static final int INVALID_ID = -1;

    /**
     * Constructeur privé pour empêcher l'instanciation
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
        ParetoFront pf = profile.forStation(depStationId);

        // Parcours de la frontière de Pareto pour la gare de départ
        pf.forEach(criteria -> {
            try {
                List<Journey.Leg> legs = new ArrayList<>();
                extractLegs(profile, depStationId, criteria, legs);

                if (!legs.isEmpty()) {
                    journeys.add(new Journey(legs));
                }
            } catch (Exception e) {
                // Simple log de l'erreur, permet de continuer avec les autres voyages
            }
        });

        // Tri des voyages selon l'heure de départ puis l'heure d'arrivée
        journeys.sort(Comparator
                .comparing(Journey::depTime)
                .thenComparing(Journey::arrTime));

        return journeys;
    }

    /**
     * Extrait récursivement les étapes d'un voyage à partir des critères de la frontière de Pareto.
     *
     * @param profile      le profil contenant les données
     * @param depStationId l'identifiant de la gare de départ
     * @param criteria     les critères du voyage actuel
     * @param legs         la liste des étapes à compléter
     */
    private static void extractLegs(Profile profile, int depStationId, long criteria,
                                    List<Journey.Leg> legs) {
        // Récupération des objets fréquemment utilisés
        TimeTable timeTable = profile.timeTable();
        Connections connections = profile.connections();

        // Décodage du critère
        int connectionId = Bits32_24_8.unpack24(PackedCriteria.payload(criteria));
        int interStops = Bits32_24_8.unpack8(PackedCriteria.payload(criteria));
        int changes = PackedCriteria.changes(criteria);
        int endMins = PackedCriteria.arrMins(criteria);

        // Validation des indices
        if (connectionId >= connections.size()) {
            return;
        }

        int depStopId = connections.depStopId(connectionId);

        // Gérer une étape à pied initiale si nécessaire
        handleInitialFootLeg(timeTable, depStationId, depStopId, profile.date(),
                PackedCriteria.depMins(criteria), legs);

        // Extraction des étapes du voyage
        for (int i = 0; i <= changes && connectionId < connections.size(); i++) {
            try {
                // Traitement de l'étape de transport courante
                long newCriteria = processTransportLeg(profile, connectionId, interStops,
                        changes - i, endMins, i == changes, legs);
                connectionId = Bits32_24_8.unpack24(PackedCriteria.payload(newCriteria));
                interStops = Bits32_24_8.unpack8(PackedCriteria.payload(newCriteria));

                // Si connectionId est devenu invalide lors du traitement, on s'arrête
                if (connectionId < 0) {
                    break;
                }


            } catch (Exception e) {
                // En cas d'erreur dans le traitement d'une étape, on arrête l'extraction
                break;
            }
        }
    }

    /**
     * Traite une étape de transport dans l'extraction d'un voyage.
     * <p>
     * Cette méthode récupère les informations de la connexion actuelle (stops, heure, trip, etc.),
     * collecte les arrêts intermédiaires si nécessaire, et crée une étape de transport qu'elle ajoute
     * à la liste des legs du voyage. Ensuite, elle détermine la station actuelle et gère la transition
     * vers la prochaine étape en appelant {@code handleNextLeg} ou en ajoutant une étape finale à pied
     * si la destination n'est pas atteinte.
     * </p>
     *
     * @param profile          le profil contenant les données de l'horaire et la frontière de Pareto
     * @param connectionId     l'identifiant de la connexion actuelle
     * @param interStops       le nombre d'arrêts intermédiaires à ignorer
     * @param remainingChanges le nombre de changements restants pour atteindre la destination
     * @param endMins          l'heure d'arrivée cible (en minutes) pour le critère
     * @param isLastLeg        vrai si c'est la dernière étape de transport
     * @param legs             la liste des legs du voyage à compléter
     * @return le critère suivant sous forme de long pour la prochaine étape, ou -1 si le voyage est terminé
     */
    private static long processTransportLeg(Profile profile, int connectionId, int interStops,
                                            int remainingChanges, int endMins, boolean isLastLeg,
                                            List<Journey.Leg> legs) {
        TimeTable timeTable = profile.timeTable();
        Connections connections = profile.connections();

        // Récupération des informations de la connexion actuelle
        int depStopId = connections.depStopId(connectionId);
        int arrStopId = connections.arrStopId(connectionId);
        int tripId = connections.tripId(connectionId);
        int depTime = connections.depMins(connectionId);

        // Collecte des arrêts intermédiaires si nécessaire
        List<Journey.Leg.IntermediateStop> intermediateStops = new ArrayList<>();
        if (interStops > 0) {
            int nextConnectionId = collectIntermediateStops(connections, timeTable, profile.date(),
                    connectionId, interStops, intermediateStops);
            if (nextConnectionId != connectionId && nextConnectionId < connections.size()) {
                connectionId = nextConnectionId;
                arrStopId = connections.arrStopId(connectionId);
            }
        }

        // Création de l'étape de transport et ajout à la liste des legs
        createTransportLeg(profile, depStopId, arrStopId, connectionId, depTime, tripId,
                intermediateStops, legs);

        // Détermination de la station actuelle après l'étape de transport
        int currentStationId = timeTable.stationId(arrStopId);

        // Gestion de la transition vers la prochaine étape ou ajout d'une étape finale à pied
        if (!isLastLeg) {
            return handleNextLeg(profile, connectionId, currentStationId, endMins, remainingChanges, legs);
        } else if (currentStationId != profile.arrStationId()) {
            addFinalFootLegIfNeeded(timeTable, currentStationId, profile.arrStationId(),
                    createStop(timeTable, arrStopId),
                    createDateTime(profile.date(), connections.arrMins(connectionId)),
                    legs);
        }
        return INVALID_ID;
    }

    /**
     * Gère la transition après une étape de transport en déterminant la prochaine étape via la frontière de Pareto.
     * <p>
     * Cette méthode récupère le critère suivant à partir de la frontière de Pareto de la station actuelle.
     * Si le critère indique une connexion valide, elle ajoute une étape à pied pour le transfert
     * vers cette prochaine connexion. Sinon, ou en cas d'absence de critère, elle ajoute une étape finale
     * à pied afin d'assurer que le voyage se termine correctement.
     * </p>
     *
     * @param profile          le profil contenant les données de l'horaire et la frontière de Pareto
     * @param connectionId     l'identifiant de la connexion précédente
     * @param currentStationId l'identifiant de la station atteinte après l'étape de transport
     * @param endMins          l'heure d'arrivée cible (en minutes) pour le prochain critère
     * @param remainingChanges le nombre de changements restants pour atteindre la destination
     * @param legs             la liste des legs du voyage à compléter
     * @return le critère suivant sous forme de long pour poursuivre l'extraction, ou -1 si le voyage est terminé
     */
    private static long handleNextLeg(Profile profile, int connectionId, int currentStationId,
                                      int endMins, int remainingChanges, List<Journey.Leg> legs) {
        TimeTable timeTable = profile.timeTable();
        Connections connections = profile.connections();

        try {
            // Récupère le critère suivant depuis la frontière de Pareto de la station actuelle
            ParetoFront nextFront = profile.forStation(currentStationId);
            long nextCriteria = nextFront.get(endMins, remainingChanges - 1);
            int nextConnectionId = Bits32_24_8.unpack24(PackedCriteria.payload(nextCriteria));

            // Si le prochain identifiant de connexion est invalide, ajouter une étape finale à pied
            if (nextConnectionId >= connections.size()) {
                addFinalFootLegIfNeeded(timeTable, currentStationId, profile.arrStationId(),
                        createStop(timeTable, connections.arrStopId(connectionId)),
                        createDateTime(profile.date(), connections.arrMins(connectionId)),
                        legs);
                return INVALID_ID;
            }
            // Ajoute une étape à pied pour le transfert vers la prochaine connexion
            addFootLegForChange(profile, connectionId, nextConnectionId, legs);
            return nextCriteria;
        } catch (NoSuchElementException e) {
            // En cas d'absence de critère suivant, ajoute une étape finale à pied pour terminer le voyage
            addFinalFootLegIfNeeded(timeTable, currentStationId, profile.arrStationId(),
                    createStop(timeTable, connections.arrStopId(connectionId)),
                    createDateTime(profile.date(), connections.arrMins(connectionId)),
                    legs);
            return INVALID_ID;
        }
    }


    /**
     * Ajoute une étape à pied pour le changement entre deux connexions.
     *
     * @param profile             le profile contenant les données.
     * @param currentConnectionId l'id de la dernière liaison de l'étape en transport.
     * @param nextConnectionId    id de la première liaison de la prochaine étape en transport.
     * @param legs                la liste des étapes à compléter.
     */
    private static void addFootLegForChange(Profile profile, int currentConnectionId,
                                            int nextConnectionId, List<Journey.Leg> legs) {
        TimeTable timeTable = profile.timeTable();
        Connections connections = profile.connections();

        int arrStopId = connections.arrStopId(currentConnectionId);
        int nextDepStopId = connections.depStopId(nextConnectionId);

        Stop arrStop = createStop(timeTable, arrStopId);
        Stop nextDepStop = createStop(timeTable, nextDepStopId);

        LocalDateTime arrDateTime = createDateTime(profile.date(),
                connections.arrMins(currentConnectionId));

        int transferMinutes = timeTable.transfers().minutesBetween(
                timeTable.stationId(arrStopId), timeTable.stationId(nextDepStopId));

        LocalDateTime nextDepDateTime = arrDateTime.plusMinutes(transferMinutes);

        legs.add(new Journey.Leg.Foot(arrStop, arrDateTime, nextDepStop, nextDepDateTime));
    }


    /**
     * Crée et ajoute une étape de transport à la liste des étapes.
     *
     * @param profile           le profile contenant les données.
     * @param depStopId         l'id de l'arrêt de départ de l'étape en transport.
     * @param arrStopId         l'id de l'arrêt d'arrivée de l'étape en transport.
     * @param connectionId      l'id de la dernière liaison de l'étape en transport.
     * @param depTime           l'heure de départ de l'étape en minutes.
     * @param tripId            l'id de la course à laquelle appartient cette étape en transport.
     * @param intermediateStops la liste des arrêts intermédiaires de cette étape.
     * @param legs              la liste des étapes à compléter.
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
        LocalDateTime arrDateTime = createDateTime(profile.date(),
                connections.arrMins(connectionId));

        int routeId = trips.routeId(tripId);
        Vehicle vehicle = timeTable.routes().vehicle(routeId);
        String routeName = timeTable.routes().name(routeId);
        String destination = trips.destination(tripId);

        legs.add(new Journey.Leg.Transport(
                depStop, depDateTime, arrStop, arrDateTime,
                intermediateStops, vehicle, routeName, destination));
    }


    /**
     * Gère l'ajout d'une étape à pied initiale si nécessaire.
     *
     * @param timeTable    est un horaire de transport public avec des données aplaties.
     * @param depStationId l'id de la gare de départ du voyage.
     * @param depStopId    l'id de l'arrêt de départ de la première liaison
     * @param date         la date du début du voyage.
     * @param depMinutes   l'heure de départ du voyage en minutes.
     * @param legs         la liste des étapes à compléter.
     */
    private static void handleInitialFootLeg(TimeTable timeTable, int depStationId, int depStopId,
                                             LocalDate date, int depMinutes,
                                             List<Journey.Leg> legs) {

        if (timeTable.stationId(depStopId) != depStationId) {
            try {
                Stop depStation = createStationStop(timeTable, depStationId);
                Stop arrStop = createStop(timeTable, depStopId);

                LocalDateTime depDateTime = createDateTime(date, depMinutes);
                int walkTime = timeTable.transfers().minutesBetween(
                        depStationId, timeTable.stationId(depStopId));

                LocalDateTime arrDateTime = depDateTime.plusMinutes(walkTime);

                legs.add(new Journey.Leg.Foot(depStation, depDateTime, arrStop, arrDateTime));
            } catch (Exception e) {
                // Ignorer si l'ajout n'est pas possible
            }
        }
    }


    /**
     * Collecte les arrêts intermédiaires pour une étape de transport.
     *
     * @param connections  les liaisons indexées.
     * @param timeTable    est un horaire de transport public avec des données aplaties.
     * @param date         la date du voyage.
     * @param connectionId l'id de la première liaison de l'étape.
     * @param count        le nombre d'arrêts intermédiaires de l'étape.
     * @param stops        la liste des arrêts intermédiaires de l'étape.
     * @return l'ID de la connexion suivante après les arrêts intermédiaires.
     */
    private static int collectIntermediateStops(Connections connections, TimeTable timeTable,
                                                LocalDate date, int connectionId, int count,
                                                List<Journey.Leg.IntermediateStop> stops) {
        int currentConnId = connectionId;

        for (int j = 0; j < count && j < 100; j++) { // Limite de sécurité
            try {
                int arrStopId = connections.arrStopId(currentConnId);
                int arrMinutes = connections.arrMins(currentConnId);

                currentConnId = connections.nextConnectionId(currentConnId);

                if (currentConnId >= connections.size()) {
                    break;
                }

                Stop interStop = createStop(timeTable, arrStopId);
                LocalDateTime arrDateTime = createDateTime(date, arrMinutes);
                LocalDateTime depDateTime = createDateTime(date,
                        connections.depMins(currentConnId));

                stops.add(new Journey.Leg.IntermediateStop(interStop, arrDateTime, depDateTime));

            } catch (Exception e) {
                break;
            }
        }

        return currentConnId;
    }


    /**
     * Ajoute une étape à pied finale si la station courante n'est pas la destination.
     *
     * @param timeTable        est un horaire de transport public avec des données aplaties.
     * @param currentStationId l'id de la gare à la fin de la dernière étape en transport.
     * @param destStationId    l'id de la gare de destination du voyage.
     * @param arrStop          l'arrêt d'arrivée de la dernière étape en transport.
     * @param arrDateTime      la date/heure d'arrivée de la dernière étape en transport.
     * @param legs             la liste des étapes à compléter.
     */
    private static void addFinalFootLegIfNeeded(TimeTable timeTable, int currentStationId,
                                                int destStationId, Stop arrStop,
                                                LocalDateTime arrDateTime, List<Journey.Leg> legs) {
        try {
            if (currentStationId != destStationId) {
                Stop finalStop = createStationStop(timeTable, destStationId);
                int transferTime = timeTable.transfers().minutesBetween(currentStationId,
                        destStationId);

                LocalDateTime finalArrDateTime = arrDateTime.plusMinutes(transferTime);

                legs.add(new Journey.Leg.Foot(arrStop, arrDateTime, finalStop, finalArrDateTime));
            }
        } catch (Exception e) {
            // Ignorer si l'ajout n'est pas possible
        }
    }


    /**
     * Crée un objet LocalDateTime à partir d'une date et d'un nombre de minutes depuis minuit.
     *
     * @param date    la date de l'évènement.
     * @param minutes l'heure de l'évènement en nombre de minutes après minuit.
     * @return la date/heure de l'évènement sous forme d'un objet LocalDateTime.
     */
    private static LocalDateTime createDateTime(LocalDate date, int minutes) {
        return LocalDateTime.of(date, LocalTime.MIDNIGHT).plusMinutes(minutes);
    }


    /**
     * Crée un objet Stop à partir d'un identifiant d'arrêt.
     *
     * @param timeTable est un horaire de transport public avec des données aplaties.
     * @param stopId    l'id de l'arrêt que l'on veut créer.
     * @return retourne l'arrêt d'id stopId.
     */
    private static Stop createStop(TimeTable timeTable, int stopId) {
        if (stopId < 0) {
            throw new IllegalArgumentException();
        }

        if (timeTable.isStationId(stopId)) {
            return createStationStop(timeTable, stopId);
        }
        else {
            int stationsSize = timeTable.stations().size();
            if (stopId < stationsSize) {
                return createStationStop(timeTable, stopId);
            }

            int platformId = stopId - stationsSize;
            if (platformId >= timeTable.platforms().size()) {
                throw new IllegalArgumentException();
            }

            int stationId = timeTable.platforms().stationId(platformId);
            return new Stop(
                    timeTable.stations().name(stationId),
                    timeTable.platforms().name(platformId),
                    timeTable.stations().longitude(stationId),
                    timeTable.stations().latitude(stationId));
        }
    }


    /**
     * Crée un objet Stop représentant une gare.
     *
     * @param timeTable est un horaire de transport public avec des données aplaties.
     * @param stationId l'id de la gare qui est représentée par cet arrêt.
     * @return l'arrêt représentant la gare d'id stationId.
     */
    private static Stop createStationStop(TimeTable timeTable, int stationId) {
        if (stationId < 0 || stationId >= timeTable.stations().size()) {
            throw new IllegalArgumentException();
        }

        return new Stop(
                timeTable.stations().name(stationId),
                null,
                timeTable.stations().longitude(stationId),
                timeTable.stations().latitude(stationId));
    }
}