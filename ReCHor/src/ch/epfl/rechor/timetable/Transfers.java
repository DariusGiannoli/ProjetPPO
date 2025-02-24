package ch.epfl.rechor.timetable;

/**
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 *
 * L'interface Transfers représente les changements indexés.
 * Dans les données utilisées, les changements ne sont possibles qu'entre (ou au sein de) gares.
 */
public interface Transfers extends Indexed{

    // IL faut lever les IndexOutOfBounds exceptions et la NoSuchElementException  !!!!!!

    /**
     * Retourne l'index de la gare de départ du changement d'index donné.
     *
     * @param id l'index du changement
     * @return l'index de la gare de départ
     */
    int depStationId(int id);

    /**
     * Retourne la durée, en minutes, du changement d'index donné.
     *
     * @param id l'index du changement
     * @return la durée du changement en minutes
     */
    int minutes(int id);

    /**
     * Retourne l'intervalle empaqueté — selon la convention utilisée par PackedRange —
     * des index des changements dont la gare d'arrivée est celle d'index donné.
     *
     * @param stationId l'index de la gare d'arrivée
     * @return l'intervalle empaqueté des index des changements
     */
    int arrivingAt(int stationId);

    /**
     * Retourne la durée, en minutes, du changement entre les deux gares d'index donnés.
     *
     * @param depStationId l'index de la gare de départ
     * @param arrStationId l'index de la gare d'arrivée
     * @return la durée du changement en minutes
     * @throws NoSuchElementException si aucun changement n'est possible entre ces deux gares
     */
    int minutesBetween(int depStationId, int arrStationId);

}
