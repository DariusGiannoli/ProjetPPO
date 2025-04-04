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
 *
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */
public final class JourneyExtractor {

    // Constante pour limiter le nombre d'arrêts intermédiaires
    private static final int MAX_INTERMEDIATE_STOPS = 100;

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

        // Extraction des voyages à partir de chaque critère de la frontière
        pf.forEach(criteria -> {
            List<Journey.Leg> legs = new ArrayList<>();
            try {
                extractLegs(profile, depStationId, criteria, legs);
                if (!legs.isEmpty()) {
                    journeys.add(new Journey(legs));
                }
            } catch (Exception e) {
                // Ignorer ce voyage et continuer avec les autres
            }
        });

        // Tri des voyages par heure de départ puis par heure d'arrivée
        journeys.sort(Comparator
                .comparing(Journey::depTime)
                .thenComparing(Journey::arrTime));

        return journeys;
    }

    /**
     * Extrait les étapes d'un voyage à partir des critères.
     *
     * @param profile      le profil contenant les données
     * @param depStationId l'identifiant de la gare de départ
     * @param criteria     les critères du voyage
     * @param legs         la liste des étapes à remplir
     */
    private static void extractLegs(Profile profile, int depStationId, long criteria,
                                    List<Journey.Leg> legs) {
        // Récupération des objets fréquemment utilisés
        TimeTable timeTable = profile.timeTable();
        Connections connections = profile.connections();

        // Extraction des données du critère
        int connectionId = Bits32_24_8.unpack24(PackedCriteria.payload(criteria));
        int interStops = Bits32_24_8.unpack8(PackedCriteria.payload(criteria));
        int changes = PackedCriteria.changes(criteria);
        int endMins = PackedCriteria.arrMins(criteria);
        int depMins = PackedCriteria.depMins(criteria);

        // Vérification de validité
        if (connectionId >= connections.size()) {
            return;
        }

        // Étape 1: Ajouter une étape à pied initiale si nécessaire
        connectionId = addInitialFootLegIfNeeded(timeTable, depStationId,
                connectionId, connections,
                profile.date(), depMins, legs);

        // Étape 2: Parcourir tous les segments du voyage
        processAllSegments(profile, connectionId, interStops, changes, endMins, legs);
    }

    /**
     * Ajoute une étape à pied initiale si nécessaire et retourne l'ID de la connexion à utiliser.
     */
    private static int addInitialFootLegIfNeeded(TimeTable timeTable, int depStationId,
                                                 int connectionId, Connections connections,
                                                 LocalDate date, int depMins,
                                                 List<Journey.Leg> legs) {
        int depStopId = connections.depStopId(connectionId);
        int currentStationId = timeTable.stationId(depStopId);

        if (currentStationId != depStationId) {
            Stop depStation = createStationStop(timeTable, depStationId);
            Stop arrStop = createStop(timeTable, depStopId);

            LocalDateTime depDateTime = createDateTime(date, depMins);
            int walkTime = timeTable.transfers().minutesBetween(depStationId, currentStationId);
            LocalDateTime arrDateTime = depDateTime.plusMinutes(walkTime);

            legs.add(new Journey.Leg.Foot(depStation, depDateTime, arrStop, arrDateTime));
        }

        return connectionId;
    }

    /**
     * Traite tous les segments du voyage.
     */
    private static void processAllSegments(Profile profile, int connectionId, int interStops,
                                           int changes, int endMins, List<Journey.Leg> legs) {
        TimeTable timeTable = profile.timeTable();
        Connections connections = profile.connections();
        Trips trips = profile.trips();
        LocalDate date = profile.date();

        int currentConnectionId = connectionId;
        int currentInterStops = interStops;

        for (int i = 0; i <= changes; i++) {
            // Récupérer les informations de la connexion courante
            int depStopId = connections.depStopId(currentConnectionId);
            int arrStopId = connections.arrStopId(currentConnectionId);
            int tripId = connections.tripId(currentConnectionId);

            // Traiter l'étape en transport
            ConnectionResult result = processTransportLeg(
                    timeTable, connections, trips, date,
                    currentConnectionId, currentInterStops,
                    depStopId, arrStopId, tripId, legs);

            if (result == null) break;

            // Mettre à jour la station courante
            int currentStationId = timeTable.stationId(result.arrStopId);
            LocalDateTime arrDateTime = result.arrDateTime;
            Stop arrStop = createStop(timeTable, result.arrStopId);

            // Gérer la suite du voyage
            if (i < changes) {
                // Chercher le prochain segment
                NextSegmentResult nextSegment = findNextSegment(
                        profile, timeTable, connections, currentStationId,
                        endMins, changes - i - 1);

                if (nextSegment == null) {
                    // Pas de segment suivant, ajouter une étape à pied finale si nécessaire
                    addFinalFootLegIfNeeded(profile, timeTable, currentStationId,
                            arrStop, arrDateTime, legs);
                    break;
                }

                // Ajouter une étape à pied pour le changement
                legs.add(new Journey.Leg.Foot(
                        arrStop, arrDateTime,
                        nextSegment.nextStop, nextSegment.nextDepDateTime));

                // Mettre à jour les variables pour la prochaine étape
                currentConnectionId = nextSegment.nextConnectionId;
                currentInterStops = nextSegment.nextInterStops;

            } else if (currentStationId != profile.arrStationId()) {
                // Dernière étape mais pas à destination, ajouter une étape à pied finale
                addFinalFootLegIfNeeded(profile, timeTable, currentStationId,
                        arrStop, arrDateTime, legs);
            }
        }
    }

    /**
     * Traite une étape en transport et retourne les informations du résultat.
     */
    private static ConnectionResult processTransportLeg(TimeTable timeTable,
                                                        Connections connections,
                                                        Trips trips, LocalDate date,
                                                        int connectionId, int interStops,
                                                        int depStopId, int arrStopId, int tripId,
                                                        List<Journey.Leg> legs) {
        // Collecter les arrêts intermédiaires
        IntermediateStopsResult intermediateResult = collectIntermediateStops(
                connections, timeTable, date, connectionId, interStops);

        if (intermediateResult.intermediateStops.size() > 0) {
            // Mettre à jour l'arrêt d'arrivée si des arrêts intermédiaires ont été collectés
            arrStopId = connections.arrStopId(intermediateResult.lastConnectionId);
        }

        // Obtenir les heures de départ et d'arrivée
        int depTime = connections.depMins(connectionId);
        int arrTime = connections.arrMins(
                intermediateResult.intermediateStops.isEmpty() ?
                        connectionId : intermediateResult.lastConnectionId);

        // Créer les objets pour l'étape
        Stop depStop = createStop(timeTable, depStopId);
        Stop arrStop = createStop(timeTable, arrStopId);

        LocalDateTime depDateTime = createDateTime(date, depTime);
        LocalDateTime arrDateTime = createDateTime(date, arrTime);

        // Créer l'étape en transport
        int routeId = trips.routeId(tripId);
        Vehicle vehicle = timeTable.routes().vehicle(routeId);
        String routeName = timeTable.routes().name(routeId);
        String destination = trips.destination(tripId);

        legs.add(new Journey.Leg.Transport(
                depStop, depDateTime, arrStop, arrDateTime,
                intermediateResult.intermediateStops, vehicle, routeName, destination));

        return new ConnectionResult(arrStopId, arrDateTime);
    }

    /**
     * Collecte les arrêts intermédiaires d'une étape et retourne le résultat.
     */
    private static IntermediateStopsResult collectIntermediateStops(Connections connections,
                                                                    TimeTable timeTable,
                                                                    LocalDate date,
                                                                    int connectionId,
                                                                    int maxStops) {
        List<Journey.Leg.IntermediateStop> stops = new ArrayList<>();
        int currentId = connectionId;
        int lastId = connectionId;

        for (int j = 0; j < Math.min(maxStops, MAX_INTERMEDIATE_STOPS); j++) {
            int arrStopId = connections.arrStopId(currentId);
            int arrTime = connections.arrMins(currentId);

            currentId = connections.nextConnectionId(currentId);
            if (currentId >= connections.size()) break;

            int depTime = connections.depMins(currentId);

            Stop interStop = createStop(timeTable, arrStopId);
            LocalDateTime arrDateTime = createDateTime(date, arrTime);
            LocalDateTime depDateTime = createDateTime(date, depTime);

            stops.add(new Journey.Leg.IntermediateStop(
                    interStop, arrDateTime, depDateTime));

            lastId = currentId;
        }

        return new IntermediateStopsResult(stops, lastId);
    }

    /**
     * Recherche les informations du prochain segment du voyage.
     */
    private static NextSegmentResult findNextSegment(Profile profile, TimeTable timeTable,
                                                     Connections connections, int currentStationId,
                                                     int endMins, int remainingChanges) {
        try {
            ParetoFront stationFront = profile.forStation(currentStationId);
            long nextCriteria = stationFront.get(endMins, remainingChanges);

            int nextConnectionId = Bits32_24_8.unpack24(PackedCriteria.payload(nextCriteria));
            int nextInterStops = Bits32_24_8.unpack8(PackedCriteria.payload(nextCriteria));

            if (nextConnectionId >= connections.size()) {
                return null;
            }

            int nextDepStopId = connections.depStopId(nextConnectionId);
            Stop nextStop = createStop(timeTable, nextDepStopId);

            // Calculer l'heure de départ du prochain segment
            int arrTime = connections.arrMins(nextConnectionId);
            LocalDateTime arrDateTime = createDateTime(profile.date(), arrTime);

            int walkTime = timeTable.transfers().minutesBetween(
                    currentStationId, timeTable.stationId(nextDepStopId));
            LocalDateTime nextDepDateTime = arrDateTime.plusMinutes(walkTime);

            return new NextSegmentResult(nextConnectionId, nextInterStops,
                    nextStop, nextDepDateTime);
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    /**
     * Ajoute une étape à pied finale si nécessaire.
     */
    private static void addFinalFootLegIfNeeded(Profile profile, TimeTable timeTable,
                                                int currentStationId, Stop currentStop,
                                                LocalDateTime arrDateTime, List<Journey.Leg> legs) {
        if (currentStationId != profile.arrStationId()) {
            try {
                Stop finalStop = createStationStop(timeTable, profile.arrStationId());
                int transferTime = timeTable.transfers().minutesBetween(
                        currentStationId, profile.arrStationId());

                LocalDateTime finalArrDateTime = arrDateTime.plusMinutes(transferTime);
                legs.add(new Journey.Leg.Foot(currentStop, arrDateTime,
                        finalStop, finalArrDateTime));
            } catch (Exception e) {
                // Ignorer si l'ajout n'est pas possible
            }
        }
    }

    /**
     * Crée un objet LocalDateTime à partir d'une date et de minutes après minuit.
     */
    private static LocalDateTime createDateTime(LocalDate date, int minutes) {
        return LocalDateTime.of(date, LocalTime.MIDNIGHT).plusMinutes(minutes);
    }

    /**
     * Crée un objet Stop à partir d'un identifiant d'arrêt.
     */
    private static Stop createStop(TimeTable timeTable, int stopId) {
        if (timeTable.isStationId(stopId)) {
            return createStationStop(timeTable, stopId);
        }

        int platformId = stopId - timeTable.stations().size();
        int stationId = timeTable.platforms().stationId(platformId);

        return new Stop(
                timeTable.stations().name(stationId),
                timeTable.platforms().name(platformId),
                timeTable.stations().longitude(stationId),
                timeTable.stations().latitude(stationId));
    }

    /**
     * Crée un objet Stop représentant une gare.
     */
    private static Stop createStationStop(TimeTable timeTable, int stationId) {
        return new Stop(
                timeTable.stations().name(stationId),
                null,
                timeTable.stations().longitude(stationId),
                timeTable.stations().latitude(stationId));
    }

    /**
     * Résultat d'une connexion comprenant l'arrêt d'arrivée et l'heure d'arrivée.
     */
    private static class ConnectionResult {
        final int arrStopId;
        final LocalDateTime arrDateTime;

        ConnectionResult(int arrStopId, LocalDateTime arrDateTime) {
            this.arrStopId = arrStopId;
            this.arrDateTime = arrDateTime;
        }
    }

    /**
     * Résultat de la collecte des arrêts intermédiaires.
     */
    private static class IntermediateStopsResult {
        final List<Journey.Leg.IntermediateStop> intermediateStops;
        final int lastConnectionId;

        IntermediateStopsResult(List<Journey.Leg.IntermediateStop> intermediateStops,
                                int lastConnectionId) {
            this.intermediateStops = intermediateStops;
            this.lastConnectionId = lastConnectionId;
        }
    }

    /**
     * Résultat de la recherche du prochain segment.
     */
    private static class NextSegmentResult {
        final int nextConnectionId;
        final int nextInterStops;
        final Stop nextStop;
        final LocalDateTime nextDepDateTime;

        NextSegmentResult(int nextConnectionId, int nextInterStops,
                          Stop nextStop, LocalDateTime nextDepDateTime) {
            this.nextConnectionId = nextConnectionId;
            this.nextInterStops = nextInterStops;
            this.nextStop = nextStop;
            this.nextDepDateTime = nextDepDateTime;
        }
    }
}