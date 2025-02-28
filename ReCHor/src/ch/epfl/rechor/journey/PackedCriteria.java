package ch.epfl.rechor.journey;

import ch.epfl.rechor.Preconditions;

/**
 * Classe utilitaire (non instanciable) permettant de manipuler
 * des critères d'optimisation empaquetés dans un entier 64 bits
 *
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */



//Bits [62..51] : heure de départ (dite complémentée) ou 0 si pas d’heure,
//Bits [50..39] : heure d’arrivée (12bits),
//Bits [38..32] : nombre de changements (7bits),
//Bits [31..0] : charge utile (payload) sur 32bits.

public final class PackedCriteria {


    //Constructeur privé pour empêcher l'instanciation.
    private PackedCriteria() {}

    /**
     * Empaquète une heure d'arrivée (sur 12 bits),
     * un nombre de changements (7 bits) et un payload (32 bits)
     * dans un long, sans heure de départ.
     * Verifie que la minute d'arrivée est comprise entre -240 et 2879, et que les changements sont compris entre 0 et 127, sinon elle envoie un IllegalArgumentException avec la methode checkArgument.
     * @param arrMins  heure d'arrivée en minutes après minuit
     * @param changes  nombre de changements
     * @param payload  payload sur 32 bits
     * @return un long conforme à la structure
     * @throws IllegalArgumentException si arrMins ou changes ne respectent pas les limites
     */
    public static long pack(int arrMins, int changes, int payload){

        // Vérifications de base, nécessaire ?
        Preconditions.checkArgument(arrMins >= -240 && arrMins < 2880);
        Preconditions.checkArgument(changes >= 0 && changes < 128);

        //On translate arrMins => [0..3120)
        int storedArr = arrMins + 240;
        Preconditions.checkArgument(storedArr < 4096);

        long arrField = ((long) storedArr) << 39;
        long changesField = ((long) changes) << 32;
        long payloadField = Integer.toUnsignedLong(payload);

        return arrField | changesField | payloadField;


        //long arrMinsLong = Integer.toUnsignedLong(arrMins);
        //long changesLong = Integer.toUnsignedLong(changes);
        //long payloadLong = Integer.toUnsignedLong(payload);
        //Preconditions.checkArgument((changes >>> 7) == 0 && (arrMins >>> 12) == 0);
        //long pack = (arrMinsLong << 39) | (changesLong << 32) | payloadLong;
        //return pack;

    }

    /**
     * Retourne l'heure d'arrivée réelle (en minutes depuis minuit) stockée dans criteria
     * @param criteria  un entier 64 bits représentant les critères
     * @return  l'heure d'arrivée réelle, en minutes depuis minuit ([-240, 2880))
     */
    public static int arrMins(long criteria){
        long storedArr = (criteria >>> 39) & 4095;
        return (int) (storedArr - 240);
    }

    /**
     *Retourne le nombre de changements stocké dans criteria
     * @param criteria  un entier 64 bits
     * @return  le nombre de changements (entre 0 et 127)
     */
    public static int changes(long criteria){
        return (int) ((criteria >>>32) & 127);
    }

    /**
     * Retourne la charge utile (payload) stockée dans criteria.
     * @param criteria  un entier 64 bits
     * @return  un entier 32 bits représentant le payload
     */
    public static int payload(long criteria){
        return (int) (criteria & 0xFFFFFFFFL);
    }

    /**
     * Indique si une heure de départ est présente dans criteria
     * @param criteria  un entier 64 bits
     * @return  true si l’heure de départ est présente, sinon false
     */
    public static boolean hasDepMins(long criteria){
        long depComplement = (criteria >>> 51) & 0xFFF;
        return depComplement != 0;
    }

    /**
     * Retourne l'heure de départ réelle (en minutes depuis minuit) stockée dans criteria,
     * et verifie que la valeur donnée en argument possède bien une heure de départ, sinon lance une IllegalArgumentException avec la methode checkArgument.
     * @param criteria un entier 64 bits
     * @return  l'heure de départ réelle, en minutes depuis minuit ([-240, 2880))
     */
    public static int depMins(long criteria){
        long depComplement = (criteria >>> 51) & 0xFFF;
        Preconditions.checkArgument(depComplement != 0);
        return (int) (4095 - depComplement - 240);
    }

