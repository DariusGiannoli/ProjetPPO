package ch.epfl.rechor;

/**
 * Classe utilitaire (non instanciable) contenant des méthodes
 * de manipulation d'une paire de valeurs empaquetées (24 bits et 8 bits)
 * dans un entier de 32 bits.
 * Cette classe est utilisée notamment pour empaqueter des intervalles d'entiers.
 *
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */
public final class Bits32_24_8 {

    //Constantes pour les masques
    private static final int EIGHT_BITS = 8;
    private static final int MAX_8_BIT = 0xFF; // 255
    private static final int MAX_24_BIT = 0xFFFFFF;

    /**Constructeur privé pour empêcher l'instanciation.*/
    private Bits32_24_8() {}

    /**
     * Empaquète deux valeurs dans un entier 32 bits.
     *
     * @param bits24 la valeur devant tenir sur 24 bits
     * @param bits8  la valeur devant tenir sur 8 bits
     * @return l’entier 32 bits contenant bits24 et bits8
     * @throws IllegalArgumentException si bits24 dépasse 24 bits ou si bits8 dépasse 8 bits
     */
    public static int pack(int bits24, int bits8) {
        Preconditions.checkArgument((bits8 & ~MAX_8_BIT) == 0
                && (bits24 & ~MAX_24_BIT) == 0);

        return (bits24 << EIGHT_BITS) | bits8;
    }

    /**
     * Retourne les 24 bits de poids fort du vecteur de 32 bits donné.
     *
     * @param bits32 un entier 32 bits
     * @return la partie haute (24 bits) de cet entier
     */
    public static int unpack24(int bits32) {
        return bits32 >>> EIGHT_BITS;
    }

    /**
     * Retourne les 8 bits de poids faible du vecteur de 32 bits donné.
     *
     * @param bits32 un entier 32 bits
     * @return la partie basse (8 bits) de cet entier
     */
    public static int unpack8(int bits32) {
        return bits32 & MAX_8_BIT;
    }
}