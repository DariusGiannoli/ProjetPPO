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

        public Builder(Builder that) {
            // On recopie uniquement les éléments effectivement utilisés
            this.tuples = Arrays.copyOf(that.tuples, that.size);
            this.size = that.size;
        }

        public boolean isEmpty() {
            return size == 0;
        }

        public Builder clear() {
            size = 0;
            return this;
        }

        public Builder add(long newTuple) {
            // 1. Recherche de la position d'insertion.
            int insertionIndex = 0;
            for (; insertionIndex < size; insertionIndex++) {
                // Si l'élément courant est strictement supérieur à newTuple,
                // c'est la première position où newTuple devrait être inséré.
                if (tuples[insertionIndex] > newTuple) {
                    break;
                }
                // Vérifier si l'élément courant domine ou est égal à newTuple.
                // On compare les critères extraits via PackedCriteria.

                /**Problème ici ds la logique de lexicographique*/
                if (PackedCriteria.arrMins(tuples[insertionIndex]) <= PackedCriteria.arrMins(newTuple)
                        && PackedCriteria.changes(tuples[insertionIndex]) <= PackedCriteria.changes(newTuple)) {
                    // newTuple est dominé ou égal ; l'ajout est abandonné.
                    return this;
                }
            }

            // 2. Comptage des tuples immédiatement suivants (à partir de insertionIndex)
            // qui sont dominés par newTuple.
            int countDominated = 0;
            for (int i = insertionIndex; i < size; i++) {
                if (PackedCriteria.arrMins(newTuple) <= PackedCriteria.arrMins(tuples[i])
                        && PackedCriteria.changes(newTuple) <= PackedCriteria.changes(tuples[i])) {
                    countDominated++;
                } else {
                    break;
                }
            }

            // 3. Redimensionnement du tableau si nécessaire.
            if (size == tuples.length) {
                // Augmentation de la capacité d'environ 1.5 fois + 1.
                tuples = java.util.Arrays.copyOf(tuples, tuples.length + (tuples.length >> 1) + 1);
            }

            // 4. Insertion du nouveau tuple.
            if (countDominated > 0) {
                // Cas : il existe au moins un tuple dominé par newTuple.
                // Remplacer le premier tuple dominé par newTuple.
                tuples[insertionIndex] = newTuple;
                // Décaler les éléments suivants (au-delà du bloc dominé) vers la gauche pour combler le vide.
                int remaining = size - (insertionIndex + countDominated);
                if (remaining > 0) {
                    System.arraycopy(tuples, insertionIndex + countDominated, tuples, insertionIndex + 1, remaining);
                }
                // Mise à jour de la taille : on retire (countDominated - 1) éléments.
                size = size - (countDominated - 1);
            } else {
                // Cas : aucun tuple existant n'est dominé par newTuple.
                // Décaler les éléments à partir de insertionIndex vers la droite pour libérer une place.
                System.arraycopy(tuples, insertionIndex, tuples, insertionIndex + 1, size - insertionIndex);
                // Insérer newTuple à la position d'insertion.
                tuples[insertionIndex] = newTuple;
                size++;
            }

            // 5. Tri de la portion utilisée pour maintenir l'ordre lexicographique.
            java.util.Arrays.sort(tuples, 0, size);
            return this;
        }

        public Builder add(int arrMins, int changes, int payload) {
            long packed = PackedCriteria.pack(arrMins, changes, payload);
            return add(packed);
        }

        public Builder addAll(Builder that) {
            // Utilise la méthode forEach avec une lambda pour ajouter chaque tuple.
            that.forEach(this::add);
            return this;
        }

        public boolean fullyDominates(Builder that, int depMins) {
            // Implémentation à compléter selon les règles de domination incluant l'heure de départ.
            return false;
        }

            public void forEach(java.util.function.LongConsumer action) {
            for (int i = 0; i < size; i++) {
                action.accept(tuples[i]);
            }
        }

        public ParetoFront build() {
            long[] result = java.util.Arrays.copyOf(tuples, size);
            return new ParetoFront(result);
        }

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
