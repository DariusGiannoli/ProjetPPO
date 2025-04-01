package ch.epfl.rechor.journey;

import ch.epfl.rechor.timetable.Connections;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.Trips;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Enregistrement qui représente un profil, c'est-à-dire une table donnant pour chaque gare du réseau
 * la frontière de Pareto des critères d'optimisation permettant d'atteindre une gare (destination)
 * à une date donnée.
 *
 * @param timeTable    l'horaire auquel correspond le profil.
 * @param date         la date du profil.
 * @param arrStationId l'index de la gare d'arrivée (destination).
 * @param stationFront la table des frontières de Pareto pour les gares.
 *
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */
public record Profile(TimeTable timeTable, LocalDate date, int arrStationId,
                      List<ParetoFront> stationFront) {

    /**Constructeur compact qui copie la table des frontières de Pareto afin de garantir l'immuabilité.*/
    public Profile {
        stationFront = List.copyOf(stationFront);
    }

    /**
     * Retourne les liaisons correspondant au profil.
     *
     * @return les connexions de l'horaire pour la date du profil.
     */
    public Connections connections() {
        return timeTable.connectionsFor(date);
    }

    /**
     * Retourne les courses correspondant au profil.
     *
     * @return les trips de l'horaire pour la date du profil.
     */
    public Trips trips() {
        return timeTable.tripsFor(date);
    }

    /**
     * Retourne la frontière de Pareto associée à la gare d'index donné.
     *
     * @param stationId l'indice de la gare
     * @return la frontière de Pareto pour la gare
     * @throws IndexOutOfBoundsException si l'indice est invalide (négatif ou hors bornes).
     */
    public ParetoFront forStation(int stationId) {
        if (stationId >= stationFront.size()) {
            throw new IndexOutOfBoundsException();
        }
        return stationFront.get(stationId);
    }

    /**
     * La classe Builder permet de construire un profil augmenté.
     * Un profil augmenté contient, outre les frontières pour les gares, celles pour les courses.
     *
     * @author Antoine Lepin (390950)
     * @author Darius Giannoli (380759)
     */
    public static final class Builder {

        private final TimeTable timeTable;
        private final LocalDate date;
        private final int arrStationId;
        private final ParetoFront.Builder[] stationsParetoFront;
        private final ParetoFront.Builder[] tripsParetoFront;

        /**
         * Construit un bâtisseur de profil pour l'horaire, la date et la gare de destination donnés.
         *
         * @param timeTable    l'horaire associé.
         * @param date         la date du profil.
         * @param arrStationId l'indice de la gare d'arrivée (destination).
         */
        public Builder(TimeTable timeTable, LocalDate date, int arrStationId) {
            this.arrStationId = arrStationId;
            this.date = date;
            this.timeTable = timeTable;
            this.stationsParetoFront = new ParetoFront.Builder[timeTable.stations().size()];
            this.tripsParetoFront = new ParetoFront.Builder[timeTable.tripsFor(date).size()];
        }

        /**
         * Retourne le bâtisseur de frontière de Pareto pour la gare d'index donné.
         *
         * @param stationId l'indice de la gare
         * @return le bâtisseur de la frontière ou null si non défini
         * @throws IndexOutOfBoundsException si l'indice est invalide.
         */
        public ParetoFront.Builder forStation(int stationId) {
            if (stationId >= stationsParetoFront.length) {
                throw new IndexOutOfBoundsException();
            }
            return stationsParetoFront[stationId];
        }

        /**
         * Retourne le bâtisseur de frontière de Pareto pour la course d'index donné.
         *
         * @param tripId l'indice de la course
         * @return le bâtisseur ou null si non défini
         * @throws IndexOutOfBoundsException si l'indice est invalide.
         */
        public ParetoFront.Builder forTrip(int tripId) {
            if (tripId >= tripsParetoFront.length) {
                throw new IndexOutOfBoundsException();
            }
            return tripsParetoFront[tripId];
        }

        /**
         * Associe le bâtisseur de frontière à la gare d'index donné.
         *
         * @param stationId l'indice de la gare
         * @param builder   le bâtisseur à associer
         */
        public void setForStation(int stationId, ParetoFront.Builder builder) {
            stationsParetoFront[stationId] = builder;
        }

        /**
         * Associe le bâtisseur de frontière à la course d'index donné.
         *
         * @param tripId  l'indice de la course
         * @param builder le bâtisseur à associer
         */
        public void setForTrip(int tripId, ParetoFront.Builder builder) {
            tripsParetoFront[tripId] = builder;
        }

        /**
         * Construit le profil final en copiant la frontière pour chaque gare.
         * Si un bâtisseur n'a pas été défini, la constante ParetoFront.EMPTY est utilisée.
         *
         * @return le profil construit.
         */
        public Profile build() {
            List<ParetoFront> stationFront = new ArrayList<>();
            for (ParetoFront.Builder builder : stationsParetoFront) {
                stationFront.add(builder == null ? ParetoFront.EMPTY : builder.build());
            }
            return new Profile(timeTable, date, arrStationId, stationFront);
        }
    }
}