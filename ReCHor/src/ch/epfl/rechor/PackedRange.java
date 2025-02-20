package ch.epfl.rechor;



public class PackedRange {

    private PackedRange() {
    }

    public static int pack(int startInclusive, int endExclusive){

        int length = endExclusive - startInclusive;
        int inter = Bits32_24_8.pack(startInclusive, length);
        return inter;
    }

    public static int length(int interval){
        int length = Bits32_24_8.unpack8(interval);

        return length;
    }

    public static int startInclusive(int interval){
        int start = Bits32_24_8.unpack24(interval);

        return start;
    }

    public static int endExclusive(int interval){
        int length = Bits32_24_8.unpack8(interval);
        int start = Bits32_24_8.unpack24(interval);

        int end = start + length;

        return end;
    }
}
