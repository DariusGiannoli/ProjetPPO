package ch.epfl.rechor.timetable;

/**
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 *
 * L'interface Platforms représente des voies ou quais indexés.
 * Elle étend l'interface Indexed et fournit des méthodes pour accéder
 * aux informations d'une voie ou d'un quai à partir de son index.
 */
public interface Platforms extends Indexed{

    // IL faut lever les IndexOutOfBounds exceptions !!!!!!

    /**
     * Retourne le nom de la voie ou du quai correspondant à l'index donné.
     *
     * @param id l'index de la voie ou du quai
     * @return le nom de la voie ou du quai (peut être vide)
     */
    String name(int id);

    /**
     * Retourne l'index de la gare à laquelle appartient la voie ou le quai d'index donné.
     *
     * @param id l'index de la voie ou du quai
     * @return l'index de la gare associée
     */
    int stationId(int id);
}
