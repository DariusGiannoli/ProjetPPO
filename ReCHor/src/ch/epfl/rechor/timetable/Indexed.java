package ch.epfl.rechor.timetable;

/**
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 *
 * L'interface Indexed est destinée à être étendue par toutes les entités
 * de l'horaire qui sont stockées dans une structure indexée (typiquement un tableau).
 * Elle définit une méthode permettant d'obtenir le nombre d'éléments.
 */
public interface Indexed {

    /**
     * Retourne le nombre d'éléments dans la donnée indexée.
     * @return  la taille = nombre d'éléments
     */
    int size();
}
