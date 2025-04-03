package ch.epfl.rechor.journey;

import ch.epfl.rechor.Preconditions;

/**
 * Classe utilitaire (non instanciable) permettant de manipuler
 * des critères d'optimisation empaquetés dans un entier 64 bits.
 * La représentation sur 64 bits est la suivante :
 * <ul>
 *   <li>Bits [62..51] : heure de départ complémentée (ou 0 si absente)</li>
 *   <li>Bits [50..39] : heure d’arrivée (12 bits)</li>
 *   <li>Bits [38..32] : nombre de changements (7 bits)</li>
 *   <li>Bits [31..0]  : charge utile (32 bits)</li>
 * </ul>
 *
 * Les heures (départ ou arrivée) sont exprimées en minutes depuis minuit,
 * mais traduites pour être toujours positives (ajout de 240 minutes).
 *
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */
public final class PackedCriteria {

    // Constantes pour les heures et vérifications
    private static final int MIN_MINUTES = -240;
    private static final int MAX_MINUTES = 2880; // minutes < 2880
    private static final int MINUTES_OFFSET = 240; // translation pour rendre les minutes positives

    // Déplacements et masques pour le packing
    private static final int SHIFT_DEP = 51;
    private static final int SHIFT_ARR = 39;
    private static final int SHIFT_CHANGES = 32;
    private static final int BITS_12_MASK = 0xFFF;
    private static final long BITS_12_MASK_LONG = 0xFFFL;// 12 bits (4095) long
    private static final int BITS_7_MASK = 0x7F;    // 7 bits (127)
    private static final long BITS_7_MASK_LONG = 0x7FL;    // 7 bits (127) long
    private static final long BITS_32_MASK = 0xFFFFFFFFL; // 32 bits

    /**Constructeur privé pour empêcher l'instanciation.*/
    private PackedCriteria() {}

    /**
     * Empaquète une heure d'arrivée, un nombre de changements et un payload dans un long,
     * sans heure de départ.
     *
     * @param arrMins heure d'arrivée en minutes depuis minuit (entre -240 et 2879)
     * @param changes nombre de changements (entre 0 et 127)
     * @param payload charge utile sur 32 bits
     * @return un long conforme à la structure
     * @throws IllegalArgumentException si arrMins ou changes ne respectent pas les limites
     */
    public static long pack(int arrMins, int changes, int payload){
        Preconditions.checkArgument(arrMins >= MIN_MINUTES && arrMins < MAX_MINUTES);
        Preconditions.checkArgument(changes >= 0 && changes < 128);

        // Translation de l'heure d'arrivée pour obtenir une valeur positive
        int storedArr = arrMins + MINUTES_OFFSET;

        long arrField = ((long) storedArr) << SHIFT_ARR;
        long changesField = ((long) changes) << SHIFT_CHANGES;
        long payloadField = Integer.toUnsignedLong(payload);

        return arrField | changesField | payloadField;
    }

    /**
     * Retourne l'heure d'arrivée réelle (en minutes depuis minuit) stockée dans criteria.
     *
     * @param criteria un entier 64 bits représentant les critères
     * @return l'heure d'arrivée réelle (entre -240 et 2879)
     */
    public static int arrMins(long criteria){
        long storedArr = (criteria >>> SHIFT_ARR) & BITS_12_MASK;
        return (int) (storedArr - MINUTES_OFFSET);
    }

    /**
     * Retourne le nombre de changements stocké dans criteria.
     *
     * @param criteria un entier 64 bits
     * @return le nombre de changements (entre 0 et 127)
     */
    public static int changes(long criteria){
        return (int) ((criteria >>>SHIFT_CHANGES) & BITS_7_MASK);
    }

    /**
     * Retourne la charge utile (payload) stockée dans criteria.
     *
     * @param criteria un entier 64 bits
     * @return un entier 32 bits représentant le payload
     */
    public static int payload(long criteria){
        return (int) (criteria & BITS_32_MASK);
    }

