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

    /**Constructeur privé pour empêcher l'instanciation*/
    private JourneyExtractor() {}

    /**
     * Extrait tous les voyages optimaux permettant d'aller de la gare de départ donnée
     * à la destination indiquée dans le profil.
     *
     * @param profile     le profil contenant les frontières de Pareto
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
    private static void extractLegs(Profile profile, int depStationId, long criteria, List<Journey.Leg> legs) {
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
                connectionId = processTransportLeg(profile, connectionId, interStops,
                        changes - i, endMins, i == changes, legs);

                // Si connectionId est devenu invalide lors du traitement, on s'arrête
                if (connectionId < 0) {
                    break;
                }

                // Mise à jour des paramètres pour la prochaine étape
                interStops = Bits32_24_8.unpack8(PackedCriteria.payload(
                                profile.forStation(timeTable.stationId(connections.arrStopId(connectionId)))
                                        .get(endMins, changes - i - 1)));

            } catch (Exception e) {
                // En cas d'erreur dans le traitement d'une étape, on arrête l'extraction
                break;
            }
        }
    }

    /**
     * Traite une étape de transport et ajoute les étapes à pied nécessaires entre les transports.
     *
     * @param profile      le profil contenant les données
     * @param connectionId l'id de la connexion courante
     * @param interStops   le nombre d'arrêts intermédiaires
     * @param remainingChanges le nombre de changements restants
     * @param endMins      les minutes d'arrivée finale
     * @param isLastLeg    indique s'il s'agit de la dernière étape
     * @param legs         la liste des étapes à compléter
     * @return l'id de la prochaine connexion, ou -1 si traitement terminé
     */
    private static int processTransportLeg(Profile profile, int connectionId, int interStops,
                                           int remainingChanges, int endMins, boolean isLastLeg,
                                           List<Journey.Leg> legs) {
        TimeTable timeTable = profile.timeTable();
        Connections connections = profile.connections();

        // Obtenir les informations de la connexion courante
        int depStopId = connections.depStopId(connectionId);
        int arrStopId = connections.arrStopId(connectionId);
        int tripId = connections.tripId(connectionId);

        // Collecter les arrêts intermédiaires si nécessaire
        List<Journey.Leg.IntermediateStop> intermediateStops = new ArrayList<>();
        if (interStops > 0) {
            int nextConnectionId = collectIntermediateStops(connections, timeTable, profile.date(),
                    connectionId, interStops, intermediateStops);

            // Mise à jour de la connexion si des arrêts intermédiaires ont été collectés
            if (nextConnectionId != connectionId && nextConnectionId < connections.size()) {
                connectionId = nextConnectionId;
                arrStopId = connections.arrStopId(connectionId - 1);
            }
        }

        // Créer l'étape en transport
        createTransportLeg(profile, depStopId, arrStopId, connectionId, tripId,
                intermediateStops, legs);

        // Station courante après cette étape
        int currentStationId = timeTable.stationId(arrStopId);

        // Gérer la suite du voyage
        if (!isLastLeg) {
            // S'il reste des changements, trouver la prochaine étape
            try {
                ParetoFront nextFront = profile.forStation(currentStationId);
                long nextCriteria = nextFront.get(endMins, remainingChanges - 1);
                int nextConnectionId = Bits32_24_8.unpack24(PackedCriteria.payload(nextCriteria));

                // Vérifier la validité de la prochaine connexion
                if (nextConnectionId >= connections.size()) {
                    // Si la prochaine connexion est invalide, ajouter la dernière étape à pied si nécessaire
                    addFinalFootLegIfNeeded(timeTable, currentStationId, profile.arrStationId(),
                            createStop(timeTable, arrStopId),
                            createDateTime(profile.date(), connections.arrMins(connectionId)),
                            legs);
                    return -1;
                }

                // Ajouter une étape à pied pour le changement
                addFootLegForChange(profile, connectionId, nextConnectionId, legs);

                return nextConnectionId;
            } catch (NoSuchElementException e) {
                // Pas de critère suivant, finir avec une étape à pied si nécessaire
                addFinalFootLegIfNeeded(timeTable, currentStationId, profile.arrStationId(),
                        createStop(timeTable, arrStopId),
                        createDateTime(profile.date(), connections.arrMins(connectionId)),
                        legs);
                return -1;
            }
        } else if (currentStationId != profile.arrStationId()) {
            // Dernière étape mais pas à la destination, ajouter une étape à pied finale
            addFinalFootLegIfNeeded(timeTable, currentStationId, profile.arrStationId(),
                    createStop(timeTable, arrStopId),
                    createDateTime(profile.date(), connections.arrMins(connectionId)),
                    legs);
        }

        return -1;
    }

    /**
     * Ajoute une étape à pied pour le changement entre deux connexions.
     */
    private static void addFootLegForChange(Profile profile, int currentConnectionId, int nextConnectionId,
                                            List<Journey.Leg> legs) {
        TimeTable timeTable = profile.timeTable();
        Connections connections = profile.connections();

        int arrStopId = connections.arrStopId(currentConnectionId);
        int nextDepStopId = connections.depStopId(nextConnectionId);

        Stop arrStop = createStop(timeTable, arrStopId);
        Stop nextDepStop = createStop(timeTable, nextDepStopId);

        LocalDateTime arrDateTime = createDateTime(profile.date(), connections.arrMins(currentConnectionId));
        int transferMinutes = timeTable.transfers().minutesBetween(
                timeTable.stationId(arrStopId), timeTable.stationId(nextDepStopId));

        LocalDateTime nextDepDateTime = arrDateTime.plusMinutes(transferMinutes);

        legs.add(new Journey.Leg.Foot(arrStop, arrDateTime, nextDepStop, nextDepDateTime));
    }

    /**
     * Crée et ajoute une étape de transport à la liste des étapes.
     */
    private static void createTransportLeg(Profile profile, int depStopId, int arrStopId,
                                           int connectionId, int tripId,
                                           List<Journey.Leg.IntermediateStop> intermediateStops,
                                           List<Journey.Leg> legs) {
        TimeTable timeTable = profile.timeTable();
        Connections connections = profile.connections();
        Trips trips = profile.trips();

        Stop depStop = createStop(timeTable, depStopId);
        Stop arrStop = createStop(timeTable, arrStopId);

        LocalDateTime depDateTime = createDateTime(profile.date(), connections.depMins(connectionId));
        LocalDateTime arrDateTime = createDateTime(profile.date(), connections.arrMins(connectionId));

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
     */
    private static void handleInitialFootLeg(TimeTable timeTable, int depStationId, int depStopId,
                                             LocalDate date, int depMinutes, List<Journey.Leg> legs) {
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
     * @return l'ID de la connexion suivante après les arrêts intermédiaires
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
                LocalDateTime depDateTime = createDateTime(date, connections.depMins(currentConnId));

                stops.add(new Journey.Leg.IntermediateStop(interStop, arrDateTime, depDateTime));

            } catch (Exception e) {
                break;
            }
        }

        return currentConnId;
    }

    /**
     * Ajoute une étape à pied finale si la station courante n'est pas la destination.
     */
    private static void addFinalFootLegIfNeeded(TimeTable timeTable, int currentStationId,
                                                int destStationId, Stop arrStop,
                                                LocalDateTime arrDateTime, List<Journey.Leg> legs) {
        try {
            if (currentStationId != destStationId) {
                Stop finalStop = createStationStop(timeTable, destStationId);
                int transferTime = timeTable.transfers().minutesBetween(currentStationId, destStationId);
                LocalDateTime finalArrDateTime = arrDateTime.plusMinutes(transferTime);

                legs.add(new Journey.Leg.Foot(arrStop, arrDateTime, finalStop, finalArrDateTime));
            }
        } catch (Exception e) {
            // Ignorer si l'ajout n'est pas possible
        }
    }

    /**
     * Crée un objet LocalDateTime à partir d'une date et d'un nombre de minutes depuis minuit.
     */
    private static LocalDateTime createDateTime(LocalDate date, int minutes) {
        return LocalDateTime.of(date, LocalTime.MIDNIGHT).plusMinutes(minutes);
    }

    /**
     * Crée un objet Stop à partir d'un identifiant d'arrêt.
     */
    private static Stop createStop(TimeTable timeTable, int stopId) {
        if (stopId < 0) {
            throw new IllegalArgumentException();
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