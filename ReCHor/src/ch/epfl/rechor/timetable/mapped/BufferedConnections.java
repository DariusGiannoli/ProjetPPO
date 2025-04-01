package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.Connections;
import ch.epfl.rechor.Bits32_24_8;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * Permet d'accéder à une table de liaisons représentée de manière aplatie.
 * <p>
 * Fournit les informations sur les liaisons (départs, arrivées, cours, positions) ainsi que l'index de la liaison suivante.
 * </p>
 *
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */
public final class BufferedConnections implements Connections {

    // Indices des champs dans la table principale des liaisons
    private static final int DEP_STOP_ID  = 0;
    private static final int DEP_MINUTES  = 1;
    private static final int ARR_STOP_ID  = 2;
    private static final int ARR_MINUTES  = 3;
    private static final int TRIP_POS_ID  = 4;

    // Structure d'une liaison (12 octets)
    private static final Structure CONNECTION_STRUCTURE = new Structure(
            Structure.field(DEP_STOP_ID, Structure.FieldType.U16),
            Structure.field(DEP_MINUTES, Structure.FieldType.U16),
            Structure.field(ARR_STOP_ID, Structure.FieldType.U16),
            Structure.field(ARR_MINUTES, Structure.FieldType.U16),
            Structure.field(TRIP_POS_ID, Structure.FieldType.S32)
    );

    // StructuredBuffer pour la table principale des liaisons
    private final StructuredBuffer structuredBuffer;
    // IntBuffer pour accéder directement aux indices de la liaison suivante
    private final IntBuffer nextBuffer;

    /**
     * Construit une instance de {@code BufferedConnections}.
     *
     * @param buffer     le {@code ByteBuffer} contenant les données aplaties des liaisons.
     * @param succBuffer le {@code ByteBuffer} contenant uniquement les indices des liaisons suivantes.
     */
    public BufferedConnections(ByteBuffer buffer, ByteBuffer succBuffer) {
        this.structuredBuffer = new StructuredBuffer(CONNECTION_STRUCTURE, buffer);
        this.nextBuffer = succBuffer.asIntBuffer();
    }

    /**
     *{@inheritDoc}
     * @param id l'index de la liaison
     * @return
     */
    @Override
    public int depStopId(int id) {
        return structuredBuffer.getU16(DEP_STOP_ID, id);
    }

    /**
     * {@inheritDoc}
     * @param id l'index de la liaison
     * @return
     */
    @Override
    public int depMins(int id) {
        return structuredBuffer.getU16(DEP_MINUTES, id);
    }

    /**
     * {@inheritDoc}
     * @param id l'index de la liaison
     * @return
     */
    @Override
    public int arrStopId(int id) {
        return structuredBuffer.getU16(ARR_STOP_ID, id);
    }

    /**
     * {@inheritDoc}
     * @param id l'index de la liaison
     * @return
     */
    @Override
    public int arrMins(int id) {
        return structuredBuffer.getU16(ARR_MINUTES, id);
    }

    /**
     * {@inheritDoc}
     * @param id l'index de la liaison
     * @return
     */
    @Override
    public int tripId(int id) {
        int tripPosPacked = structuredBuffer.getS32(TRIP_POS_ID, id);
        return Bits32_24_8.unpack24(tripPosPacked);
    }

    /**
     * {@inheritDoc}
     * @param id l'index de la liaison
     * @return
     */
    @Override
    public int tripPos(int id) {
        int tripPosPacked = structuredBuffer.getS32(TRIP_POS_ID, id);
        return Bits32_24_8.unpack8(tripPosPacked);
    }

    /**
     * {@inheritDoc}
     * @param id l'index de la liaison
     * @return
     */
    @Override
    public int nextConnectionId(int id) {
        return nextBuffer.get(id);
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public int size() {
        return structuredBuffer.size();
    }
}