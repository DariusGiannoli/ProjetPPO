package ch.epfl.rechor;

/**
 * Classe utilitaire (non instanciable) permettant de manipuler des intervalles
 * d'entiers empaquetés dans un int. La borne inférieure occupe 24 bits et la longueur
 * de l'intervalle occupe 8 bits.
 * Cette représentation compacte est utilisée pour optimiser la recherche de voyages.
 *
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */

public final class PackedRange {

    //Constructeur privé pour empêcher l’instanciation.
    private PackedRange() {}

    /**
     * Empaquète l'intervalle d'entiers dans un int.
     *
     * @param startInclusive borne inférieure (incluse)
     * @param endExclusive   borne supérieure (exclue)
     * @return l'entier 32 bits représentant l'intervalle empaqueté
     */
    public static int pack(int startInclusive, int endExclusive){
        int length = endExclusive - startInclusive;
        return Bits32_24_8.pack(startInclusive, length);
    }

    /**
     * Retourne la longueur de l'intervalle empaqueté.
     *
     * @param interval l'intervalle empaqueté
     * @return la longueur de l'intervalle
     */
    public static int length(int interval){
        return Bits32_24_8.unpack8(interval);
    }

    /**
     * Retourne la borne inférieure (incluse) de l'intervalle empaqueté.
     *
     * @param interval l'intervalle empaqueté
     * @return la borne inférieure, incluse
     */
    public static int startInclusive(int interval){
        return Bits32_24_8.unpack24(interval);
    }

    /**
     * Retourne la borne supérieure (exclue) de l'intervalle empaqueté.
     *
     * @param interval l'intervalle empaqueté
     * @return la borne supérieure, exclue
     */
    public static int endExclusive(int interval){
        return startInclusive(interval) + length(interval);
    }
}