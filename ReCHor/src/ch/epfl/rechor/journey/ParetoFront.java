package ch.epfl.rechor.journey;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.function.LongConsumer;

/**
 * Représente une frontière de Pareto immuable de critères d'optimisation.
 * Les tuples (paires ou triplets de critères) sont empilés sous forme de longueurs
 * dans un tableau trié selon l'ordre lexicographique
 * (basé sur l'heure d'arrivée et le nombre de changements).
 * Cette structure est utilisée pour optimiser la recherche des trajets optimaux.
 * Les instances de ParetoFront sont immuables.
 * La seule façon de les construire est d'utiliser le Builder.
 *
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */
public final class ParetoFront {

    // Shared string constants
    private static final String EMPTY_FRONTIER = "ParetoFront EMPTY";
    private static final String EMPTY_BUILDER = "Builder: ParetoFront EMPTY";
    private static final String PARETO_FRONT_PREFIX = "ParetoFront";
    private static final String BUILDER_PREFIX = "Builder ParetoFront";
    private static final String OPEN_BRACKET_NEWLINE = " [\n";
    private static final String ARR_MINS_PREFIX = "  arrMins=";
    private static final String CHANGES_PREFIX = ", changes=";
    private static final String NEWLINE = "\n";
    private static final String CLOSE_BRACKET = "]";

    // Mask to ignore payload bits (lower 32 bits)
    private static final long PAYLOAD_MASK = ~0xFFFFFFFFL;

    // tableau final contenant les tuples empaquetés de la frontière.
    private final long[] tuples;

    // Frontière de Pareto vide.
    public static final ParetoFront EMPTY = new ParetoFront(new long[0]);

    /**
     * Constructeur privé recevant un tableau de tuples triés.
     *
     * @param tuples le tableau de tuples triés.
     */
    private ParetoFront(long[] tuples) {
        this.tuples = tuples;
    }

    /**
     * Renvoie le nombre de tuples dans la frontière.
     *
     * @return la taille de la frontière
     */
    public int size() {
        return tuples.length;
    }

    /**
     * Recherche et renvoie le tuple avec l'heure d'arrivée et le nombre de changements donnés.
     *
     * @param arrMins l'heure d'arrivée (en minutes après minuit).
     * @param changes le nombre de changements.
     * @return le tuple empaqueté correspondant aux critères
     * @throws NoSuchElementException si aucun tuple ne correspond aux critères.
     */
    public long get(int arrMins, int changes) {
        for (long tuple : tuples) {
            if (PackedCriteria.arrMins(tuple) == arrMins
                    && PackedCriteria.changes(tuple) == changes) {
                return tuple;
            }
        }
        throw new NoSuchElementException();
    }

    /**
     * Itère à travers tous les tuples de la frontière
     * et applique l'action spécifiée à chacun d'entre eux.
     *
     * @param action l'action à appliquer à tous les tuples.
     */
    public void forEach(LongConsumer action) {
        for (long tuple : tuples) {
            action.accept(tuple);
        }
    }

    /**
     * Renvoie une représentation textuelle lisible de la frontière de Pareto,
     * indiquant l'heure d'arrivée et le nombre de changements pour chaque tuple.
     *
     * @return String représentant la frontière.
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
     * Constructeur de la frontière de Pareto.
     * Permet l'insertion de tuples empaquetés tout en maintenant l'ordre lexicographique
     * et en éliminant les tuples dominés.
     */
    public static final class Builder {

        // Capacité initiale du tableau.
        private static final int INITIAL_CAPACITY = 2;
        // Tableau interne qui stock les tuples.
        private long[] tuples;
        // Nombre d'éléments présents dans le tableau.
        private int size;

        /**
         * Construit un nouveau Bâtisseur avec une frontière vide.
         */
        public Builder() {
            this.tuples = new long[INITIAL_CAPACITY];
            this.size = 0;
        }

        /**
         * Copie le bâtisseur.
         *
         * @param that le bâtisseur à copier.
         */
        public Builder(Builder that) {
            this.tuples = Arrays.copyOf(that.tuples, that.size);
            this.size = that.size;
        }

        /**
         * Vérifie si la frontière en construction est vide.
         *
         * @return vrai si elle est vide, sinon faux.
         */
        public boolean isEmpty() {
            return size == 0;
        }

        /**
         * Vide la frontière en cours de construction.
         *
         * @return la frontière en construction pour l'enchainement des méthodes.
         */
        public Builder clear() {
            size = 0;
            return this;
        }

        /**
         * Ajoute le n-tuple empaqueté donné à la frontière ;
         * Cet ajout n'est effectué que si le nouveau n-tuple n'est pas dominé ou égal
         * à un n-tuple dans la frontière,
         * et tous les n-tuples existants dominés par le nouveau n-tuple sont supprimés.
         *
         * @param packedTuple le nouveau tuple à ajouter à la frontière en construction.
         * @return la frontière en construction pour l'enchainement des méthodes.
         */
        public Builder add(long packedTuple) {
            // Masquer les bits de la charge utile pour la comparaison de la position
            long adjusted = packedTuple & PAYLOAD_MASK;

            // 1. Déterminer position d'insertion tout en vérifiant si le nouveau tuple est dominé.
            int pos = findInsertionPosition(packedTuple, adjusted);
            if (pos < 0) {
                // Le tuple est dominé, on ne l'ajoute pas.
                return this;
            }

            // 2. Supprimer les tuples dominés par le nouveau tuple dans la portion [pos, size).
            int newSize = removeDominatedTuples(pos, packedTuple);

            // 3. Assurer la capacité d'accueil et insérer le nouveau tuple.
            ensureCapacity(newSize + 1);
            System.arraycopy(tuples, pos, tuples, pos + 1, newSize - pos);
            tuples[pos] = packedTuple;
            size = newSize + 1;

            return this;
        }

