package ch.epfl.rechor.timetable;

import ch.epfl.rechor.journey.Vehicle;

/**
 * L'interface Routes représente les lignes de transport public indexées.
 * Ele étend l'interface Indexed et fournit des méthodes pour accéder aux informations
 * d'une ligne de transport.
 *
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */
public interface Routes extends Indexed{

    /**
     * Retourne le type de véhicule desservant la ligne d'index donné.
     *
     * @param id l'index de la ligne
     * @return le type de véhicule associé
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    Vehicle vehicle(int id);

    /**
     * Retourne le nom de la ligne d'index donné.
     *
     * @param id l'index de la ligne
     * @return le nom de la ligne
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    String name(int id);
}