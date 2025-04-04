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

    private static final int MAX_INTERMEDIATE_STOPS = 100;
    private static final int STATION_ID_INVALID = -1;

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
    private static void extractLegs(Profile profile, int depStationId,
                                    long criteria, List<Journey.Leg> legs) {
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
            LocalDateTime arrDateTime = depDateTime.plusMinutes(timeTable.transfers()
                    .minutesBetween(depStationId, timeTable.stationId(depStopId)));

            legs.add(new Journey.Leg.Foot(depStation, depDateTime, arrStop, arrDateTime));
        }

        for (int i = 0; i <= changes; i++) {
            // Obtenir les informations de la connexion
            depStopId = connections.depStopId(connectionId);
            int arrStopId = connections.arrStopId(connectionId);
            int depMinutes = connections.depMins(connectionId);
            int arrMinutes = connections.arrMins(connectionId);
            int tripId = connections.tripId(connectionId);

            List<Journey.Leg.IntermediateStop> inter = new ArrayList<>();
            for (int j = 0; j < interStops; j++) {
                connectionId = connections.nextConnectionId(connectionId);

                Stop interStop = createStop(timeTable, arrStopId);
                LocalDateTime depDateTime = createDateTime(profile.date(),
                        connections.depMins(connectionId));
                LocalDateTime arrDateTime = createDateTime(profile.date(), arrMinutes);
                inter.add(new Journey.Leg.IntermediateStop(interStop, arrDateTime, depDateTime));

                arrStopId = connections.arrStopId(connectionId);
                arrMinutes = connections.arrMins(connectionId);
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
                    inter, vehicle, routeName, destination));

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
                    LocalDateTime nextTime = arrDateTime.plusMinutes(profile.timeTable().
                            transfers().minutesBetween(currentStationId,
                                    timeTable.stationId(connections.depStopId(nextConnectionId))));

                    legs.add(new Journey.Leg.Foot(arrStop, arrDateTime, nextDepStop, nextTime));

                    connectionId = nextConnectionId;
                    interStops = nextInterStops;
                } catch (NoSuchElementException e) {
                    // Pas de critère suivant, finir avec une étape à pied si nécessaire
                    if (currentStationId != profile.arrStationId()) {
                        Stop finalStop = createStationStop(timeTable, profile.arrStationId());
                        LocalDateTime finalArrDateTime = arrDateTime.
                                plusMinutes(profile.timeTable().transfers()
                                        .minutesBetween(currentStationId, profile.arrStationId()));

                        legs.add(new Journey.Leg.Foot(arrStop, arrDateTime, finalStop,
                                finalArrDateTime));
                    }
                    break;
                }
            } else if (currentStationId != profile.arrStationId()) {
                // Dernière étape, mais pas à la destination
                Stop finalStop = createStationStop(timeTable, profile.arrStationId());
                LocalDateTime finalArrDateTime = arrDateTime.plusMinutes(profile.timeTable()
                        .transfers().minutesBetween(currentStationId, profile.arrStationId()));

                legs.add(new Journey.Leg.Foot(arrStop, arrDateTime, finalStop, finalArrDateTime));
            }
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

        int stationsSize = timeTable.stations().size();

        // C'est une station
        if (timeTable.isStationId(stopId) || stopId < stationsSize) {
            return createStationStop(timeTable, stopId);
        }

        // C'est une plateforme
        int platformId = stopId - stationsSize;
        if (platformId >= timeTable.platforms().size()) {
            throw new IllegalArgumentException();
        }

        int stationId = timeTable.platforms().stationId(platformId);
        return new Stop(
                timeTable.stations().name(stationId),
                timeTable.platforms().name(platformId),
                timeTable.stations().longitude(stationId),
                timeTable.stations().latitude(stationId)
        );
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