package ch.epfl.rechor.journey;

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

    private JourneyExtractor() {}

    /**
     * Extrait tous les voyages optimaux permettant d'aller de la gare d'index donné
     * à la gare d'arrivée du profil.
     *
     * @param profile le profil contenant les frontières de Pareto
     * @param depStationId l'index de la gare de départ
     * @return une liste de voyages triés par heure de départ puis d'arrivée
     */
    public static List<Journey> journeys(Profile profile, int depStationId) {
        List<Journey> journeys = new ArrayList<>();
        ParetoFront pf = profile.forStation(depStationId);

        pf.forEach(criteria -> {
            try {
                List<Journey.Leg> legs = new ArrayList<>();
                extractLegs(profile, depStationId, criteria, legs);
                if (!legs.isEmpty()) {
                    journeys.add(new Journey(legs));
                }
            } catch (Exception e) {
                // Ignorer les erreurs d'extraction
            }
        });

        journeys.sort(Comparator
                .comparing(Journey::depTime)
                .thenComparing(Journey::arrTime));

        return journeys;
    }

    private static void extractLegs(Profile profile, int depStationId, long criteria, List<Journey.Leg> legs) {
        TimeTable timeTable = profile.timeTable();
        Connections connections = profile.connections();
        Trips trips = profile.trips();

        int connectionId = PackedCriteria.payload(criteria);
        int changes = PackedCriteria.changes(criteria);
        int currentStationId = depStationId;

        for (int i = 0; i < changes; i++) {
            // Obtenir les informations de la connexion
            int depStopId = connections.depStopId(connectionId);
            int arrStopId = connections.arrStopId(connectionId);
            int depMinutes = connections.depMins(connectionId);
            int arrMinutes = connections.arrMins(connectionId);
            int tripId = connections.tripId(connectionId);

            // Ajouter une étape à pied initiale si nécessaire
            if (legs.isEmpty() && timeTable.stationId(depStopId) != currentStationId) {
                Stop depStation = createStationStop(timeTable, currentStationId);
                Stop arrStop = createStop(timeTable, depStopId);

                LocalDateTime arrDateTime = createDateTime(profile.date(), depMinutes);
                LocalDateTime depDateTime = arrDateTime.minusMinutes(5);

                legs.add(new Journey.Leg.Foot(depStation, depDateTime, arrStop, arrDateTime));
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
                    List.of(), vehicle, routeName, destination));

            // Préparer la prochaine étape
            int nextStationId = timeTable.stationId(arrStopId);

            // Si ce n'est pas la dernière étape
            if (i < changes - 1) {
                try {
                    ParetoFront nextFront = profile.forStation(nextStationId);
                    long nextCriteria = nextFront.get(arrMinutes, changes - i - 1);
                    int nextConnectionId = PackedCriteria.payload(nextCriteria);

                    int nextDepStopId = connections.depStopId(nextConnectionId);
                    int nextDepMinutes = connections.depMins(nextConnectionId);

                    // Ajouter une étape à pied (changement) si nécessaire
                    if (timeTable.stationId(nextDepStopId) == nextStationId) {
                        Stop nextDepStop = createStop(timeTable, nextDepStopId);
                        LocalDateTime nextDepDateTime = createDateTime(profile.date(), nextDepMinutes);

                        legs.add(new Journey.Leg.Foot(arrStop, arrDateTime, nextDepStop, nextDepDateTime));
                    }

                    connectionId = nextConnectionId;
                } catch (NoSuchElementException e) {
                    // Pas de critère suivant, finir avec une étape à pied si nécessaire
                    if (nextStationId != profile.arrStationId()) {
                        Stop finalStop = createStationStop(timeTable, profile.arrStationId());
                        LocalDateTime finalArrDateTime = arrDateTime.plusMinutes(5);

                        legs.add(new Journey.Leg.Foot(arrStop, arrDateTime, finalStop, finalArrDateTime));
                    }
                    break;
                }
            } else if (nextStationId != profile.arrStationId()) {
                // Dernière étape, mais pas à la destination
                Stop finalStop = createStationStop(timeTable, profile.arrStationId());
                LocalDateTime finalArrDateTime = arrDateTime.plusMinutes(5);

                legs.add(new Journey.Leg.Foot(arrStop, arrDateTime, finalStop, finalArrDateTime));
            }

            currentStationId = nextStationId;
        }
    }

    private static LocalDateTime createDateTime(LocalDate date, int minutes) {
        return LocalDateTime.of(date, LocalTime.of(minutes / 60, minutes % 60));
    }

    private static Stop createStop(TimeTable timeTable, int stopId) {
        if (timeTable.isStationId(stopId)) {
            int stationId = stopId;
            return new Stop(
                    timeTable.stations().name(stationId),
                    null,
                    timeTable.stations().longitude(stationId),
                    timeTable.stations().latitude(stationId));
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

    private static Stop createStationStop(TimeTable timeTable, int stationId) {
        return new Stop(
                timeTable.stations().name(stationId),
                null,
                timeTable.stations().longitude(stationId),
                timeTable.stations().latitude(stationId));
    }
}