package ch.epfl.rechor.journey;

import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.Connections;
import ch.epfl.rechor.timetable.Trips;
import ch.epfl.rechor.timetable.Stations;
import ch.epfl.rechor.timetable.Transfers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;


public final record Router(TimeTable timetable) {

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

        // Initialisation des frontières de Pareto pour chaque gare.
        ParetoFront.Builder[] stationPF = new ParetoFront.Builder[nStations];
        for (int i = 0; i < nStations; i++) {
            stationPF[i] = new ParetoFront.Builder();
        }

        // Récupération des courses actives pour la date
        // et initialisation des frontières pour chaque course.
        Trips trips = timetable.tripsFor(date);
        int nTrips = trips.size();
        ParetoFront.Builder[] tripPF = new ParetoFront.Builder[nTrips];
        for (int i = 0; i < nTrips; i++) {
            tripPF[i] = new ParetoFront.Builder();
        }

        // Récupération des liaisons actives pour la date.
        Connections connections = timetable.connectionsFor(date);
        int nConnections = connections.size();

        // Parcours de chaque liaison par ordre d'index
        // (les connexions sont déjà triées par ordre décroissant d'heure de départ).
        for (int cid = 0; cid < nConnections; cid++) {
            final int currentCid = cid;
            final int arrivalStation = connections.arrStopId(currentCid);
            final int arrMinsOfConn = connections.arrMins(currentCid);
            final int tripId = connections.tripId(currentCid);
            final int depTime = connections.depMins(currentCid);
            final int departureStation = connections.depStopId(currentCid);

            // Création d'un Builder pour accumuler la frontière temporaire de la connexion courante.
            ParetoFront.Builder f = new ParetoFront.Builder();

            // Option 1 : Descendre à l'arrivée de la liaison et marcher jusqu'à la destination,
            // si le temps de marche depuis la gare d'arrivée est faisable.
            if (walkTimes[arrivalStation] >= 0) {
                int arrivalTimeWithWalk = arrMinsOfConn + walkTimes[arrivalStation];
                // Le tuple est ajouté avec 0 changement
                // et le payload fixé à l'identifiant de la connexion.
                f.add(arrivalTimeWithWalk, 0, currentCid);
            }

            // Option 2 : Continuer dans le même véhicule (la course).
            tripPF[tripId].forEach(tuple -> {
                int arrMins = PackedCriteria.arrMins(tuple);
                int changes = PackedCriteria.changes(tuple);
                int payload = PackedCriteria.payload(tuple);
                f.add(arrMins, changes, payload);
            });

            // Option 3 : Changer de véhicule à l'arrivée de la liaison.
            stationPF[arrivalStation].forEach(tuple -> {
                int tupleDepMins = PackedCriteria.depMins(tuple);
                if (tupleDepMins >= arrMinsOfConn) {
                    int arrMins = PackedCriteria.arrMins(tuple);
                    int changes = PackedCriteria.changes(tuple) + 1;
                    int payload = PackedCriteria.payload(tuple);
                    f.add(arrMins, changes, payload);
                }
            });

            // Optimisation 1 : Si la frontière calculée pour la connexion est vide,
            // on passe à la suivante.
            if (f.isEmpty()) {
                continue;
            }

            // Mise à jour de la frontière de la course correspondante.
            tripPF[tripId].addAll(f);

            // Optimisation 2 : Mise à jour conditionnelle de la frontière de la gare de départ.
            if (!stationPF[departureStation].fullyDominates(f, depTime)) {
                f.forEach(tuple -> {
                    long modifiedTuple = PackedCriteria.withDepMins(tuple, depTime);
                    stationPF[departureStation].add(modifiedTuple);
                });
            }
        }

        // Construction finale :
        // création d'une liste immuable des frontières de Pareto pour chaque gare.
        List<ParetoFront> stationFronts = new ArrayList<>(nStations);
        for (int i = 0; i < nStations; i++) {
            stationFronts.add(stationPF[i].build());
        }

        // Retour du profil final encapsulant l'horaire, la date,
        // la gare de destination et les frontières.
        return new Profile(timetable, date, destinationStationId, stationFronts);
    }
}
