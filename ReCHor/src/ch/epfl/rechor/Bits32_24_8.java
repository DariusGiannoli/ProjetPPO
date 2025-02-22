package ch.epfl.rechor;

/**
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 *  Classe utilitaire (non instanciable) contenant des méthodes
 *  de manipulation de deux entiers empaquetés (24 bits et 8 bits)
 *  dans un entier de 32 bits.
 */
public final class Bits32_24_8 {

    //Constructeur privé pour empêcher l’instanciation
    private Bits32_24_8() {}

    /**
     * Empaquète deux valeurs dans un entier 32 bits
     * @param bits24  la valeur devant tenir sur 24 bits
     * @param bits8   la valeur devant tenir sur 8 bits
     * @return l’entier de 32 bits contenant bits24 et bits8
     * @throws IllegalArgumentException si bits24 ou bits ne tiennent pas dans leur nombre de bits respectif
     */
    public static int pack(int bits24, int bits8) {
        Preconditions.checkArgument((bits8 >>> 8) == 0 && (bits24 >>> 24) == 0);
        return (bits24 << 8) | bits8;
    }

    /**
     * Retourne les 24 bits de poids fort du vecteur de 32 bits donné
     * @param bits32  un entier 32 bits
     * @return  la partie haute (24 bits) de cet entier
     */
    public static int unpack24(int bits32) {
        return bits32 >>> 8;
    }

    /**
     * Retourne les 8 bits de poids faible du vecteur de 32 bits donné
     * @param bits32  un entier 32 bits
     * @return  la partie basse (8 bits) de cet entier
     */
    public static int unpack8(int bits32) {
        return bits32 & 255;
    }
}
