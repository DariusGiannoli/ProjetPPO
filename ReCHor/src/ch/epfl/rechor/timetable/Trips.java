package ch.epfl.rechor.timetable;

/**
 * L'interface Trips représente les courses de transport public indexées.
 * Elle étend l'interface Indexed et fournit des méthodes pour accéder aux informations
 * d'une course.
 *
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */
public interface Trips extends Indexed{

    /**
     * Retourne l'index de la ligne associée à la course d'index donné.
     *
     * @param id l'index de la course
     * @return l'index de la ligne
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    int routeId(int id);

    /**
     * Retourne le nom de la destination finale de la course d'index donné.
     *
     * @param id l'index de la course
     * @return le nom de la destination finale
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    String destination(int id);
}
