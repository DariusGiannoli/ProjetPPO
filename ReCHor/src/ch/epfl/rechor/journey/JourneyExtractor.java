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
 * Classe utilitaire permettant d'extraire des voyages optimaux à partir d'un profil.
 * <p>
 * Cette classe est non instanciable et fournit des méthodes statiques pour
 * extraire et construire des voyages à partir des critères de Pareto.
 * </p>
 *
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */
public final class JourneyExtractor {

    //==========================================================================
    // 1. Constantes
    //==========================================================================

    private static final int MAX_INTERMEDIATE_STOPS = 100;
    private static final int INVALID_ID = -1;

    //==========================================================================
    // 2. Classes internes de support
    //==========================================================================

    /**
     * Contexte d'extraction contenant les références fréquemment utilisées.
     */
    private static final class ExtractionContext {
        final Profile profile;
        final TimeTable timeTable;
        final Connections connections;
        final Trips trips;
        final LocalDate date;

        ExtractionContext(Profile profile) {
            this.profile = profile;
            this.timeTable = profile.timeTable();
            this.connections = profile.connections();
            this.trips = profile.trips();
            this.date = profile.date();
        }

        /**
         * Retourne l'ID de la gare pour un arrêt donné.
         */
        int stationIdForStop(int stopId) {
            return timeTable.stationId(stopId);
        }

        /**
         * Retourne la durée du transfert entre deux gares.
         */
        int transferMinutes(int depStationId, int arrStationId) {
            return timeTable.transfers().minutesBetween(depStationId, arrStationId);
        }

        /**
         * Vérifie si l'arrêt donné est valide.
         */
        boolean isValidStopId(int stopId) {
            return stopId >= 0 && stopId < (timeTable.stations().size() + timeTable.platforms().size());
        }

        /**
         * Vérifie si la connexion donnée est valide.
         */
        boolean isValidConnectionId(int connectionId) {
            return connectionId >= 0 && connectionId < connections.size();
        }
    }

    /**
     * Structure simple pour stocker les critères décodés.
     */
    private static record DecodedCriteria(int connectionId, int interStops, int changes, int endMins) {
        /**
         * Retourne la prochaine connexion après avoir traité les interStops.
         */
        int getConnectionAfterInterStops(Connections connections) {
            int connId = connectionId;
            for (int i = 0; i < interStops; i++) {
                connId = connections.nextConnectionId(connId);
            }
            return connId;
        }
    }

    //==========================================================================
    // 3. Constructeur privé (classe utilitaire)
    //==========================================================================

    private JourneyExtractor() { }

    //==========================================================================
    // 4. Méthode publique principale
    //==========================================================================

    /**
     * Extrait tous les voyages optimaux à partir du profil pour la gare de départ spécifiée.
     * Les voyages sont triés par heure de départ puis par heure d'arrivée.
     *
     * @param profile      le profil contenant les données de Pareto
     * @param depStationId l'identifiant de la gare de départ
     * @return une liste triée des voyages extraits
     */
    public static List<Journey> journeys(Profile profile, int depStationId) {
        List<Journey> journeys = new ArrayList<>();
        ParetoFront paretoFront = profile.forStation(depStationId);
        ExtractionContext context = new ExtractionContext(profile);

        paretoFront.forEach(criteria -> {
            try {
                // Pré-alloue la liste en fonction du nombre de changements
                int changes = PackedCriteria.changes(criteria);
                List<Journey.Leg> legs = new ArrayList<>(changes + 2); // +2 pour les legs à pied potentiels

                // Extraction des legs pour ce critère
                extractLegs(context, depStationId, criteria, legs);

                // Gestion de l'étape finale pour atteindre la destination
                addFinalFootLegIfNeeded(context, legs);

                // Création du voyage si des legs ont été trouvés
                if (!legs.isEmpty()) {
                    journeys.add(new Journey(legs));
                }
            } catch (Exception e) {
                // Log de l'erreur sans interrompre le traitement
                System.err.println("Erreur lors de l'extraction d'un voyage: " + e.getMessage());
            }
        });

        // Tri des voyages par heure de départ puis par heure d'arrivée
        return sortJourneys(journeys);
    }

    //==========================================================================
    // 5. Méthodes principales d'extraction
    //==========================================================================

