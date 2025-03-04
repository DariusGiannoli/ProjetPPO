package ch.epfl.rechor.timetable.mapped;


import ch.epfl.rechor.timetable.StationAliases;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Class qui permet d'accéder à un tableau de noms alternatifs de gares représentées de manière aplatie, et implémente l'interface StationAliases.
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */
public final class BufferedStationAliases implements StationAliases {

    private static final int ALIAS_ID = 0;
    private static final int STATION_NAME_ID = 1;

    private static final Structure ALIAS_STRUCTURE = new Structure(
            Structure.field(ALIAS_ID, Structure.FieldType.U16),
            Structure.field(STATION_NAME_ID, Structure.FieldType.U16)
    );

    private final List<String> stringTable;
    private final StructuredBuffer structuredBuffer;


    /**
     * Construit une instance donnant accès aux données aplaties disponibles dans le tableau buffer,
     * en utilisant la table de chaînes stringTable pour déterminer la valeur des chaînes référencées par ces données
     * @param stringTable tableau de String référencées par les données de buffer.
     * @param buffer tableau de données aplaties.
     */
    public BufferedStationAliases(List<String> stringTable, ByteBuffer buffer){
        this.stringTable = stringTable;
        this.structuredBuffer = new StructuredBuffer(ALIAS_STRUCTURE, buffer);
    }

    @Override
    public String alias(int id) {
        int aliasIndex = structuredBuffer.getU16(ALIAS_ID, id);
        return stringTable.get(aliasIndex);
    }

    @Override
    public String stationName(int id) {
        int stationNameIndex = structuredBuffer.getU16(STATION_NAME_ID, id);
        return stringTable.get(stationNameIndex);
    }

    @Override
    public int size() {
        return structuredBuffer.size();
    }



}
