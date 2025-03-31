package ch.epfl.rechor;

/**
 * Classe utilitaire pour vérifier les préconditions des méthodes.
 * Cette classe est non instanciable et ne sert qu’à contenir des méthodes statiques.
 *
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */
public final class Preconditions {

    //Constructeur privé pour empêcher l'instanciation.
    private Preconditions() {}

    /**
     * Vérifie que la condition donnée est vraie.
     *
     * @param shouldBeTrue la condition à vérifier
     * @throws IllegalArgumentException si la condition est fausse
     */
    public static void checkArgument(boolean shouldBeTrue){
        if(!shouldBeTrue) {
            throw new IllegalArgumentException();
        }
    }
}