    /**
     * Extrait les legs d'un voyage à partir d'un critère.
     *
     * @param context      le contexte d'extraction
     * @param depStationId l'identifiant de la gare de départ
     * @param criteria     le critère de la frontière de Pareto
     * @param legs         la liste mutable dans laquelle les legs sont ajoutés
     */
    private static void extractLegs(ExtractionContext context, int depStationId, long criteria, List<Journey.Leg> legs) {
        // Décodage du critère
        DecodedCriteria decoded = decodeCriteria(criteria);

        // Validation de la connexion
        if (!context.isValidConnectionId(decoded.connectionId())) {
            return;
        }

        // Départ effectif de la première liaison
        int initialDepStopId = context.connections.depStopId(decoded.connectionId());

        // Ajout de l'étape à pied initiale si nécessaire
        addInitialFootLegIfNeeded(context, depStationId, initialDepStopId,
                PackedCriteria.depMins(criteria), legs);

        // Traitement des étapes de transport
        processTransportSequence(context, decoded, legs);
    }

    /**
     * Traite une séquence d'étapes de transport et leurs correspondances.
     *
     * @param context le contexte d'extraction
     * @param initial les critères initiaux décodés
     * @param legs    la liste mutable dans laquelle les legs sont ajoutés
     */
    private static void processTransportSequence(ExtractionContext context, DecodedCriteria initial, List<Journey.Leg> legs) {
        int connectionId = initial.connectionId();
        int interStops = initial.interStops();
        int remainingChanges = initial.changes();
        int endMins = initial.endMins();

        for (int i = 0; i <= remainingChanges && context.isValidConnectionId(connectionId); i++) {
            // Création et ajout du leg de transport
            Journey.Leg.Transport transportLeg = createTransportLeg(context, connectionId, interStops);
            legs.add(transportLeg);

            if (i == remainingChanges) {
                break; // C'était le dernier leg de transport
            }

            // Recherche du prochain critère pour la correspondance
            int currentStationId = context.stationIdForStop(Integer.parseInt(transportLeg.arrStop().name()));
            long nextCriteria;
            try {
                nextCriteria = getNextCriteria(context, currentStationId, remainingChanges - i - 1, endMins);
            } catch (NoSuchElementException e) {
                break; // Pas de critère suivant trouvé
            }

            // Décodage du prochain critère
            DecodedCriteria next = decodeCriteria(nextCriteria);
            int nextConnectionId = next.connectionId();

            if (!context.isValidConnectionId(nextConnectionId)) {
                break;
            }

            // Ajout d'un leg à pied pour la correspondance
            int nextDepStopId = context.connections.depStopId(nextConnectionId);
            addConnectionFootLeg(context, transportLeg.arrStop(), nextDepStopId, transportLeg.arrTime(), legs);

            // Mise à jour pour la prochaine itération
            connectionId = nextConnectionId;
            interStops = next.interStops();
        }
    }

    //==========================================================================
    // 6. Méthodes de création de legs
    //==========================================================================

    /**
     * Crée un leg de transport avec ses arrêts intermédiaires.
     *
     * @param context      le contexte d'extraction
     * @param connectionId l'identifiant de la liaison
     * @param interStops   le nombre d'arrêts intermédiaires
     * @return un leg de transport
     */
    private static Journey.Leg.Transport createTransportLeg(ExtractionContext context, int connectionId, int interStops) {
        int depStopId = context.connections.depStopId(connectionId);
        int arrStopId = context.connections.arrStopId(connectionId);
        int tripId = context.connections.tripId(connectionId);
        int depMinutes = context.connections.depMins(connectionId);
        int arrMinutes = context.connections.arrMins(connectionId);

        // Collecte des arrêts intermédiaires si nécessaire
        List<Journey.Leg.IntermediateStop> intermediateStops = new ArrayList<>();
        if (interStops > 0) {
            int finalConnectionId = collectIntermediateStops(context, connectionId, interStops, intermediateStops);
            if (finalConnectionId != connectionId && context.isValidConnectionId(finalConnectionId)) {
                arrStopId = context.connections.arrStopId(finalConnectionId);
                arrMinutes = context.connections.arrMins(finalConnectionId);
            }
        }

        // Création des objets nécessaires
        Stop depStop = createStop(context.timeTable, depStopId);
        Stop arrStop = createStop(context.timeTable, arrStopId);
        LocalDateTime depDateTime = createDateTime(context.date, depMinutes);
        LocalDateTime arrDateTime = createDateTime(context.date, arrMinutes);

        // Extraction des informations de route et destination
        int routeId = context.trips.routeId(tripId);
        String routeName = context.timeTable.routes().name(routeId);
        String destination = context.trips.destination(tripId);
        Vehicle vehicle = context.timeTable.routes().vehicle(routeId);

        return new Journey.Leg.Transport(
                depStop, depDateTime, arrStop, arrDateTime,
                intermediateStops, vehicle, routeName, destination);
    }

