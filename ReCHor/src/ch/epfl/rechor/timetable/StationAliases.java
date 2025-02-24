package ch.epfl.rechor.timetable;

/**
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 *
 *  L'interface StationAliases représente les noms alternatifs des gares.
 *  Elle étend l'interface Indexed et fournit des méthodes pour accéder
 *  aux informations d'un nom alternatif.
 */
public interface StationAliases extends Indexed {

    // IL faut lever les IndexOutOfBounds exceptions !!!!!!

    /**
     * Retourne le nom alternatif (alias) d'index donné.
     *
     * @param id l'index du nom alternatif
     * @return le nom alternatif
     */
    String alias(int id);

    /**
     * Retourne le nom de la gare à laquelle correspond l'alias d'index donné.
     *
     * @param id l'index du nom alternatif
     * @return le nom de la gare associée
     */
    String stationName(int id);
}
