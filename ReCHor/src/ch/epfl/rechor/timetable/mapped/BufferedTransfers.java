package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.Transfers;
import ch.epfl.rechor.PackedRange;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * Permet d'accéder à une table de changements représentée de manière aplatie.
 * <p>
 * Pré-calcule une table associant, pour chaque gare d'arrivée,
 * l'intervalle empaqueté des index des changements.
 * </p>
 *
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */
public final class BufferedTransfers implements Transfers {

    //Indices des champs dans la table principale des changements
    private static final int DEP_STATION_ID = 0; // U16 index de la gare de départ
    private static final int ARR_STATION_ID = 1; // U16 index de la gare d'arrivée
    private static final int TRANSFER_MINUTES = 2; // U8 durée du changement

    // Structure d'un changement (5 octets)
    private static final Structure TRANSFER_STRUCTURE = new Structure(
            Structure.field(DEP_STATION_ID, Structure.FieldType.U16),
            Structure.field(ARR_STATION_ID, Structure.FieldType.U16),
            Structure.field(TRANSFER_MINUTES, Structure.FieldType.U8)
    );

    // StructuredBuffer pour accéder aux données des changements
    private final StructuredBuffer structuredBuffer;
    // Tableau pré-calculé associant, pour chaque gare d'arrivée,
    // l'intervalle empaqueté des index des changements
    private final int[] arrivingAtTable;

    /**
     * Construit une instance de {@code BufferedTransfers} à partir d'un {@code ByteBuffer}
     * contenant les données aplaties des changements.
     * <p>
     * Le constructeur effectue deux passes sur les données :
     * <ul>
     *   <li>La première passe détermine le maximum d'index de gare d'arrivée pour dimensionner
     *   le tableau.</li>
     *   <li>La deuxième passe regroupe les enregistrements par gare d'arrivée et stocke,
     *   pour chaque gare, l'intervalle empaqueté des index des changements correspondants.</li>
     * </ul>
     * </p>
     *
     * @param buffer le {@code ByteBuffer} contenant les données aplaties des changements.
     */
    public BufferedTransfers(ByteBuffer buffer) {
        this.structuredBuffer = new StructuredBuffer(TRANSFER_STRUCTURE, buffer);
        this.arrivingAtTable = buildArrivingAtTable();
    }

    /**
     * Construit le tableau associant à chaque gare d'arrivée l'intervalle empaqueté
     * des index des changements correspondants.
     *
     * @return un tableau d'entiers où chaque indice correspond à un identifiant de gare d'arrivée,
     *         et la valeur est un intervalle empaqueté via {@code PackedRange}.
     */
    private int[] buildArrivingAtTable() {
        int numChanges = structuredBuffer.size();

        // Déterminer le maximum d'index de gare d'arrivée pour dimensionner le tableau
        int maxArrStation = -1;
        for (int i = 0; i < numChanges; i++) {
            int arrStation = structuredBuffer.getU16(ARR_STATION_ID, i);
            if (arrStation > maxArrStation) {
                maxArrStation = arrStation;
            }
        }

        // Créer le tableau pour couvrir tous les indices de gare (de 0 à maxArrStation inclus)
        int[] table = new int[maxArrStation + 1];
        // Initialiser chaque entrée avec un intervalle vide (convention : PackedRange.pack(0, 0))
        Arrays.fill(table, PackedRange.pack(0, 0));

        // Parcourir le buffer et regrouper les enregistrements par gare d'arrivée
        int i = 0;
        while (i < numChanges) {
            int currentArrStation = structuredBuffer.getU16(ARR_STATION_ID, i);
            int startIndex = i;
            // Avancer tant que l'index de la gare d'arrivée reste le même
            while (i < numChanges && structuredBuffer.getU16(ARR_STATION_ID, i)
                    == currentArrStation) {
                i++;
            }
            int endIndex = i; // Intervalle [startIndex, endIndex)
            table[currentArrStation] = PackedRange.pack(startIndex, endIndex);
        }
        return table;
    }

    /**
     * {@inheritDoc}
     *
     * @param id l'index du changement
     * @return l'index de la gare de départ pour ce changement
     */
    @Override
    public int depStationId(int id) {
        return structuredBuffer.getU16(DEP_STATION_ID, id);
    }

    /**
     * {@inheritDoc}
     *
     * @param id l'index du changement
     * @return la durée du changement en minutes
     */
    @Override
    public int minutes(int id) {
        return structuredBuffer.getU8(TRANSFER_MINUTES, id);
    }

    /**
     * {@inheritDoc}
     *
     * @param stationId l'index de la gare d'arrivée
     * @return l'intervalle empaqueté des index des changements pour cette gare
     * @throws IndexOutOfBoundsException si l'index de la gare n'est pas valide
     */
    @Override
    public int arrivingAt(int stationId) {
        if (stationId < 0 || stationId >= arrivingAtTable.length) {
            throw new IndexOutOfBoundsException();
        }

        return arrivingAtTable[stationId];
    }

    /**
     * {@inheritDoc}
     *
     * @param depStationId l'index de la gare de départ
     * @param arrStationId l'index de la gare d'arrivée
     * @return la durée du changement en minutes
     * @throws NoSuchElementException si aucun changement correspondant n'est trouvé
     */
    @Override
    public int minutesBetween(int depStationId, int arrStationId) {
        int interval = arrivingAt(arrStationId);
        int start = PackedRange.startInclusive(interval);
        int end = PackedRange.endExclusive(interval);

        for (int i = start; i < end; i++) {
            if (structuredBuffer.getU16(DEP_STATION_ID, i) == depStationId) {
                return structuredBuffer.getU8(TRANSFER_MINUTES, i);
            }
        }
        throw new NoSuchElementException();
    }

    /**
     * {@inheritDoc}
     *
     * @return le nombre total de changements contenus dans le buffer
     */
    @Override
    public int size() {
        return structuredBuffer.size();
    }
}