package ch.epfl.rechor.journey;

import ch.epfl.rechor.Preconditions;
import ch.epfl.rechor.journey.PackedCriteria;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.function.LongConsumer;

/**
 * Représente une frontière de Pareto immuable des critères d'optimisation.
 * Les tuples (paires ou triplets de critères) sont empaquetés sous forme de longs dans un tableau trié
 * selon l'ordre lexicographique (basé sur l'heure d'arrivée et le nombre de changements).
 * Cette structure est utilisée pour optimiser la recherche de voyages optimaux.
 * Les instances de ParetoFront sont immuables. La seule façon de les construire est via le bâtisseur
 *
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */
public final class ParetoFront {

    // Constantes de chaînes partagées
    private static final String EMPTY_FRONTIER = "ParetoFront EMPTY";
    private static final String EMPTY_BUILDER = "Builder: ParetoFront EMPTY";
    private static final String PARETO_FRONT_PREFIX = "ParetoFront";
    private static final String BUILDER_PREFIX = "Builder ParetoFront";
    private static final String OPEN_BRACKET_NEWLINE = " [\n";
    private static final String ARR_MINS_PREFIX = "  arrMins=";
    private static final String CHANGES_PREFIX = ", changes=";
    private static final String NEWLINE = "\n";
    private static final String CLOSE_BRACKET = "]";

    // Tableau final contenant les tuples empaquetés de la frontière
    private final long[] tuples;

    //Frontière de Pareto vide
    public static final ParetoFront EMPTY = new ParetoFront(new long[0]);

    /**
     * Constructeur privé recevant un tableau trié de tuples.
     *
     * @param tuples le tableau de tuples empaquetés
     */
    private ParetoFront(long[] tuples) {
        this.tuples = tuples;
    }

    /**
     * Retourne le nombre de tuples présents dans la frontière.
     *
     * @return la taille de la frontière
     */
    public int size() {
        return tuples.length;
    }

    /**
     * Recherche et retourne le tuple dont l'heure d'arrivée et le nombre de changements
     * correspondent aux valeurs données.
     *
     * @param arrMins l'heure d'arrivée réelle (en minutes après minuit)
     * @param changes le nombre de changements
     * @return le tuple empaqueté correspondant
     * @throws NoSuchElementException si aucun tuple ne correspond aux critères
     */
    public long get(int arrMins, int changes) {
        for (long tuple : tuples) {
            if (PackedCriteria.arrMins(tuple) == arrMins && PackedCriteria.changes(tuple) == changes) {
                return tuple;
            }
        }
        throw new NoSuchElementException();
    }

    /**
     * Parcourt tous les tuples de la frontière et applique l'action spécifiée à chacun d'eux.
     *
     * @param action l'action à appliquer à chaque tuple
     */
    public void forEach(LongConsumer action) {
        for (long tuple : tuples) {
            action.accept(tuple);
        }
    }

    /**
     * Retourne une représentation textuelle lisible de la frontière de Pareto,
     * indiquant pour chaque tuple l'heure d'arrivée et le nombre de changements.
     *
     * @return une chaîne représentant la frontière
     */
    @Override
    public String toString() {
        if (tuples.length == 0) {
            return EMPTY_FRONTIER;
        }
        StringBuilder sb = new StringBuilder(PARETO_FRONT_PREFIX + OPEN_BRACKET_NEWLINE);
        for (long tuple : tuples) {
            sb.append(ARR_MINS_PREFIX)
                    .append(PackedCriteria.arrMins(tuple))
                    .append(CHANGES_PREFIX)
                    .append(PackedCriteria.changes(tuple))
                    .append(NEWLINE);
        }
        sb.append(CLOSE_BRACKET);
        return sb.toString();
    }

    /**
     * Bâtisseur de la frontière de Pareto.
     * Permet d'insérer des tuples empaquetés tout en maintenant l'ordre lexicographique
     * et en éliminant les tuples dominés.
     */
    public static final class Builder {

        //Tableau dynamique interne stockant les tuples
        private long[] tuples;
        //Nombre d'éléments effectivement présents dans le tableau
        int size;

        /**
         * Construit un nouveau Builder avec une frontière vide
         */
        public Builder() {
            this.tuples = new long[2];
            this.size = 0;
        }

        /**
         * Constructeur de copie.
         *
         * @param that le Builder à copier
         */
        public Builder(Builder that) {
            this.tuples = Arrays.copyOf(that.tuples, that.size);
            this.size = that.size;
        }

        /**
         * Vérifie si la frontière en cours de construction est vide.
         *
         * @return true si vide et false sinon
         */
        public boolean isEmpty() {
            return size == 0;
        }

        /**
         * Vide la frontière en cours de construction.
         *
         * @return this pour chaîner les appels
         */
        public Builder clear() {
            size = 0;
            return this;
        }

        private int compact(long[] array, long packedTuple, int pos) {
            int dst = 0;
            for (int src = 0; src < size-pos; src += 1) {
                if (PackedCriteria.dominatesOrIsEqual(packedTuple, array[src])) continue;
                if (dst != src) array[dst] = array[src];
                dst += 1;
            }
            return dst;
        }


