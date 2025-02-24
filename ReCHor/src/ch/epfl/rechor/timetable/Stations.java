package ch.epfl.rechor.timetable;

import ch.epfl.rechor.Preconditions;

/**
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 *
 * L'interface Stations représente les gares indexées.
 * Elle étend l'interface Indexed et fournit des méthodes pour accéder aux informations
 * d'une gare donnée par son index.
 *
 */
public interface Stations extends Indexed {

    // IL faut lever les IndexOutOfBounds exceptions !!!!!!

    /**
     * Retourne le nom de la gare correspondant à l'index donné.
     *
     * @param id l'index de la gare
     * @return le nom de la gare
     */
    String name(int id);

    /**
     * Retourne la longitude de la gare correspondant à l'index donné, en degrés.
     *
     * @param id l'index de la gare
     * @return la longitude en degrés
     */
    double longitude(int id);

    /**
     * Retourne la latitude de la gare correspondant à l'index donné, en degrés.
     *
     * @param id l'index de la gare
     * @return la latitude en degrés
     */
    double latitude(int id);
}
