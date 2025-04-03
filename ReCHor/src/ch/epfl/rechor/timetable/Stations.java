package ch.epfl.rechor.timetable;

/**
 * L'interface Stations représente les gares indexées.
 * Elle étend l'interface Indexed et fournit des méthodes pour accéder
 * aux informations d'une gare identifiée par son index.
 *
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */
public interface Stations extends Indexed {

    /**
     * Retourne le nom de la gare correspondant à l'index donné.
     *
     * @param id l'index de la gare
     * @return le nom de la gare
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    String name(int id);

    /**
     * Retourne la longitude (en degrés) de la gare correspondant à l'index donné.
     *
     * @param id l'index de la gare
     * @return la longitude en degrés
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    double longitude(int id);

    /**
     * Retourne la latitude (en degrés) de la gare correspondant à l'index donné.
     *
     * @param id l'index de la gare
     * @return la latitude en degrés
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    double latitude(int id);
}