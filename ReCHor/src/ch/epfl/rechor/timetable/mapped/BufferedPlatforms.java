package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.Platforms;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Permet d'accéder à un tableau de voies ou quais représentée de manière aplatie, et implémente l'interface Platforms.
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
     * Construit une instance donnant accès aux données aplaties disponibles dans le tableau buffer,
     * en utilisant la table de chaînes stringTable pour déterminer la valeur des chaînes référencées par ces données.
     * @param stringTable tableau de String référencées par les données de buffer.
     * @param buffer tableau de données aplaties.
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
