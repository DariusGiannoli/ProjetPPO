package ch.epfl.rechor.timetable;

import java.util.NoSuchElementException;

/**
 * L'interface Transfers représente les changements indexés.
 * Dans les données utilisées, les changements ne sont possibles qu'entre (ou au sein de) gares.
 *
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */
public interface Transfers extends Indexed {

    /**
     * Retourne l'index de la gare de départ pour le changement identifié par l'index donné.
     *
     * @param id l'index du changement
     * @return l'index de la gare de départ
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    int depStationId(int id);

    /**
     * Retourne la durée (en minutes) du changement identifié par l'index donné.
     *
     * @param id l'index du changement
     * @return la durée du changement en minutes
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    int minutes(int id);

    /**
     * Retourne l'intervalle empaqueté
     * des index des changements dont la gare d'arrivée correspond à l'index donné.
     *
     * @param stationId l'index de la gare d'arrivée
     * @return l'intervalle empaqueté des index des changements
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    int arrivingAt(int stationId);

    /**
     * Retourne la durée (en minutes)
     * du changement entre les gares identifiées par les index donnés.
     *
     * @param depStationId l'index de la gare de départ
     * @param arrStationId l'index de la gare d'arrivée
     * @return la durée du changement en minutes
     * @throws NoSuchElementException    si aucun changement n'est possible entre ces gares
     * @throws IndexOutOfBoundsException si l'un des index est invalide
     */
    int minutesBetween(int depStationId, int arrStationId);
}