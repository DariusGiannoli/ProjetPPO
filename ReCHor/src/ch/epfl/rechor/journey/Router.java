package ch.epfl.rechor.journey;

import ch.epfl.rechor.Bits32_24_8;
import ch.epfl.rechor.PackedRange;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.Connections;
import ch.epfl.rechor.timetable.Stations;
import ch.epfl.rechor.timetable.Transfers;

import java.time.LocalDate;

import java.util.NoSuchElementException;


/**
 * Représente un objet capable de calculer le profil de tous les voyages optimaux permettant de se
 * rendre de n'importe quelle gare du réseau à une gare d'arrivée donnée, un jour donné.
 * @param timetable table des horaires de transport public indéxés.
 *
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */
public record Router(TimeTable timetable) {

    /**
     * Calcule le profil des voyages optimaux permettant de se rendre à la gare de destination
     * (destinationStationId) pour le jour spécifié.
     *
     * @param date                 la date du voyage.
     * @param destinationStationId l'indice de la gare de destination.
     * @return le profil (Profile) des voyages optimaux.
     */
    public Profile profile(LocalDate date, int destinationStationId) {
        // Récupération des gares et du nombre total de gares.
        Profile.Builder profileBuilder = new Profile.Builder(timetable, date, destinationStationId);
        Stations stations = timetable.stations();
        int nStations = stations.size();

        // Pré-calcul des temps de marche depuis chaque gare vers la destination.
        Transfers transfers = timetable.transfers();
        int[] walkTimes = new int[nStations];
        for (int i = 0; i < nStations; i++) {
            try {
                walkTimes[i] = transfers.minutesBetween(i, destinationStationId);
            } catch (NoSuchElementException e) {
                walkTimes[i] = -1;
            }
        }

        // Récupération des liaisons actives pour la date.
        Connections connections = timetable.connectionsFor(date);
        int nConnections = connections.size();

        // Parcours de chaque liaison par ordre d'index
        // (les connexions sont déjà triées par ordre décroissant d'heure de départ).
        for (int cId = 0; cId < nConnections; cId++) {
            final int arrivalStop = connections.arrStopId(cId);
            final int arrivalStation = timetable.stationId(arrivalStop);
            final int arrMinsOfConn = connections.arrMins(cId);
            final int tripId = connections.tripId(cId);

            // Builder pour accumuler la frontière temporaire de la connexion courante.
            ParetoFront.Builder builder = new ParetoFront.Builder();
            // Option 1
            firstOption(walkTimes[arrivalStation], arrMinsOfConn, cId, builder);
            // Option 2
            ParetoFront.Builder builderForTrip = profileBuilder.forTrip(tripId);
            secondOption(builderForTrip, builder);
            // Option 3
            ParetoFront.Builder builderForStation = profileBuilder.forStation
                    (timetable.stationId(connections.arrStopId(cId)));
            thirdOption(builderForStation, cId, arrMinsOfConn, builder);

            // Optimisation 1: Si frontière calculée est vide. passer à la suivante
            if (builder.isEmpty()) {continue;}

            if (builderForTrip == null) {
                profileBuilder.setForTrip(tripId, new ParetoFront.Builder(builder));
            } else {
                builderForTrip.addAll(builder);
            }
            //Optimisation 2
            secondOptimisation(transfers, connections, builder, profileBuilder, cId);
        }
        return profileBuilder.build();
    }

    /**
     *  Option 1 de l'algorithme : Descendre à l'arrivée de la liaison et
     *  marcher jusqu'à la destination si le temps de marche depuis la gare d'arrivée est faisable.
     * @param walkTime temps de marche depuis la gare d'arrivée vers la destination.
     * @param arrMinsOfConn l'heure d'arrivée de la liaison en minutes.
     * @param currentConnectionId l'id de la liaison.
     * @param builder le bâtisseur de frontière de Pareto auquel on ajoute le tuple.
     */
    private void firstOption(int walkTime, int arrMinsOfConn,
                             int currentConnectionId, ParetoFront.Builder builder) {
        if (walkTime >= 0) {
            int arrivalTimeWithWalk = arrMinsOfConn + walkTime;
            int newPayload = Bits32_24_8.pack(currentConnectionId, 0);
            // Le tuple est ajouté avec 0 changement
            builder.add(PackedCriteria.pack(arrivalTimeWithWalk, 0, newPayload));
        }
    }

