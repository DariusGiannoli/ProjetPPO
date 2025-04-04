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
     * Construit une instance de {@code BufferedTransfers}.
     * <p>
     * Parcourt deux fois le buffer afin de construire la table associant
     * à chaque gare d'arrivée l'intervalle des changements.
     * </p>
     *
     * @param buffer le {@code ByteBuffer} contenant les données aplaties des changements.
     */
    public BufferedTransfers(ByteBuffer buffer) {
        this.structuredBuffer = new StructuredBuffer(TRANSFER_STRUCTURE, buffer);
        int numChanges = structuredBuffer.size();

        // Première passe : déterminer le maximum d'ARR_STATION_ID pour dimensionner la table
        int maxArrStation = -1;
        for (int i = 0; i < numChanges; i++) {
            int arrStation = structuredBuffer.getU16(ARR_STATION_ID, i);
            if (arrStation > maxArrStation) {
                maxArrStation = arrStation;
            }
        }

        // Créer la table avec une taille couvrant tous les indices de gares
        // (0 à maxArrStation inclus)
        arrivingAtTable = new int[maxArrStation + 1];
        // Initialiser la table avec des intervalles vides (convention : PackedRange.pack(0, 0))
        Arrays.fill(arrivingAtTable, PackedRange.pack(0, 0));

        // Deuxième passe : regrouper les enregistrements pour chaque gare d'arrivée.
        int i = 0;
        while (i < numChanges) {
            int currentArrStation = structuredBuffer.getU16(ARR_STATION_ID, i);
            int startIndex = i;
            while (i < numChanges && structuredBuffer.getU16(ARR_STATION_ID, i)
                    == currentArrStation) {
                i++;
            }
            int endIndex = i; // intervalle [startIndex, endIndex)
            arrivingAtTable[currentArrStation] = PackedRange.pack(startIndex, endIndex);
        }
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