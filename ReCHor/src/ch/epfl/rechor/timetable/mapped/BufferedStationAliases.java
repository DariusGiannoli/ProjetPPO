package ch.epfl.rechor.timetable.mapped;


import ch.epfl.rechor.timetable.StationAliases;

import java.nio.ByteBuffer;
import java.util.List;

public final class BufferedStationAliases implements StationAliases {

    private static final int ALIAS_ID = 0;
    private static final int STATION_NAME_ID = 1;

    private static final Structure ALIAS_STRUCTURE = new Structure(
            Structure.field(ALIAS_ID, Structure.FieldType.U16),
            Structure.field(STATION_NAME_ID, Structure.FieldType.U16)
    );

    private final List<String> stringTable;
    private final StructuredBuffer structuredBuffer;


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
