package ch.epfl.rechor.timetable;

/**
 * L'interface Indexed est destinée à être étendue par toutes les entités
 * de l'horaire stockées dans une structure indexée (par exemple, un tableau).
 * Elle définit une méthode permettant d'obtenir le nombre d'éléments.
 *
 *  @author Antoine Lepin (390950)
 *  @author Darius Giannoli (380759)
 */
public interface Indexed {

    /**
     * Retourne le nombre d'éléments dans la donnée indexée.
     * Cette valeur définit la plage d'indices valides (de 0 à size()-1 inclus).
     *
     * @return  la taille = nombre d'éléments
     */
    int size();
}