    /**
     * Collecte les arrêts intermédiaires pour une étape de transport.
     *
     * @param context      le contexte d'extraction
     * @param connectionId l'identifiant de la liaison initiale
     * @param count        le nombre d'arrêts intermédiaires à collecter
     * @param stops        la liste dans laquelle ajouter les arrêts intermédiaires
     * @return l'identifiant de la dernière liaison traitée
     */
    private static int collectIntermediateStops(ExtractionContext context, int connectionId,
                                                int count, List<Journey.Leg.IntermediateStop> stops) {
        int currentConnId = connectionId;
        int processedStops = 0;
        int maxStops = Math.min(count, MAX_INTERMEDIATE_STOPS);

        while (processedStops < maxStops) {
            try {
                int arrStopId = context.connections.arrStopId(currentConnId);
                int arrMinutes = context.connections.arrMins(currentConnId);

                // Obtenir la prochaine liaison
                int nextConnId = context.connections.nextConnectionId(currentConnId);
                if (!context.isValidConnectionId(nextConnId)) {
                    break;
                }

                // Créer l'arrêt intermédiaire
                Stop interStop = createStop(context.timeTable, arrStopId);
                LocalDateTime arrDateTime = createDateTime(context.date, arrMinutes);
                LocalDateTime depDateTime = createDateTime(context.date, context.connections.depMins(nextConnId));

                stops.add(new Journey.Leg.IntermediateStop(interStop, arrDateTime, depDateTime));

                // Mise à jour pour la prochaine itération
                currentConnId = nextConnId;
                processedStops++;
            } catch (Exception e) {
                break; // En cas d'erreur, on s'arrête là
            }
        }

        return currentConnId;
    }

    //==========================================================================
    // 7. Méthodes de gestion des legs à pied
    //==========================================================================

    /**
     * Ajoute un leg à pied initial si nécessaire.
     *
     * @param context      le contexte d'extraction
     * @param depStationId l'identifiant de la gare de départ
     * @param depStopId    l'identifiant de l'arrêt de départ
     * @param depMinutes   l'heure de départ en minutes
     * @param legs         la liste des legs à compléter
     */
    private static void addInitialFootLegIfNeeded(ExtractionContext context, int depStationId,
                                                  int depStopId, int depMinutes, List<Journey.Leg> legs) {
        if (context.stationIdForStop(depStopId) != depStationId) {
            Stop depStation = createStationStop(context.timeTable, depStationId);
            Stop arrStop = createStop(context.timeTable, depStopId);
            LocalDateTime depDateTime = createDateTime(context.date, depMinutes);

            createAndAddFootLeg(context, depStation, arrStop, depStationId,
                    context.stationIdForStop(depStopId), depDateTime, legs);
        }
    }

    /**
     * Ajoute un leg à pied pour une correspondance.
     *
     * @param context  le contexte d'extraction
     * @param arrStop  l'arrêt d'arrivée de l'étape précédente
     * @param nextDepStopId l'identifiant de l'arrêt de départ suivant
     * @param arrTime  l'heure d'arrivée à l'arrêt
     * @param legs     la liste des legs à compléter
     */
    private static void addConnectionFootLeg(ExtractionContext context, Stop arrStop,
                                             int nextDepStopId, LocalDateTime arrTime, List<Journey.Leg> legs) {
        Stop nextDepStop = createStop(context.timeTable, nextDepStopId);
        int arrStopStationId = context.stationIdForStop(Integer.parseInt(arrStop.name()));
        int nextDepStopStationId = context.stationIdForStop(nextDepStopId);

        createAndAddFootLeg(context, arrStop, nextDepStop, arrStopStationId, nextDepStopStationId, arrTime, legs);
    }

    /**
     * Ajoute un leg à pied final si nécessaire.
     *
     * @param context le contexte d'extraction
     * @param legs    la liste des legs à compléter
     */
    private static void addFinalFootLegIfNeeded(ExtractionContext context, List<Journey.Leg> legs) {
        if (legs.isEmpty()) {
            return;
        }

        Journey.Leg lastLeg = legs.get(legs.size() - 1);
        Stop lastArrStop = lastLeg.arrStop();
        int lastStationId = context.stationIdForStop(Integer.parseInt(lastArrStop.name()));

        if (lastStationId != context.profile.arrStationId()) {
            Stop destinationStop = createStationStop(context.timeTable, context.profile.arrStationId());

            createAndAddFootLeg(context, lastArrStop, destinationStop,
                    lastStationId, context.profile.arrStationId(),
                    lastLeg.arrTime(), legs);
        }
    }

