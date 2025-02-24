package ch.epfl.rechor;


import java.util.NoSuchElementException;

/**
 * Class pour verifier une condition.
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */
public final class Preconditions {
    /**
     * Constructeur de Preconditions, en privé pour qu'on ne puisse pas créer d'instance de
     * cette classe.
     */
    private Preconditions() {
    }

    /**
     * @param shouldBeTrue la condition qui doit être respectée.
     * @throws IllegalArgumentException lancée si la condition donnée en argument est fausse.
     */
    public static void checkArgument(boolean shouldBeTrue){
        if(!shouldBeTrue) {
            throw new IllegalArgumentException();
        }
    }

}
