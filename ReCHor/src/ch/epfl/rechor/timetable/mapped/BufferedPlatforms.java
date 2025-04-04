package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.Platforms;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Fournit un accès aux voies ou quais stockées de manière aplatie.
 * Implémente l'interface Platforms.
 * Chaque enregistrement comporte deux champs (U16) :
 * <ul>
 *   <li>Index de chaîne pour le nom</li>
 *   <li>Index de la gare parente</li>
 * </ul>
 *
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */
public final class BufferedPlatforms implements Platforms {

    // Indices des champs dans la table principale des voies/quais
    private static final int NAME_ID = 0; //U16, Index de chaîne du nom de la voie/quai
    private static final int STATION_ID = 1; //U16, Index de la gare parente

    private static final Structure PLATFORM_STRUCTURE = new Structure(
            Structure.field(NAME_ID, Structure.FieldType.U16),
            Structure.field(STATION_ID, Structure.FieldType.U16)
    );

    private final List<String> stringTable;
    private final StructuredBuffer structuredBuffer;

    /**
     * Construit une instance d'accès aux voies/quais.
     *
     * @param stringTable table des chaînes de caractères
     * @param buffer      tampon contenant les données aplaties
     */
    public BufferedPlatforms(List<String> stringTable, ByteBuffer buffer) {
        this.stringTable = stringTable;
        this.structuredBuffer = new StructuredBuffer(PLATFORM_STRUCTURE, buffer);
    }

    /**
     * {@inheritDoc}
     *
     * @param id l'index de la voie ou du quai
     * @return le nom de la voie ou du quai
     */
    @Override
    public String name(int id) {
        int nameIndex = structuredBuffer.getU16(NAME_ID, id);
        return stringTable.get(nameIndex);
    }

    /**
     * {@inheritDoc}
     *
     * @param id l'index de la voie ou du quai
     * @return l'indice de la gare parente
     */
    @Override
    public int stationId(int id) {
        return structuredBuffer.getU16(STATION_ID, id);
    }

    /**
     * {@inheritDoc}
     *
     * @return le nombre d'enregistrements
     */
    @Override
    public int size() {
        return structuredBuffer.size();
    }
}