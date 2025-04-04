package ch.epfl.rechor.timetable;

/**
 * L'interface Platforms représente les voies ou quais indexés.
 * Elle étend l'interface Indexed et fournit des méthodes pour accéder aux informations
 * d'une voie ou d'un quai à partir de son index.
 *
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */
public interface Platforms extends Indexed {

    /**
     * Retourne le nom de la voie ou du quai correspondant à l'index donné.
     *
     * @param id l'index de la voie ou du quai
     * @return le nom de la voie ou du quai (peut être une chaîne vide).
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    String name(int id);

    /**
     * Retourne l'index de la gare à laquelle appartient
     * la voie ou le quai identifié par l'index donné.
     *
     * @param id l'index de la voie ou du quai
     * @return l'index de la gare associée
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    int stationId(int id);
}