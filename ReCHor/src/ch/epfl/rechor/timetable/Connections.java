package ch.epfl.rechor.timetable;

/**
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 *
 * L'interface Connections représente des liaisons indexées.
 * Pour les besoins de l'algorithme de recherche de voyages, les liaisons
 * doivent être ordonnées par heure de départ décroissante.
 */
public interface Connections extends Indexed{

    // IL faut lever les IndexOutOfBounds exceptions !!!!!!

    /**
     * Retourne l'index de l'arrêt de départ de la liaison d'index donné.
     * Cet index est une gare s'il est inférieur au nombre total de gares ou une voie/quai si l'index de la voie/quai
     * correspond à (index - nombre de gares).
     *
     * @param id l'index de la liaison
     * @return l'index de l'arrêt de départ
     */
    int depStopId(int id);

    /**
     * Retourne l'heure de départ de la liaison d'index donné, exprimée en minutes après minuit.
     *
     * @param id l'index de la liaison
     * @return l'heure de départ en minutes après minuit
     */
    int depMins(int id);

    /**
     * Retourne l'index de l'arrêt d'arrivée de la liaison d'index donné.
     * <p>
     * Note : cet index peut représenter une gare s'il est inférieur au nombre total de gares, ou une voie/quai si
     * l'index de la voie/quai correspond à (index - nombre de gares).
     *
     * @param id l'index de la liaison
     * @return l'index de l'arrêt d'arrivée
     */
    int arrStopId(int id);

    /**
     * Retourne l'heure d'arrivée de la liaison d'index donné, exprimée en minutes après minuit.
     *
     * @param id l'index de la liaison
     * @return l'heure d'arrivée en minutes après minuit
     */
    int arrMins(int id);

    /**
     * Retourne l'index de la course à laquelle appartient la liaison d'index donné.
     *
     * @param id l'index de la liaison
     * @return l'index de la course associée
     */
    int tripId(int id);

    /**
     * Retourne la position de la liaison d'index donné dans la course à laquelle elle appartient.
     * La première liaison d'une course ayant l'index 0.
     *
     * @param id l'index de la liaison
     * @return la position dans la course
     */
    int tripPos(int id);

    /**
     * Retourne l'index de la liaison suivante dans la course à laquelle appartient la liaison d'index donné.
     * Si la liaison d'index donné est la dernière de la course, retourne l'index de la première liaison de la course.
     *
     * @param id l'index de la liaison
     * @return l'index de la liaison suivante
     */
    int nextConnectionId(int id);

}