    /**
     * Option 2 de l'algorithme : Continuer dans le même véhicule (la course).
     * @param builderForTrip le bâtisseur de frontière de Pareto pour la course à laquelle
     *                       appartient la liaison.
     * @param builder le bâtisseur de frontière de Pareto auquel on ajoute builderForTrip.
     */
    private void secondOption(ParetoFront.Builder builderForTrip, ParetoFront.Builder builder) {
        if (builderForTrip != null) {
            builder.addAll(builderForTrip);
        }
    }

    /**
     * // Option 3 de l'algorithme : Changer de véhicule à l'arrivée de la liaison.
     * @param builderStation le bâtisseur de frontière de Pareto pour la gare d'arrivée de la
     *                       liaison.
     * @param connectionId l'id de la liaison.
     * @param arrMinsOfConn l'heure d'arrivée de la liaison en minutes.
     * @param builder le bâtisseur de frontière de Pareto auquel on ajoute builderStation.
     */
    private void thirdOption(ParetoFront.Builder builderStation, int connectionId,
                             int arrMinsOfConn, ParetoFront.Builder builder) {
        int payload = Bits32_24_8.pack(connectionId, 0);

        if (builderStation != null) {
            builderStation.forEach(tuple -> {
                int tupleDepMins = PackedCriteria.depMins(tuple);
                if (tupleDepMins >= arrMinsOfConn) {
                    int arrMins = PackedCriteria.arrMins(tuple);
                    int changes = PackedCriteria.changes(tuple);
                    long newCriteria = PackedCriteria.pack(arrMins, changes, payload);
                    builder.add(PackedCriteria.withAdditionalChange(newCriteria));
                }
            });
        }
    }

    /**
     * Deuxième optimisation, la mise à jour des frontières des gares n'a pas besoin d'être faite
     * si tous les tuples de la frontière builder, augmentés de l'heure de départ de la liaison,
     * sont dominés par au moins un tuple de la frontière de la gare de départ de la liaison.
     *
     * @param transfers les transfers indéxés.
     * @param connections les liaisons indéxées.
     * @param builder le bâtisseur de frontière de Pareto temporaire de la connexion courante.
     * @param profileBuilder le bâtisseur du profil augmenté en construction.
     * @param currentConnectionId id de la liaison.
     */
    public void secondOptimisation(Transfers transfers, Connections connections,
                                   ParetoFront.Builder builder, Profile.Builder profileBuilder,
                                   int currentConnectionId) {
        int departureStation = timetable.stationId(connections.depStopId(currentConnectionId));
        int depTime = connections.depMins(currentConnectionId);

        int arrivingAt = transfers.arrivingAt(departureStation);
        for(int j = PackedRange.startInclusive(arrivingAt);
            j < PackedRange.endExclusive(arrivingAt); j++) {
            int depStationId = transfers.depStationId(j);
            int depMinusTransfer = depTime - transfers.minutes(j);
            ParetoFront.Builder secondStationBuilder = profileBuilder.forStation(depStationId);

            if(secondStationBuilder == null) {
                profileBuilder.setForStation(depStationId, new ParetoFront.Builder());
            }

            ParetoFront.Builder finalStationBuilder = profileBuilder.forStation(depStationId);
            if(finalStationBuilder.fullyDominates(builder, depTime)) {
                continue;
            }

            builder.forEach((tuple) -> {
                int connectionId = Bits32_24_8.unpack24(PackedCriteria.payload(tuple));
                int TripPosition = connections.tripPos(connectionId);
                int currentTripPos = connections.tripPos(currentConnectionId);

                int newPayload = Bits32_24_8.pack(currentConnectionId,TripPosition - currentTripPos);

                long newCriteria = PackedCriteria.withDepMins(
                        PackedCriteria.pack(PackedCriteria.arrMins(tuple),
                                PackedCriteria.changes(tuple), newPayload), depMinusTransfer);
                finalStationBuilder.add(newCriteria);
            });

        }
    }
}

