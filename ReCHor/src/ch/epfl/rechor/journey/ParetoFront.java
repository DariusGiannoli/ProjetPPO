package ch.epfl.rechor.journey;

import ch.epfl.rechor.Preconditions;
import ch.epfl.rechor.journey.PackedCriteria;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.function.LongConsumer;

/**
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 *
 * Représentation immuable de la frontière de Pareto des critères d'optimisation. Les tuples de critères
 * sont empaquetés dans un tableau de type long[]
 */
public final class ParetoFront {

    // Tableau privé contenant les tuples empaquetés.
    private final long[] tuples;

    /**
     * Instance publique, statique et finale représentant une frontière de Pareto vide.
     */
    public static final ParetoFront EMPTY = new ParetoFront(new long[0]);

    /**
     * Constructeur privé recevant un tableau de tuples empaquetés.
     *
     * @param tuples le tableau de tuples empaquetés
     */
    private ParetoFront(long[] tuples) {
        this.tuples = tuples;
    }

    /**
     * Retourne le nombre de tuples présents dans la frontière.
     *
     * @return le nombre de tuples
     */
    public int size() {
        return tuples.length;
    }

    /**
     * Retourne le tuple empaqueté dont l'heure d'arrivée et le nombre de changements
     * correspondent aux valeurs données.
     *
     * @param arrMins l'heure d'arrivée réelle, en minutes après minuit
     * @param changes le nombre de changements
     * @return le tuple empaqueté correspondant aux critères
     * @throws NoSuchElementException si aucun tuple ne correspond aux critères
     */
    public long get(int arrMins, int changes) {
        long foundTuple = 0;
        boolean found = false;

        for (long tuple : tuples) {
            if (PackedCriteria.arrMins(tuple) == arrMins
                    && PackedCriteria.changes(tuple) == changes) {
                foundTuple = tuple;
                found = true;
                break;
            }
        }

        if (!found) {
            throw new NoSuchElementException();
        }

        return foundTuple;
    }

    /**
     * Parcourt tous les tuples de la frontière et applique l'action donnée à chacun d'eux.
     *
     * @param action l'action à appliquer à chaque tuple (un LongConsumer)
     */
    public void forEach(LongConsumer action) {
        for (long tuple : tuples) {
            action.accept(tuple);
        }
    }

    /**
     * Retourne une représentation textuelle lisible de la frontière de Pareto.
     * Chaque tuple est affiché avec son heure d'arrivée et son nombre de changements.
     *
     * @return une chaîne représentant la frontière
     */
    @Override
    public String toString() {
        if (tuples.length == 0) {
            return "ParetoFront EMPTY";
        }
        StringBuilder sb = new StringBuilder("ParetoFront [\n");
        for (long tuple : tuples) {
            sb.append("  arrMins=")
                    .append(PackedCriteria.arrMins(tuple))
                    .append(", changes=")
                    .append(PackedCriteria.changes(tuple))
                    .append("\n");
        }
        sb.append("]");
        return sb.toString();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * @author Antoine Lepin (390950)
     * @author Darius Giannoli (380759)
     *
     * Bâtisseur de la frontière de Pareto.
     */
    public static final class Builder {

        private long[] tuples;

        int size;

        /**
         * Constructeur public qui retourne un bâtisseur dont la frontière
         * en cours de construction est vide.
         */
        public Builder() {
            this.tuples = new long[2];
            this.size = 0;
        }


        /**
         * retourne un nouveau bâtisseur avec les mêmes attributs que celui reçu en argument (constructeur de copie).
         * @param that est un builder que l'on copie.
         */
        public Builder(Builder that) {
            // On recopie uniquement les éléments effectivement utilisés
            this.tuples = Arrays.copyOf(that.tuples, that.size);
            this.size = that.size;
        }

        /**
         * @return retourne vrai si et seulement si la frontière en cours de construction est vide.
         */
        public boolean isEmpty() {
            return size == 0;
        }

        /**
         * vide la frontière en cours de construction en supprimant tous ses éléments.
         * @return lui même car c'est un builder.
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


//            int startDominated = pos;
//            while (startDominated < size
//                    && PackedCriteria.dominatesOrIsEqual(packedTuple, tuples[startDominated])) {
//                startDominated++;
//            }

            long[] arrayCopy = new long[size - pos];
            System.arraycopy(tuples, pos, arrayCopy, 0, size-pos);
            int notDominated = compact(arrayCopy, packedTuple, pos);
            int dominated = size - pos - notDominated;


            if (dominated > 0) {

                tuples[pos] = packedTuple;

                //int nbToShift = size - startDominated;
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
                return "Builder: ParetoFront EMPTY";
            }
            StringBuilder sb = new StringBuilder("Builder ParetoFront [\n");
            for (int i = 0; i < size; i++) {
                sb.append("  arrMins=")
                        .append(PackedCriteria.arrMins(tuples[i]))
                        .append(", changes=")
                        .append(PackedCriteria.changes(tuples[i]))
                        .append("\n");
            }
            sb.append("]");
            return sb.toString();
        }
    }
}
