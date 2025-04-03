package ch.epfl.rechor.timetable;

/**
 * L'interface Connections représente les liaisons indexées.
 * Pour l'algorithme de recherche de voyages,
 * les liaisons doivent être ordonnées par heure de départ décroissante.
 *
 *  Toutes les méthodes de cette interface peuvent lever IndexOutOfBoundsException
 *  si l'index fourni est invalide (négatif ou supérieur ou égal à size()).
 *
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */
public interface Connections extends Indexed{

    /**
     * Retourne l'index de l'arrêt de départ de la liaison d'index donné.
     * Cet index représente une gare si inférieur au nombre total de gares,
     * sinon il correspond à (index - nombre de gares) pour une voie/quai.
     *
     * @param id l'index de la liaison
     * @return l'index de l'arrêt de départ
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    int depStopId(int id);

    /**
     * Retourne l'heure de départ de la liaison d'index donné, exprimée en minutes après minuit.
     *
     * @param id l'index de la liaison
     * @return l'heure de départ (en minutes après minuit)
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    int depMins(int id);

    /**
     * Retourne l'index de l'arrêt d'arrivée de la liaison d'index donné.
     * Peut représenter une gare ou une voie/quai selon la valeur.
     *
     * @param id l'index de la liaison
     * @return l'index de l'arrêt d'arrivée
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    int arrStopId(int id);

    /**
     * Retourne l'heure d'arrivée de la liaison d'index donné, exprimée en minutes après minuit.
     *
     * @param id l'index de la liaison
     * @return l'heure d'arrivée (en minutes après minuit)
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    int arrMins(int id);

    /**
     * Retourne l'index de la course à laquelle appartient la liaison d'index donné.
     *
     * @param id l'index de la liaison
     * @return l'index de la course associée
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    int tripId(int id);

    /**
     * Retourne la position de la liaison d'index donné dans la course correspondante.
     * La première liaison d'une course a l'index 0.
     *
     * @param id l'index de la liaison
     * @return la position dans la course
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    int tripPos(int id);

    /**
     * Retourne l'index de la liaison suivante dans la course.
     * Si la liaison est la dernière, retourne l'index de la première liaison de la course.
     *
     * @param id l'index de la liaison
     * @return l'index de la liaison suivante
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    int nextConnectionId(int id);
}
