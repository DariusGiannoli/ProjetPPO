package ch.epfl.rechor.journey;

import ch.epfl.rechor.timetable.Connections;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.Trips;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Enregistrement qui représente un profil, qui est une table donnant, pour chaque gare du réseau, la frontière de Pareto des critères d'optimisation pour aller à une gare, un jour donné.
 * @param timeTable l'horaire auquel correspond le profil.
 * @param date la date à laquelle correspond le profil
 * @param arrStationId l'index de la gare d'arrivée à laquelle correspond le profil
 * @param stationFront la table des frontières de Pareto de toutes les gares, qui contient, à un index donné, la frontière de la gare de même index.
 *
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */
public record Profile(TimeTable timeTable, LocalDate date, int arrStationId, List<ParetoFront> stationFront) {

    /**
     * Constructeur de Profile, qui copie la table des frontières de Pareto afin de garantir l'immuabilité de la classe.
     */
    public Profile {
        stationFront = List.copyOf(stationFront);
    }

    /**
     * @return retourne les liaisons correspondant au profil, qui sont celles de l'horaire, à la date à laquelle correspond le profil.
     */
    public Connections connections() {
        return timeTable.connectionsFor(date);
    }

    /**
     * @return retourne les courses correspondant au profil, qui sont celles de l'horaire, à la date à laquelle correspond le profil.
     */
    public Trips trips(){
        return timeTable.tripsFor(date);
    }

    /**
     * @param stationId index de la gare dont on veut la frontière de Pareto.
     * @return retourne la frontière de Pareto pour la gare d'index donné.
     * @throws IndexOutOfBoundsException est lancée si cet index est invalide.
     */
    public ParetoFront forStation(int stationId) {
        if(stationId >= stationFront.size()) {
            throw new IndexOutOfBoundsException();
        }else {
            return stationFront.get(stationId);
        }
    }

    /**
     * La class Builder représente un bâtisseur de profil.
     * Il représente un profil augmenté en cours de construction, mais finit par bâtir un profil simple.
     *
     * @author Antoine Lepin (390950)
     * @author Darius Giannoli (380759)
     */
    public static final class Builder {

        private TimeTable timeTable;
        private LocalDate date;
        private int arrStationId;
        private ParetoFront.Builder[] stationsParetoFront;
        private ParetoFront.Builder[] tripsParetoFront;

        /**
         * Construit un bâtisseur de profil pour l'horaire, la date et la gare de destination donnés.
         * @param timeTable est l'horaire auquel correspond le profil que l'on construit.
         * @param date est la date à laquelle correspond le profil que l'on construit.
         * @param arrStationId est l'index de la gare d'arrivée à laquelle correspond le profil que l'on construit.
         */
        public Builder(TimeTable timeTable, LocalDate date, int arrStationId) {
            this.arrStationId = arrStationId;
            this.date = date;
            this.timeTable = timeTable;

            stationsParetoFront = new ParetoFront.Builder[timeTable.stations().size()];
            tripsParetoFront = new ParetoFront.Builder[timeTable.tripsFor(date).size()];
        }

        /**
         * @param stationId est l'index de la gare dont on veut le bâtisseur de la frontière de Pareto.
         * @return retourne le bâtisseur de la frontière de Pareto pour la gare d'index donné, qui est null si aucun appel à setForStation n'a été fait précédemment pour cette gare.
         * @throws IndexOutOfBoundsException est levée si l'index donné est invalide.
         */
        public ParetoFront.Builder forStation(int stationId){
            if(stationId >= stationsParetoFront.length){
                throw new IndexOutOfBoundsException();
            } else {
                return stationsParetoFront[stationId];
            }
        }

        /**
         * @param tripId est l'index de la course dont on veut le bâtisseur de la frontière de Pareto.
         * @return retourne le bâtisseur de la frontière de Pareto pour la course d'index donné, qui est null si aucun appel à setForStation n'a été fait précédemment pour cette gare.
         * @throws IndexOutOfBoundsException est levée si l'index donné est invalide.
         */
        public ParetoFront.Builder forTrip(int tripId) {
            if(tripId >= tripsParetoFront.length){
                throw new IndexOutOfBoundsException();
            } else {
                return tripsParetoFront[tripId];
            }
        }

        /**
         * Associe le bâtisseur de frontière de Pareto donné à la gare d'index donné.
         * @param stationId index de la gare que l'on veut associer.
         * @param builder index du bâtisseur de frontière de Pareto que l'on veut associer.
         */
        public void setForStation(int stationId, ParetoFront.Builder builder){
            stationsParetoFront[stationId] = builder;
        }

        /**
         * Associe le bâtisseur de frontière de Pareto donné à la course d'index donné.
         * @param tripId index de la course que l'on veut associer.
         * @param builder index du bâtisseur de frontière de Pareto que l'on veut associer.
         */
        public void setForTrip(int tripId, ParetoFront.Builder builder){
            tripsParetoFront[tripId] = builder;
        }

        /**
         * @return retourne le profil simple, sans les frontières de Pareto correspondant aux courses, en cours de construction. Lorsque la methode rencontre un bâtisseur de gare null, met un ParetoFront.EMPTY dans le profil.
         */
        public Profile build(){
            List<ParetoFront> stationFront = new ArrayList<>();
            for (int i = 0; i < stationsParetoFront.length; i++) {
                if(stationsParetoFront[i] == null) {
                    stationFront.add(ParetoFront.EMPTY);
                } else{
                stationFront.add(stationsParetoFront[i].build());
                }
            }
            return new Profile(timeTable, date, arrStationId, stationFront);
        }
    }
}
