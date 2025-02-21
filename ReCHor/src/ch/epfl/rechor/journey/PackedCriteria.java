package ch.epfl.rechor.journey;

import ch.epfl.rechor.Preconditions;

public class PackedCriteria {

    private PackedCriteria() {}

    public static long pack(int arrMins, int changes, int payload){
        Preconditions.checkArgument((changes >> 7) == 0 && (arrMins >> 12) == 0);
        long pack = (((long) arrMins) << 39) + (((long) changes) << 32) + payload;
        return pack;
    }

    public static boolean hasDepMins(long criteria){
        long hasDep = criteria >>> 51;
        return hasDep > 0;
    }

    public static int depMins(long criteria){
        long dep = criteria >>> 51;
        Preconditions.checkArgument(dep > 0);

        return (int) (-dep + 4095 - 240);
    }

    public static int arrMins(long criteria){
        long arr = (criteria >>> 39) & 4095;

        return (int) (arr - 240);
    }

    public static int changes(long criteria){
        long change = (criteria >>> 32) & 127;

        return (int) change;
    }

    public static int payload(long criteria){
        long payload = criteria & 0xffffffffL;
        return (int) payload;
    }

    public static boolean dominatesOrIsEqual(long criteria1, long criteria2){
        Preconditions.checkArgument(hasDepMins(criteria1) == hasDepMins(criteria2));

        boolean dominates = (arrMins(criteria1) <= arrMins(criteria2)) & (changes(criteria1) <= changes(criteria2));

        if(hasDepMins(criteria1)) {
            dominates = dominates & (depMins(criteria1) <= depMins(criteria2));
        }
        return dominates;
    }

    public static long withoutDepMins(long criteria){
        long newCriteria = criteria & (~((0b111111111111L) << 51));
        return newCriteria;
    }

    public static long withDepMins(long criteria, int depMins1){
        long newCriteria = withoutDepMins(criteria);
        newCriteria = newCriteria | (((long) depMins1) << 51);
        return newCriteria;
    }

    public static long withAdditionalChange(long criteria){
        int changes = changes(criteria) + 1;
        long newCriteria = criteria & (~((0b1111111L) << 32));
        newCriteria = newCriteria | (((long) changes) << 32);

        return newCriteria;
    }

    public static long withPayload(long criteria, int payload1){
        long newCriteria = criteria & (~0xffffffffL);
        newCriteria = newCriteria | ((long) payload1);

        return newCriteria;
    }


}
