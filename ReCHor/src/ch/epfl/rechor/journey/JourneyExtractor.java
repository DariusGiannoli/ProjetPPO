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

        pf.forEach(criteria -> {
            List<Journey.Leg> legs = new ArrayList<>();
            extractLegs(profile, depStationId, criteria, legs);
            if (!legs.isEmpty()) {
                journeys.add(new Journey(legs));
            }
        });

        journeys.sort(Comparator
                .comparing(Journey::depTime)
                .thenComparing(Journey::arrTime));

        return journeys;
    }

    /**
     * Extrait les étapes d'un voyage à partir des critères.
     */
    private static void extractLegs(Profile profile, int depStationId, long criteria, List<Journey.Leg> legs) {
        TimeTable timeTable = profile.timeTable();
        Connections connections = profile.connections();
        Trips trips = profile.trips();

        int connectionId = Bits32_24_8.unpack24(PackedCriteria.payload(criteria));
        int interStops = Bits32_24_8.unpack8(PackedCriteria.payload(criteria));
        int changes = PackedCriteria.changes(criteria);
        int endMins = PackedCriteria.arrMins(criteria);
        int depTime = PackedCriteria.depMins(criteria);

        int depStopId = connections.depStopId(connectionId);

        // Ajouter une étape à pied initiale si nécessaire
        if (timeTable.stationId(depStopId) != depStationId) {
            Stop depStation = createStationStop(timeTable, depStationId);
            Stop arrStop = createStop(timeTable, depStopId);

            LocalDateTime depDateTime = createDateTime(profile.date(), depTime);
            LocalDateTime arrDateTime = depDateTime.plusMinutes(
                    timeTable.transfers().minutesBetween(depStationId, timeTable.stationId(depStopId)));

            legs.add(new Journey.Leg.Foot(depStation, depDateTime, arrStop, arrDateTime));
        }

        // Traitement des étapes de transport et des correspondances
        for (int i = 0; i <= changes; i++) {
            // Obtenir les informations de la connexion
            depStopId = connections.depStopId(connectionId);
            int arrStopId = connections.arrStopId(connectionId);
            int depMinutes = connections.depMins(connectionId);
            int arrMinutes = connections.arrMins(connectionId);
            int tripId = connections.tripId(connectionId);

            // Collecter les arrêts intermédiaires
            List<Journey.Leg.IntermediateStop> intermediateStops = new ArrayList<>();
            int nextConnId = connectionId;
            for (int j = 0; j < interStops; j++) {
                nextConnId = connections.nextConnectionId(nextConnId);

                Stop interStop = createStop(timeTable, arrStopId);
                LocalDateTime arrDateTime = createDateTime(profile.date(), arrMinutes);
                LocalDateTime depDateTime = createDateTime(profile.date(), connections.depMins(nextConnId));

                intermediateStops.add(new Journey.Leg.IntermediateStop(interStop, arrDateTime, depDateTime));

                arrStopId = connections.arrStopId(nextConnId);
                arrMinutes = connections.arrMins(nextConnId);
            }

            // Créer l'étape en transport
            Stop depStop = createStop(timeTable, depStopId);
            Stop arrStop = createStop(timeTable, arrStopId);

            LocalDateTime depDateTime = createDateTime(profile.date(), depMinutes);
            LocalDateTime arrDateTime = createDateTime(profile.date(), arrMinutes);

            int routeId = trips.routeId(tripId);
            Vehicle vehicle = timeTable.routes().vehicle(routeId);
            String routeName = timeTable.routes().name(routeId);
            String destination = trips.destination(tripId);

            legs.add(new Journey.Leg.Transport(
                    depStop, depDateTime, arrStop, arrDateTime,
                    intermediateStops, vehicle, routeName, destination));

            // Préparer la prochaine étape
            int currentStationId = timeTable.stationId(arrStopId);

            // Si ce n'est pas la dernière étape
            if (i < changes) {
                try {
                    ParetoFront nextFront = profile.forStation(currentStationId);
                    long nextCriteria = nextFront.get(endMins, changes - i - 1);
                    int nextConnectionId = Bits32_24_8.unpack24(PackedCriteria.payload(nextCriteria));
                    int nextInterStops = Bits32_24_8.unpack8(PackedCriteria.payload(nextCriteria));

                    int nextDepStopId = connections.depStopId(nextConnectionId);

                    // Ajouter une étape à pied (changement)
                    Stop nextDepStop = createStop(timeTable, nextDepStopId);
                    int transferMinutes = timeTable.transfers().minutesBetween(
                            currentStationId, timeTable.stationId(nextDepStopId));
                    LocalDateTime nextTime = arrDateTime.plusMinutes(transferMinutes);

                    legs.add(new Journey.Leg.Foot(arrStop, arrDateTime, nextDepStop, nextTime));

                    connectionId = nextConnectionId;
                    interStops = nextInterStops;
                } catch (NoSuchElementException e) {
                    // Pas de critère suivant, finir avec une étape à pied si nécessaire
                    if (currentStationId != profile.arrStationId()) {
                        Stop finalStop = createStationStop(timeTable, profile.arrStationId());
                        int transferMinutes = timeTable.transfers().minutesBetween(
                                currentStationId, profile.arrStationId());
                        LocalDateTime finalArrDateTime = arrDateTime.plusMinutes(transferMinutes);

                        legs.add(new Journey.Leg.Foot(arrStop, arrDateTime, finalStop, finalArrDateTime));
                    }
                    break;
                }
            } else if (currentStationId != profile.arrStationId()) {
                // Dernière étape, mais pas à la destination
                Stop finalStop = createStationStop(timeTable, profile.arrStationId());
                int transferMinutes = timeTable.transfers().minutesBetween(
                        currentStationId, profile.arrStationId());
                LocalDateTime finalArrDateTime = arrDateTime.plusMinutes(transferMinutes);

                legs.add(new Journey.Leg.Foot(arrStop, arrDateTime, finalStop, finalArrDateTime));
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
     * Crée un objet Stop représentant un arrêt à partir de son identifiant.
     */
    private static Stop createStop(TimeTable timeTable, int stopId) {
        if (timeTable.isStationId(stopId)) {
            return createStationStop(timeTable, stopId);
        } else {
            int platformId = stopId - timeTable.stations().size();
            int stationId = timeTable.platforms().stationId(platformId);
            return new Stop(
                    timeTable.stations().name(stationId),
                    timeTable.platforms().name(platformId),
                    timeTable.stations().longitude(stationId),
                    timeTable.stations().latitude(stationId));
        }
    }

    /**
     * Crée un objet Stop représentant une gare à partir de son identifiant.
     */
    private static Stop createStationStop(TimeTable timeTable, int stationId) {
        return new Stop(
                timeTable.stations().name(stationId),
                null,
                timeTable.stations().longitude(stationId),
                timeTable.stations().latitude(stationId));
    }
}