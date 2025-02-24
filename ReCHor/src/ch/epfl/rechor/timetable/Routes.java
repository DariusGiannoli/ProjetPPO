package ch.epfl.rechor.timetable;

import ch.epfl.rechor.journey.Vehicle;

/**
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 *
 * L'interface Routes représente les lignes de transport public indexées.
 * Elle étend l'interface Indexed et fournit des méthodes pour accéder aux informations
 * d'une ligne de transport.
 */
public interface Routes extends Indexed{

    // IL faut lever les IndexOutOfBounds exceptions !!!!!!

    /**
     * Retourne le type de véhicule desservant la ligne d'index donné.
     *
     * @param id l'index de la ligne
     * @return le type de véhicule associé à la ligne
     */
    Vehicle vehicle(int id);

    /**
     * Retourne le nom de la ligne d'index donné
     *
     * @param id l'index de la ligne
     * @return le nom de la ligne
     */
    String name(int id);
}