    /**
     * Indique si une heure de départ est présente dans criteria.
     *
     * @param criteria un entier 64 bits
     * @return true si l'heure de départ est présente, false sinon
     */
    public static boolean hasDepMins(long criteria){
        long depComplement = (criteria >>> SHIFT_DEP) & BITS_12_MASK;
        return depComplement != 0;
    }

    /**
     * Retourne l'heure de départ réelle (en minutes depuis minuit) stockée dans criteria.
     *
     * @param criteria un entier 64 bits
     * @return l'heure de départ réelle (entre -240 et 2879)
     * @throws IllegalArgumentException si criteria ne contient pas d'heure de départ
     */
    public static int depMins(long criteria){
        long depComplement = (criteria >>> SHIFT_DEP) & BITS_12_MASK;
        Preconditions.checkArgument(depComplement != 0);

        return (int) (BITS_12_MASK - depComplement - MINUTES_OFFSET);
    }

    /**
     * Retourne un nouveau critère identique à criteria, mais sans heure de départ.
     *
     * @param criteria un entier 64 bits
     * @return un entier 64 bits sans heure de départ
     */
    public static long withoutDepMins(long criteria) {
        return criteria & ~(BITS_12_MASK_LONG << SHIFT_DEP);
    }

    /**
     * Retourne un nouveau critère identique à criteria, mais avec l'heure départ fixée à depMins.
     * Vérifie que depMins est compris entre -240 et 2879 et qu'il est antérieur ou égal à
     * l'heure d'arrivée.
     *
     * @param criteria un entier 64 bits
     * @param depMins l'heure de départ réelle (entre -240 et 2879)
     * @return un nouvel entier 64 bits avec l'heure de départ stockée
     * @throws IllegalArgumentException si depMins n'est pas dans les limites
     * ou est postérieure à l'heure d'arrivée
     */
    public static long withDepMins(long criteria, int depMins) {
        Preconditions.checkArgument(depMins >= MIN_MINUTES && depMins < MAX_MINUTES);

        int depComplement = BITS_12_MASK - (depMins + MINUTES_OFFSET);
        long cleared = withoutDepMins(criteria);

        return cleared | ((long) depComplement << SHIFT_DEP);
    }

    /**
     * Indique si criteria1 domine ou est égal à criteria2 selon les règles d'optimisation.
     * (Minimiser l'heure d'arrivée et le nombre de changements, maximiser l'heure de départ)
     *
     * @param criteria1 un entier 64 bits
     * @param criteria2 un entier 64 bits
     * @return true si criteria1 domine ou est égal à criteria2, false sinon
     * @throws IllegalArgumentException si l'un des critères possède une heure de dep et non l'autre
     */
    public static boolean dominatesOrIsEqual(long criteria1, long criteria2){
        boolean hasDep = hasDepMins(criteria1);
        Preconditions.checkArgument(hasDep == hasDepMins(criteria2));

        return (arrMins(criteria1) <= arrMins(criteria2))
                && (changes(criteria1) <= changes(criteria2))
                && (!hasDep || depMins(criteria1) >= depMins(criteria2));
    }

    /**
     * Retourne un nouveau critère identique à criteria avec le nombre de changements incrémenté
     * de 1
     *
     * @param criteria un entier 64 bits
     * @return un entier 64 bits avec le champ "changes" augmenté de 1
     * @throws IllegalArgumentException si le nombre de changements atteint sa limite
     */
    public static long withAdditionalChange(long criteria){
        int currentChanges = changes(criteria);
        int newChanges = currentChanges + 1;
        long newCriteria = criteria & ~(BITS_7_MASK_LONG << SHIFT_CHANGES);

        return newCriteria | ((long) newChanges << SHIFT_CHANGES);
    }

    /**
     * Retourne un nouveau critère identique à criteria avec un payload remplacé par payload.
     *
     * @param criteria un entier 64 bits
     * @param payload  le nouveau payload (32 bits)
     * @return un entier 64 bits avec le payload mis à jour
     */
    public static long withPayload(long criteria, int payload){
        long newCriteria = criteria & ~BITS_32_MASK;
        long payloadLong = Integer.toUnsignedLong(payload);

        return newCriteria | payloadLong;
    }
}