package ch.epfl.rechor.timetable;

/**
 * L'interface StationAliases représente les noms alternatifs des gares.
 * Elle étend l'interface Indexed et fournit des méthodes pour accéder
 * aux informations d'un alias.
 *
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */
public interface StationAliases extends Indexed {

    /**
     * Retourne le nom alternatif (alias) à l'index donné.
     *
     * @param id l'index de l'alias
     * @return le nom alternatif
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    String alias(int id);

    /**
     * Retourne le nom de la gare associé à l'alias à l'index donné.
     *
     * @param id l'index de l'alias
     * @return le nom de la gare associée
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    String stationName(int id);
}