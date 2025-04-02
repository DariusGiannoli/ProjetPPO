package ch.epfl.rechor.journey;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.function.LongConsumer;

/**
 * Represents an immutable Pareto frontier of optimization criteria.
 * Tuples (pairs or triplets of criteria) are packed as longs in a sorted array
 * according to lexicographical order (based on arrival time and number of changes).
 * This structure is used to optimize the search for optimal journeys.
 * ParetoFront instances are immutable. The only way to build them is via the Builder.
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

    // Final array containing the packed tuples of the frontier
    private final long[] tuples;

    // Empty Pareto frontier
    public static final ParetoFront EMPTY = new ParetoFront(new long[0]);

    /**
     * Private constructor receiving a sorted array of tuples.
     *
     * @param tuples the array of packed tuples
     */
    private ParetoFront(long[] tuples) {
        this.tuples = tuples;
    }

    /**
     * Returns the number of tuples in the frontier.
     *
     * @return the size of the frontier
     */
    public int size() {
        return tuples.length;
    }

    /**
     * Searches and returns the tuple with the given arrival time and number of changes.
     *
     * @param arrMins the actual arrival time (in minutes after midnight)
     * @param changes the number of changes
     * @return the packed tuple corresponding to the criteria
     * @throws NoSuchElementException if no tuple matches the criteria
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
     * Iterates through all tuples in the frontier and applies the specified action to each.
     *
     * @param action the action to apply to each tuple
     */
    public void forEach(LongConsumer action) {
        for (long tuple : tuples) {
            action.accept(tuple);
        }
    }

    /**
     * Returns a readable text representation of the Pareto frontier,
     * showing arrival time and number of changes for each tuple.
     *
     * @return a string representing the frontier
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
     * Builder for the Pareto frontier.
     * Allows insertion of packed tuples while maintaining lexicographical order
     * and eliminating dominated tuples.
     */
    public static final class Builder {
        // Initial capacity for the array
        private static final int INITIAL_CAPACITY = 2;

        // Internal dynamic array storing the tuples
        private long[] tuples;
        // Number of elements actually present in the array
        private int size;

        /**
         * Constructs a new Builder with an empty frontier
         */
        public Builder() {
            this.tuples = new long[INITIAL_CAPACITY];
            this.size = 0;
        }

        /**
         * Copy constructor.
         *
         * @param that the Builder to copy
         */
        public Builder(Builder that) {
            this.tuples = Arrays.copyOf(that.tuples, that.size);
            this.size = that.size;
        }

        /**
         * Checks if the frontier under construction is empty.
         *
         * @return true if empty, false otherwise
         */
        public boolean isEmpty() {
            return size == 0;
        }

        /**
         * Empties the frontier under construction.
         *
         * @return this for method chaining
         */
        public Builder clear() {
            size = 0;
            return this;
        }

        /**
         * Adds the given packed tuple to the frontier;
         * This addition is only made if the new tuple is not dominated by or equal to one in
         * the frontier, and any existing tuples dominated by the new one are removed.
         *
         * @param packedTuple the new tuple to be added to the Pareto frontier
         * @return this for method chaining
         */
        public Builder add(long packedTuple) {
            // Mask out payload bits for position comparison
            long adjusted = packedTuple & PAYLOAD_MASK;

            // 1. Find position and check if dominated
            int pos = 0;
            while (pos < size && tuples[pos] < adjusted) {
                if (PackedCriteria.dominatesOrIsEqual(tuples[pos], packedTuple)) {
                    return this; // Tuple is dominated, don't add it
                }
                pos++;
            }

            // Check if tuple at insertion position dominates the new one
            if (pos < size && PackedCriteria.dominatesOrIsEqual(tuples[pos], packedTuple)) {
                return this;
            }

            // 2. Handle dominated elements after position
            int dst = pos;
            for (int src = pos; src < size; src++) {
                if (!PackedCriteria.dominatesOrIsEqual(packedTuple, tuples[src])) {
                    if (dst != src) tuples[dst] = tuples[src];
                    dst++;
                }
            }
            int newSize = dst;

            // 3. Ensure capacity for the new element
            if (newSize + 1 > tuples.length) {
                int newCapacity = Math.max(tuples.length + 1, (int)(tuples.length * 1.5));
                long[] newArray = new long[newCapacity];
                System.arraycopy(tuples, 0, newArray, 0, pos);
                System.arraycopy(tuples, pos, newArray, pos + 1, newSize - pos);
                tuples = newArray;
            } else {
                // Make space for new element
                System.arraycopy(tuples, pos, tuples, pos + 1, newSize - pos);
            }

            // 4. Insert new tuple and update size
            tuples[pos] = packedTuple;
            size = newSize + 1;

            return this;
        }

        /**
         * Adds a tuple (arrMins, changes, payload) to the frontier,
         * without departure time.
         *
         * @param arrMins the arrival time in minutes after midnight
         * @param changes the number of changes
         * @param payload the payload value
         * @return this for method chaining
         */
        public Builder add(int arrMins, int changes, int payload) {
            return add(PackedCriteria.pack(arrMins, changes, payload));
        }

        /**
         * Adds all tuples from another Builder 'that' to the current builder,
         * using Pareto logic (domination).
         *
         * @param that the other builder
         * @return this for method chaining
         */
        public Builder addAll(Builder that) {
            that.forEach(this::add);
            return this;
        }

        /**
         * Iterates through the frontier under construction and applies action.accept(tuple)
         * for each tuple.
         *
         * @param action a LongConsumer
         */
        public void forEach(LongConsumer action) {
            for (int i = 0; i < size; i++) {
                action.accept(tuples[i]);
            }
        }

        /**
         * Returns true if and only if all tuples of 'that', once fixed with the departure time
         * 'depMins',are dominated by at least one tuple of the current builder.
         *
         * @param that the other builder
         * @param depMins the departure time to fix
         * @return true if all tuples of 'that' are dominated by a tuple of the current builder
         */
        public boolean fullyDominates(Builder that, int depMins) {
            if (that.isEmpty()) return true;
            if (this.isEmpty()) return false;

            for (int i = 0; i < that.size; i++) {
                long fixedThat = PackedCriteria.withDepMins(that.tuples[i], depMins);
                boolean dominated = false;

                for (int j = 0; j < this.size; j++) {
                    if (PackedCriteria.dominatesOrIsEqual(tuples[j], fixedThat)) {
                        dominated = true;
                        break;
                    }
                }

                if (!dominated) return false;
            }
            return true;
        }

        /**
         * Builds the immutable Pareto frontier from the builder content.
         *
         * @return an immutable ParetoFront
         */
        public ParetoFront build() {
            if (size == 0) return EMPTY;
            return new ParetoFront(Arrays.copyOf(tuples, size));
        }

        /**
         * @return returns the Pareto frontier as a string for better readability
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