        /**
         * Recherche la position d'insertion pour le nouveau tuple.
         * Si le tuple est déjà dominé par un tuple existant, renvoie -1.
         *
         * @param newTuple le nouveau tuple empaqueté.
         * @param adjusted la valeur du tuple avec la charge utile masquée.
         * @return la position d'insertion ou -1 si le tuple est dominé.
         */
        private int findInsertionPosition(long newTuple, long adjusted) {
            int pos = 0;
            while (pos < size && tuples[pos] < adjusted) {
                if (PackedCriteria.dominatesOrIsEqual(tuples[pos], newTuple)) {
                    return -1; // Le tuple existant domine le nouveau tuple.
                }
                pos++;
            }
            if (pos < size && PackedCriteria.dominatesOrIsEqual(tuples[pos], newTuple)) {
                return -1;
            }
            return pos;
        }

        /**
         * Parcourt la portion de tableau à partir de startPos et élimine les tuples
         * dominés par newTuple.
         *
         * @param startPos la position de départ.
         * @param newTuple le nouveau tuple à insérer.
         * @return la nouvelle taille effective de la portion traitée.
         */
        private int removeDominatedTuples(int startPos, long newTuple) {
            int dst = startPos;
            for (int src = startPos; src < size; src++) {
                if (!PackedCriteria.dominatesOrIsEqual(newTuple, tuples[src])) {
                    if (dst != src) {
                        tuples[dst] = tuples[src];
                    }
                    dst++;
                }
            }
            return dst;
        }

        /**
         * Assure que le tableau interne dispose de la capacité requise et redimensionne si
         * necessaire
         *
         * @param requiredCapacity la capacité minimale requise.
         */
        private void ensureCapacity(int requiredCapacity) {
            if (requiredCapacity > tuples.length) {
                int newCapacity = Math.max(tuples.length * 2, requiredCapacity);
                tuples = Arrays.copyOf(tuples, newCapacity);
            }
        }

        /**
         * Ajoute un tuple (arrMins, changes, payload) à la frontière, sans l'heure de départ.
         *
         * @param arrMins l'heure d'arrivée en minutes après minuit.
         * @param changes le nombre de changements
         * @param payload la valeur de la payload.
         * @return la frontière en construction pour l'enchainement des méthodes.
         */
        public Builder add(int arrMins, int changes, int payload) {
            long criteria = PackedCriteria.pack(arrMins, changes, payload);
            return add(criteria);
        }

        /**
         * Ajoute tous les tuples d'un autre bâtisseur au constructeur actuel,
         * en utilisant la logique de Pareto (domination).
         *
         * @param that l'autre bâtisseur.
         * @return la frontière en construction pour l'enchainement des méthodes.
         */
        public Builder addAll(Builder that) {
            that.forEach(this::add);
            return this;
        }

        /**
         * Itère à travers la frontière en construction et applique l'action pour chaque tuple.
         *
         * @param action a LongConsumer
         */
        public void forEach(LongConsumer action) {
            for (int i = 0; i < size; i++) {
                action.accept(tuples[i]);
            }
        }

        /**
         * Retourne vrai si et seulement si tous les tuples du bâtisseur 'that',
         * une fois fixés avec l'heure de départ 'depMins',
         * sont dominés par au moins un tuple du bâtisseur actuel.
         *
         * @param that    l'autre bâtisseur.
         * @param depMins l'heure de départ en min après minuit que l'on veut ajouter aux tuples
         *                de that.
         * @return vrai si tous les tuples de 'that' sont dominés par un tuple du bâtisseur actuel.
         */
        public boolean fullyDominates(Builder that, int depMins) {

            for (int i = 0; i < that.size; i++) {
                long fixedThat = PackedCriteria.withDepMins(that.tuples[i], depMins);

                // Vérifier si ce tuple est dominé par au moins un tuple de this
                boolean dominated = false;
                for (int j = 0; j < this.size; j++) {
                    if (PackedCriteria.dominatesOrIsEqual(tuples[j], fixedThat)) {
                        dominated = true;
                        break;
                    }
                }

                // Si ce tuple n'est pas dominé, alors this ne domine pas entièrement that
                if (!dominated) {
                    return false;
                }
            }

            // Tous les tuples sont dominés
            return true;
        }

        /**
         * Construit la frontière de Pareto immuable à partir du contenu du bâtisseur.
         *
         * @return une frontière de Pareto immuable.
         */
        public ParetoFront build() {
            if (size == 0) {
                return EMPTY;
            }

            return new ParetoFront(Arrays.copyOf(tuples, size));
        }

        /**
         * Redéfinition de la méthode toString
         *
         * @return renvoie la frontière de Pareto sous forme de chaîne de caractères pour
         * une meilleure lisibilité.
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