    //
    //     public static long withoutDepMins(long criteria) {
    //      long newCriteria = criteria & (~((0b111111111111L) << 51));
    //      return newCriteria;
    //     }
    //
    //     public static long withDepMins(long criteria, int depMins1) {
    //      long newCriteria = withoutDepMins(criteria);
    //      newCriteria = newCriteria | (((long) depMins1) << 51);
    //      return newCriteria;
    //     }
    //

    /**
     * Retourne un nouveau critère identique à criteria, mais sans heure de départ
     * @param criteria  un entier 64 bits
     * @return  la valeur 64 bits sans heure de départ
     */
    public static long withoutDepMins(long criteria) {
        return criteria & ~(0xFFFL << 51);
    }

    /**
     * Retourne un nouveau critère identique à criteria, mais avec l'heure de départ
     * fixée à depMins. Verifie que lq minute de départ est comprise entre -240 et 2879, et que la nouvelle minute de départ est antérieure ou égale à la minute d'arrivée, sinon elle lance une IllegalArgumentException avec la methode checkArgument.
     * @param criteria  un entier 64 bits
     * @param depMins   l'heure de départ réelle, en minutes depuis minuit ([-240, 2880))
     * @return  un nouvel entier 64 bits avec l'heure de départ stockée
     */
    public static long withDepMins(long criteria, int depMins) {
        Preconditions.checkArgument(depMins >= -240 && depMins < 2880);
        int depComplement = 4095 - (depMins + 240);
        long cleared = withoutDepMins(criteria);
        return cleared | ((long) depComplement << 51);
    }


     //public static boolean dominatesOrIsEqual(long criteria1, long criteria2){
         //Preconditions.checkArgument(hasDepMins(criteria1) == hasDepMins(criteria2));

         //boolean dominates = (arrMins(criteria1) <= arrMins(criteria2)) & (changes(criteria1) <= changes(criteria2));

        // if(hasDepMins(criteria1)) {
        // dominates = dominates & (depMins(criteria1) <= depMins(criteria2));
         //}
        // return dominates;
    // }


    /**
     * Indique si criteria1 domine ou est égal à criteria2 selon les règles d'optimisation.
     *      //Minimiser l'heure d'arrivée : arrMins1 <= arrMins2
     *     //Minimiser le nombre de changements : changes1 <= changes2
     *     //Maximiser l'heure de départ (si présente) : depMins1 >= depMins2
     *     //Si l'un a une heure de départ et pas l'autre, une exception IllegalArgumentException est levée par la methode checkArgument.
     *
     * @param criteria1  un entier 64 bits
     * @param criteria2  un entier 64 bits
     * @return  true si criteria1 domine ou est égal à criteria2, false sinon
     */
    public static boolean dominatesOrIsEqual(long criteria1, long criteria2){

        //Check
        boolean hasDep1 = hasDepMins(criteria1);
        boolean hasDep2 = hasDepMins(criteria2);
        Preconditions.checkArgument(hasDep1 == hasDep2);

        boolean result = (arrMins(criteria1) <= arrMins(criteria2))
                && (changes(criteria1) <= changes(criteria2));

        if(hasDepMins(criteria1)) {
            result &=  (depMins(criteria1) >= depMins(criteria2));
        }
        return result;
    }

    /**
     * Retourne un nouveau critère identique à criteria avec le nombre de changements incrémenté de 1.
     * Verifie que le nombre de changements apres l'ajout de 1 est inférieur à 128, sinon elle lève une IllegalArgumentException avec la methode checkArgument.
     * @param criteria un entier de 64 bits
     * @return  un entier 64 bits avec le champ "changes" augmenté de 1
     */
    public static long withAdditionalChange(long criteria){
        int changes = changes(criteria) + 1;
        long newCriteria = criteria & ~(0b1111111L << 32);
        return newCriteria | ((long) changes << 32);
    }

    /**
     * Retourne un nouveau critère identique à criteria avec un payload
     * remplacé par payload
     * @param criteria un entier de 64 bits
     * @param payload le nouveau payload (32 bits)
     * @return  un entier 64 bits avec le payload mis à jour
     */
    public static long withPayload(long criteria, int payload){
        long newCriteria = criteria & ~0xffffffffL;
        long payloadLong = Integer.toUnsignedLong(payload);
        return newCriteria | payloadLong;
    }
}













