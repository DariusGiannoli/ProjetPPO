package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.Connections;
import ch.epfl.rechor.Bits32_24_8;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * La classe BufferedConnections permet d'accéder à une table de liaisons représentée de manière aplatie.
 * Elle implémente l'interface Connections et se charge de fournir les informations sur les liaisons
 * (départs, arrivées, cours, positions) ainsi que l'index de la liaison suivante via une table auxiliaire.
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

    // Définition de la structure d'une liaison (12 octets au total)
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
     * Construit une instance de BufferedConnections.
     *
     * @param buffer     le ByteBuffer contenant les données aplaties des liaisons.
     * @param succBuffer le ByteBuffer contenant uniquement les indices des liaisons suivantes.
     */
    public BufferedConnections(ByteBuffer buffer, ByteBuffer succBuffer) {
        this.structuredBuffer = new StructuredBuffer(CONNECTION_STRUCTURE, buffer);
        // Transformation du ByteBuffer en IntBuffer pour accéder aux valeurs entières directement
        this.nextBuffer = succBuffer.asIntBuffer();
    }

    /**
     * Retourne l'index de l'arrêt de départ pour la liaison d'index donné.
     *
     * @param id l'index de la liaison
     * @return l'index de l'arrêt de départ (U16)
     */
    @Override
    public int depStopId(int id) {
        return structuredBuffer.getU16(DEP_STOP_ID, id);
    }

    /**
     * Retourne l'heure de départ (en minutes après minuit) pour la liaison d'index donné.
     *
     * @param id l'index de la liaison
     * @return l'heure de départ (U16)
     */
    @Override
    public int depMins(int id) {
        return structuredBuffer.getU16(DEP_MINUTES, id);
    }

    /**
     * Retourne l'index de l'arrêt d'arrivée pour la liaison d'index donné.
     *
     * @param id l'index de la liaison
     * @return l'index de l'arrêt d'arrivée (U16)
     */
    @Override
    public int arrStopId(int id) {
        return structuredBuffer.getU16(ARR_STOP_ID, id);
    }

    /**
     * Retourne l'heure d'arrivée (en minutes après minuit) pour la liaison d'index donné.
     *
     * @param id l'index de la liaison
     * @return l'heure d'arrivée (U16)
     */
    @Override
    public int arrMins(int id) {
        return structuredBuffer.getU16(ARR_MINUTES, id);
    }

    /**
     * Retourne l'index de la course (trip) à laquelle appartient la liaison d'index donné.
     * Cet index est extrait des 24 bits de poids fort du champ TRIP_POS_ID.
     *
     * @param id l'index de la liaison
     * @return l'index de la course
     */
    @Override
    public int tripId(int id) {
        int tripPosPacked = structuredBuffer.getS32(TRIP_POS_ID, id);
        return Bits32_24_8.unpack24(tripPosPacked);
    }

    /**
     * Retourne la position de la liaison dans la course, extraite des 8 bits de poids faible du champ TRIP_POS_ID.
     *
     * @param id l'index de la liaison
     * @return la position dans la course
     */
    @Override
    public int tripPos(int id) {
        int tripPosPacked = structuredBuffer.getS32(TRIP_POS_ID, id);
        return Bits32_24_8.unpack8(tripPosPacked);
    }

    /**
     * Retourne l'index de la liaison suivante dans la course pour la liaison d'index donné.
     * La table auxiliaire (succBuffer) est utilisée via son IntBuffer associé.
     *
     * @param id l'index de la liaison
     * @return l'index de la liaison suivante
     */
    @Override
    public int nextConnectionId(int id) {
        return nextBuffer.get(id);
    }

    /**
     * Retourne le nombre total de liaisons contenues dans le buffer.
     *
     * @return le nombre d'enregistrements de liaisons
     */
    @Override
    public int size() {
        return structuredBuffer.size();
    }
}