        /**
         * ajoute à la frontière le tuple de critères empaquetés donné;
         * cet ajout n'est fait que si le nouveau tuple n'est pas dominé ou égal à un de la frontière,
         * et tous les éventuels tuples existants et dominés par le nouveau en sont supprimés.
         * @param packedTuple le nouveau tuple que l'on veut ajouter à la frontière de Pareto.
         * @return lui même car c'est un builder.
         */

        // A vérifier
        public Builder add(long packedTuple) {

            long adjusted = packedTuple & ~0xFFFFFFFFL;


            int pos = 0;
            while (pos < size && tuples[pos] < adjusted) {

                if (PackedCriteria.dominatesOrIsEqual(tuples[pos], packedTuple)) {
                    return this; // ajout annulé
                }
                pos++;
            }

            if (pos < size && PackedCriteria.dominatesOrIsEqual(tuples[pos], packedTuple)) {
                return this; // ajout annulé
            }


            long[] arrayCopy = new long[size - pos];
            System.arraycopy(tuples, pos, arrayCopy, 0, size-pos);
            int notDominated = compact(arrayCopy, packedTuple, pos);
            int dominated = size - pos - notDominated;


            if (dominated > 0) {

                tuples[pos] = packedTuple;

                System.arraycopy(arrayCopy, 0, tuples, pos + 1, notDominated);
                size = pos + notDominated + 1;
            } else {

                if (size == tuples.length) {
                    int newCap = (int) (tuples.length * 1.5);
                    if (newCap == tuples.length) {
                        newCap++; // s'assurer d'une augmentation
                    }
                    long[] newArray = new long[newCap];

                    System.arraycopy(tuples, 0, newArray, 0, pos);

                    newArray[pos] = packedTuple;

                    System.arraycopy(tuples, pos, newArray, pos + 1, size - pos);
                    tuples = newArray;
                } else {

                    System.arraycopy(tuples, pos, tuples, pos + 1, size - pos);
                    tuples[pos] = packedTuple;
                }
                size++;
            }
            return this;
        }


        /**
         * Ajoute un tuple (arrMins, changes, payload) dans la frontière,
         * sans heure de départ.
         * @return this, pour chaînage
         */
        public Builder add(int arrMins, int changes, int payload) {
            long packed = PackedCriteria.pack(arrMins, changes, payload);
            return add(packed);
        }

        /**
         * Ajoute tous les tuples d'un autre Builder 'that' dans le builder courant,
         * en utilisant la logique Pareto (domination).
         * @param that l'autre builder
         * @return this, pour chaînage
         */
        public Builder addAll(Builder that) {
            that.forEach(this::add);
            return this;
        }

        /**
         * Parcourt la frontière en cours de construction et applique action.accept(tuple)
         * pour chacun des tuples.
         * @param action un LongConsumer
         */
        public void forEach(LongConsumer action) {
            for (int i = 0; i < size; i++) {
                action.accept(tuples[i]);
            }
        }

        /**
         * Retourne true si et seulement si tous les tuples de 'that', une fois qu'on leur
         * a fixé l'heure de départ 'depMins', sont dominés par au moins un tuple du builder courant.
         * @param that l'autre builder
         * @param depMins l'heure de départ à fixer
         * @return true si la totalité des tuples de 'that' sont dominés par un tuple du builder courant
         */
        public boolean fullyDominates(Builder that, int depMins) {

            for (int i = 0; i < that.size; i++) {
                long thatTuple = that.tuples[i];
                // Fixer l'heure de départ
                long fixedThat = PackedCriteria.withDepMins(thatTuple, depMins);

                // Cherche un tuple dans 'this' qui domine ou égale 'fixedThat'
                boolean dominatedByAtLeastOne = false;
                for (int j = 0; j < this.size; j++) {

                    if (PackedCriteria.dominatesOrIsEqual(tuples[j], fixedThat)) {
                        dominatedByAtLeastOne = true;
                        break;
                    }
                }
                if (!dominatedByAtLeastOne) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Construit la frontière de Pareto immuable à partir du contenu du builder.
         * Copie les tuples [0..size) pour garantir l'immuabilité.
         * @return un ParetoFront immuable.
         */
        public ParetoFront build() {
            long[] finalArray = Arrays.copyOf(tuples, size);
            return new ParetoFront(finalArray);
        }

        /**
         * @return retourne la frontière de Pareto sous forme de chaine de caractère pour que cela soit plus lisible.
         */
        @Override
        public String toString() {
            if (size == 0) {
                return EMPTY_BUILDER;
            }
            StringBuilder sb = new StringBuilder(BUILDER_PREFIX + OPEN_BRACKET_NEWLINE);
            for (int i = 0; i < size; i++) {
                sb.append(ARR_MINS_PREFIX)
                        .append(PackedCriteria.arrMins(tuples[i]))
                        .append(CHANGES_PREFIX)
                        .append(PackedCriteria.changes(tuples[i]))
                        .append(NEWLINE);
            }
            sb.append(CLOSE_BRACKET);
            return sb.toString();
        }
    }
}
