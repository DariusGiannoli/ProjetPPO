package ch.epfl.rechor.journey;

import ch.epfl.rechor.Bits32_24_8;
import ch.epfl.rechor.PackedRange;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.Connections;
import ch.epfl.rechor.timetable.Stations;
import ch.epfl.rechor.timetable.Transfers;

import java.time.LocalDate;

import java.util.NoSuchElementException;


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
        Profile.Builder builder = new Profile.Builder(timetable, date, destinationStationId);
        Stations stations = timetable.stations();
        int nStations = stations.size();

        // Pré-calcul des temps de marche depuis chaque gare vers la destination.
        int[] walkTimes = new int[nStations];
        Transfers transfers = timetable.transfers();
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
            final int currentCid = cId;
            final int arrivalStop = connections.arrStopId(currentCid);
            final int arrivalStation = timetable.stationId(arrivalStop);
            final int arrMinsOfConn = connections.arrMins(currentCid);
            final int tripId = connections.tripId(currentCid);
            final int depTime = connections.depMins(currentCid);
            final int departureStop = connections.depStopId(currentCid);
            final int departureStation = timetable.stationId(departureStop);

            // Création d'un Builder pour accumuler
            // la frontière temporaire de la connexion courante.
            ParetoFront.Builder f = new ParetoFront.Builder();

            // Option 1 : Descendre à l'arrivée de la liaison et marcher jusqu'à la destination,
            // si le temps de marche depuis la gare d'arrivée est faisable.
            if (walkTimes[arrivalStation] >= 0) {
                int arrivalTimeWithWalk = arrMinsOfConn + walkTimes[arrivalStation];
                int newPayload = Bits32_24_8.pack(currentCid, 0);
                // Le tuple est ajouté avec 0 changement
                f.add(PackedCriteria.pack(arrivalTimeWithWalk, 0, newPayload));
            }

            // Option 2 : Continuer dans le même véhicule (la course).
            ParetoFront.Builder bT = builder.forTrip(tripId);
            if (bT != null) {
                f.addAll(bT);
            }

            // Option 3 : Changer de véhicule à l'arrivée de la liaison.
            ParetoFront.Builder bS = builder.forStation(timetable.stationId(connections.arrStopId(currentCid)));
            int payload = Bits32_24_8.pack(currentCid, 0);

            if (bS != null) {
                bS.forEach(tuple -> {
                    int tupleDepMins = PackedCriteria.depMins(tuple);
                    if (tupleDepMins >= arrMinsOfConn) {
                        int arrMins = PackedCriteria.arrMins(tuple);
                        int changes = PackedCriteria.changes(tuple);
                        long newCriteria = PackedCriteria.pack(arrMins, changes, payload);
                        f.add(PackedCriteria.withAdditionalChange(newCriteria));
                    }
                });
            }

            // Optimisation 1 : Si la frontière calculée pour la connexion est vide,
            // on passe à la suivante.
            if (f.isEmpty()) {
                continue;
            }

            // Mise à jour de la frontière de la course correspondante.
            if (bT == null) {
                builder.setForTrip(tripId, new ParetoFront.Builder(f));
            } else {
                bT.addAll(f);
            }


            // Optimisation 2 : Mise à jour conditionnelle de la frontière de la gare de départ.
            int arrivingAt = transfers.arrivingAt(departureStation);
            for(int j = PackedRange.startInclusive(arrivingAt); j < PackedRange.endExclusive(arrivingAt); j++) {
                int depMinusTransfer = depTime - transfers.minutes(j);
                ParetoFront.Builder DbS = builder.forStation(transfers.depStationId(j));

                if(DbS == null) {
                    builder.setForStation(transfers.depStationId(j), new ParetoFront.Builder());
                }

                ParetoFront.Builder finalDbS = builder.forStation(transfers.depStationId(j));;
                if(finalDbS.fullyDominates(f, depTime)) {
                    continue;
                }

                f.forEach((tuple) -> {
                    int connectionId = Bits32_24_8.unpack24(PackedCriteria.payload(tuple));
                    int TripPosition = connections.tripPos(connectionId);

                    int newPayload = Bits32_24_8.pack(currentCid, TripPosition - connections.tripPos(currentCid));

                    long newCriteria = PackedCriteria.withDepMins(PackedCriteria.pack(PackedCriteria.arrMins(tuple), PackedCriteria.changes(tuple), newPayload), depMinusTransfer);
                    finalDbS.add(newCriteria);
                });


            }

        }


        return builder.build();
    }
}

