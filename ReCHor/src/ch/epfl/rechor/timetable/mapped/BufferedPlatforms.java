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

    private static final int NAME_ID = 0;
    private static final int STATION_ID = 1;

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

    @Override
    public String name(int id) {
        int nameIndex = structuredBuffer.getU16(NAME_ID, id);
        return stringTable.get(nameIndex);
    }

    @Override
    public int stationId(int id) {
        return structuredBuffer.getU16(STATION_ID, id);
    }

    @Override
    public int size() {
        return structuredBuffer.size();
    }
}