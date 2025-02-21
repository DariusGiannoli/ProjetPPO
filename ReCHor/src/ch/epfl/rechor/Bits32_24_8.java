package ch.epfl.rechor;


public class Bits32_24_8 {

    private Bits32_24_8() {}

    public static int pack(int bits24, int bits8) {
        Preconditions.checkArgument((bits8 >> 8) == 0 && (bits24 >> 24) == 0);
        int bits32 = (bits24 << 8) + bits8;

        return bits32;
    }

    public static int unpack24(int bits32) {
        int bits24 = bits32 >>> 8;
        return bits24;
    }

    public static int unpack8(int bits32) {
        int bits8 = bits32 & 255;
        return bits8;
    }

}
