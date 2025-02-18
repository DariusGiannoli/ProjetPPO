package ch.epfl.rechor;

/*
 *	Author:      Antoine Lepin
 *	Date:
 */

public final class Preconditions {
    private Preconditions() {
    }

    public static void checkArgument(boolean shouldBeTrue){
        if(!shouldBeTrue) {
            throw new IllegalArgumentException();
        }
    }
}