    /**
     * Crée et ajoute un leg à pied à la liste des legs.
     */
    private static void createAndAddFootLeg(ExtractionContext context, Stop depStop, Stop arrStop,
                                            int depStationId, int arrStationId,
                                            LocalDateTime depDateTime, List<Journey.Leg> legs) {
        try {
            int transferMinutes = context.transferMinutes(depStationId, arrStationId);
            LocalDateTime arrDateTime = depDateTime.plusMinutes(transferMinutes);
            legs.add(new Journey.Leg.Foot(depStop, depDateTime, arrStop, arrDateTime));
        } catch (Exception e) {
            System.err.println("Erreur lors de la création d'un leg à pied: " + e.getMessage());
        }
    }

    //==========================================================================
    // 8. Méthodes utilitaires pour les critères
    //==========================================================================

    /**
     * Décode les critères d'optimisation en valeurs individuelles.
     *
     * @param criteria le critère à décoder
     * @return un objet contenant les différentes valeurs décodées
     */
    private static DecodedCriteria decodeCriteria(long criteria) {
        return new DecodedCriteria(
                Bits32_24_8.unpack24(PackedCriteria.payload(criteria)), // connectionId
                Bits32_24_8.unpack8(PackedCriteria.payload(criteria)),  // interStops
                PackedCriteria.changes(criteria),                       // changes
                PackedCriteria.arrMins(criteria)                        // endMins
        );
    }

    /**
     * Recherche le prochain critère d'optimisation dans la frontière de Pareto.
     *
     * @param context          le contexte d'extraction
     * @param stationId        l'identifiant de la gare courante
     * @param remainingChanges le nombre de changements restants
     * @param endMins          les minutes d'arrivée finale attendues
     * @return le critère pour la prochaine étape
     * @throws NoSuchElementException si aucun critère n'est disponible
     */
    private static long getNextCriteria(ExtractionContext context, int stationId,
                                        int remainingChanges, int endMins) {
        ParetoFront nextFront = context.profile.forStation(stationId);
        return nextFront.get(endMins, remainingChanges);
    }

    //==========================================================================
    // 9. Méthodes auxiliaires de création et manipulation
    //==========================================================================

    /**
     * Crée un objet LocalDateTime à partir d'une date et de minutes.
     *
     * @param date    la date
     * @param minutes le nombre de minutes après minuit
     * @return le LocalDateTime correspondant
     */
    private static LocalDateTime createDateTime(LocalDate date, int minutes) {
        return LocalDateTime.of(date, LocalTime.MIDNIGHT).plusMinutes(minutes);
    }

    /**
     * Crée un objet Stop à partir d'un ID d'arrêt.
     *
     * @param timeTable l'horaire
     * @param stopId    l'identifiant de l'arrêt à créer
     * @return le Stop correspondant
     * @throws IllegalArgumentException si l'identifiant est invalide
     */
    private static Stop createStop(TimeTable timeTable, int stopId) {
        if (stopId < 0) {
            throw new IllegalArgumentException("Identifiant d'arrêt négatif: " + stopId);
        }

        if (timeTable.isStationId(stopId)) {
            return createStationStop(timeTable, stopId);
        } else {
            int platformId = stopId - timeTable.stations().size();
            if (platformId >= timeTable.platforms().size()) {
                throw new IllegalArgumentException("Identifiant de quai invalide: " + platformId);
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
     * @param timeTable l'horaire
     * @param stationId l'identifiant de la gare
     * @return le Stop représentant la gare
     * @throws IllegalArgumentException si l'identifiant est invalide
     */
    private static Stop createStationStop(TimeTable timeTable, int stationId) {
        if (stationId < 0 || stationId >= timeTable.stations().size()) {
            throw new IllegalArgumentException("Identifiant de gare invalide: " + stationId);
        }

        return new Stop(
                timeTable.stations().name(stationId),
                null,
                timeTable.stations().longitude(stationId),
                timeTable.stations().latitude(stationId));
    }

    /**
     * Trie une liste de voyages par heure de départ puis heure d'arrivée.
     *
     * @param journeys la liste de voyages à trier
     * @return la liste triée
     */
    private static List<Journey> sortJourneys(List<Journey> journeys) {
        journeys.sort(Comparator
                .comparing(Journey::depTime)
                .thenComparing(Journey::arrTime));
        return journeys;
    }
}