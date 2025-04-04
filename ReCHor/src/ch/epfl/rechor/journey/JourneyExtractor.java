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
 * Optimisée pour la concision et l'efficacité en réduisant les appels redondants et en
 * pré-cachant les valeurs récurrentes.
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
     * Extrait les étapes d'un voyage à partir des critères d'optimisation.
     *
     * @param profile      le profil contenant les frontières de Pareto
     * @param depStationId l'indice de la gare de départ
     * @param criteria     les critères d'optimisation empaquetés
     * @param legs         la liste dans laquelle ajouter les étapes extraites
     */
    private static void extractLegs(Profile profile, int depStationId, long criteria, List<Journey.Leg> legs) {
        TimeTable timeTable = profile.timeTable();
        Connections connections = profile.connections();
        Trips trips = profile.trips();

        // Décodage des critères d'optimisation
        int connectionId = Bits32_24_8.unpack24(PackedCriteria.payload(criteria));
        int interStops = Bits32_24_8.unpack8(PackedCriteria.payload(criteria));
        int changes = PackedCriteria.changes(criteria);
        int endMins = PackedCriteria.arrMins(criteria);
        int depTime = PackedCriteria.depMins(criteria);

        int depStopId = connections.depStopId(connectionId);

        // Ajouter une étape à pied initiale si nécessaire
        if (timeTable.stationId(depStopId) != depStationId) {
            addInitialFootLeg(profile, timeTable, depStationId, depStopId, depTime, legs);
        }

        for (int i = 0; i <= changes; i++) {
            // Traiter chaque étape de transport et changement éventuel associé
            processTransportLeg(profile, timeTable, connections, trips, connectionId, interStops,
                    changes, endMins, i, legs);

            // Mettre à jour l'ID de connexion pour la prochaine itération (si nécessaire)
            // Cette mise à jour est faite à l'intérieur de processTransportLeg
            if (i < changes) {
                try {
                    ParetoFront nextFront = profile.forStation(timeTable.stationId(legs.get(legs.size() - 1).arrStop()));
                    long nextCriteria = nextFront.get(endMins, changes - i - 1);
                    connectionId = Bits32_24_8.unpack24(PackedCriteria.payload(nextCriteria));
                    interStops = Bits32_24_8.unpack8(PackedCriteria.payload(nextCriteria));
                } catch (NoSuchElementException e) {
                    // Pas de critère suivant, on a terminé les changements
                    break;
                }
            }
        }
    }

    /**
     * Ajoute une étape à pied initiale si l'arrêt de départ n'est pas la gare de départ choisie.
     */
    private static void addInitialFootLeg(Profile profile, TimeTable timeTable, int depStationId,
                                          int depStopId, int depTime, List<Journey.Leg> legs) {

        Stop depStation = createStationStop(timeTable, depStationId);
        Stop arrStop = createStop(timeTable, depStopId);

        LocalDateTime depDateTime = createDateTime(profile.date(), depTime);
        LocalDateTime arrDateTime = depDateTime.plusMinutes(
                timeTable.transfers().minutesBetween(depStationId, timeTable.stationId(depStopId)));

        legs.add(new Journey.Leg.Foot(depStation, depDateTime, arrStop, arrDateTime));
    }

    /**
     * Traite une étape de transport et ajoute éventuellement une étape à pied qui suit.
     */
    private static void processTransportLeg(Profile profile, TimeTable timeTable, Connections connections,
                                            Trips trips, int connectionId, int interStops, int changes, int endMins, int currentChange,
                                            List<Journey.Leg> legs) {

        // Obtenir les informations de la connexion
        int depStopId = connections.depStopId(connectionId);
        int arrStopId = connections.arrStopId(connectionId);
        int depMinutes = connections.depMins(connectionId);
        int arrMinutes = connections.arrMins(connectionId);
        int tripId = connections.tripId(connectionId);

        // Collecter les arrêts intermédiaires
        List<Journey.Leg.IntermediateStop> intermediateStops = collectIntermediateStops(
                profile, timeTable, connections, connectionId, interStops);

        // Créer et ajouter l'étape en transport
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

        // Vérifier s'il faut ajouter une étape à pied finale
        int currentStationId = timeTable.stationId(arrStopId);

        if (currentChange < changes) {
            // Pas le dernier changement, on doit ajouter une étape à pied (changement)
            addTransferLeg(profile, timeTable, connections, arrStop, currentStationId, arrDateTime, legs);
        } else if (currentStationId != profile.arrStationId()) {
            // Dernier changement mais pas à la destination finale
            addFinalFootLeg(profile, timeTable, arrStop, currentStationId, arrDateTime, legs);
        }
    }

    /**
     * Collecte les arrêts intermédiaires pour une étape de transport.
     */
    private static List<Journey.Leg.IntermediateStop> collectIntermediateStops(
            Profile profile, TimeTable timeTable, Connections connections,
            int connectionId, int interStops) {

        List<Journey.Leg.IntermediateStop> intermediateStops = new ArrayList<>();
        int nextConnId = connectionId;
        int arrStopId = connections.arrStopId(connectionId);
        int arrMinutes = connections.arrMins(connectionId);

        for (int j = 0; j < interStops; j++) {
            nextConnId = connections.nextConnectionId(nextConnId);

            Stop interStop = createStop(timeTable, arrStopId);
            LocalDateTime arrDateTime = createDateTime(profile.date(), arrMinutes);
            LocalDateTime depDateTime = createDateTime(profile.date(), connections.depMins(nextConnId));

            intermediateStops.add(new Journey.Leg.IntermediateStop(interStop, arrDateTime, depDateTime));

            arrStopId = connections.arrStopId(nextConnId);
            arrMinutes = connections.arrMins(nextConnId);
        }

        return intermediateStops;
    }

    /**
     * Ajoute une étape à pied de correspondance entre deux étapes de transport.
     */
    private static void addTransferLeg(Profile profile, TimeTable timeTable, Connections connections,
                                       Stop arrStop, int currentStationId, LocalDateTime arrDateTime, List<Journey.Leg> legs) {

        try {
            ParetoFront nextFront = profile.forStation(currentStationId);
            long nextCriteria = nextFront.get(PackedCriteria.arrMins(nextFront.get(0)), 0);
            int nextConnectionId = Bits32_24_8.unpack24(PackedCriteria.payload(nextCriteria));
            int nextDepStopId = connections.depStopId(nextConnectionId);

            // Ajouter une étape à pied (changement)
            Stop nextDepStop = createStop(timeTable, nextDepStopId);
            int transferMinutes = timeTable.transfers().minutesBetween(
                    currentStationId, timeTable.stationId(nextDepStopId));
            LocalDateTime nextTime = arrDateTime.plusMinutes(transferMinutes);

            legs.add(new Journey.Leg.Foot(arrStop, arrDateTime, nextDepStop, nextTime));
        } catch (NoSuchElementException e) {
            // Pas de critère suivant, finir avec une étape à pied si nécessaire
            if (currentStationId != profile.arrStationId()) {
                addFinalFootLeg(profile, timeTable, arrStop, currentStationId, arrDateTime, legs);
            }
        }
    }

    /**
     * Ajoute une étape à pied finale vers la destination.
     */
    private static void addFinalFootLeg(Profile profile, TimeTable timeTable,
                                        Stop arrStop, int currentStationId, LocalDateTime arrDateTime, List<Journey.Leg> legs) {

        Stop finalStop = createStationStop(timeTable, profile.arrStationId());
        int transferMinutes = timeTable.transfers().minutesBetween(
                currentStationId, profile.arrStationId());
        LocalDateTime finalArrDateTime = arrDateTime.plusMinutes(transferMinutes);

        legs.add(new Journey.Leg.Foot(arrStop, arrDateTime, finalStop, finalArrDateTime));
    }

    /**
     * Crée un objet LocalDateTime à partir d'une date et de minutes après minuit.
     *
     * @param date    la date
     * @param minutes le nombre de minutes après minuit
     * @return l'objet LocalDateTime correspondant
     */
    private static LocalDateTime createDateTime(LocalDate date, int minutes) {
        return LocalDateTime.of(date, LocalTime.MIDNIGHT).plusMinutes(minutes);
    }

    /**
     * Crée un objet Stop représentant un arrêt à partir de son identifiant.
     *
     * @param timeTable l'horaire contenant les données
     * @param stopId    l'identifiant de l'arrêt
     * @return un objet Stop représentant l'arrêt
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
     *
     * @param timeTable l'horaire contenant les données
     * @param stationId l'identifiant de la gare
     * @return un objet Stop représentant la gare
     */
    private static Stop createStationStop(TimeTable timeTable, int stationId) {
        return new Stop(
                timeTable.stations().name(stationId),
                null,
                timeTable.stations().longitude(stationId),
                timeTable.stations().latitude(stationId));
    }
}