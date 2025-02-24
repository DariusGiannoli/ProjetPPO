package ch.epfl.rechor.timetable;

/**
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 *
 * L'interface Trips représente des courses de transport public indexées.
 * Elle étend l'interface Indexed et fournit des méthodes pour accéder aux informations
 * d'une course.
 */
public interface Trips extends Indexed{

    // IL faut lever les IndexOutOfBounds exceptions !!!!!!

    /**
     * Retourne l'index de la ligne à laquelle appartient la course d'index donné.
     *
     * @param id l'index de la course
     * @return l'index de la ligne associée
     */
    int routeId(int id);

    /**
     * Retourne le nom de la destination finale de la course d'index donné.
     *
     * @param id l'index de la course
     * @return le nom de la destination finale
     */
    String destination(int id);
}
