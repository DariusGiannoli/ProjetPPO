package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.Stations;

import java.nio.ByteBuffer;
import java.util.List;

public final class BufferedStations implements Stations {

    private static final int NAME_ID = 0;
    private static final int LON      = 1;
    private static final int LAT      = 2;

    private static final Structure STATION_STRUCTURE = new Structure(
            Structure.field(NAME_ID, Structure.FieldType.U16),
            Structure.field(LON, Structure.FieldType.S32),
            Structure.field(LAT, Structure.FieldType.S32)
    );

    private static final double UNIT_TO_DEGREES = Math.scalb(360.0, -32);

    private final List<String> stringTable;
    private final StructuredBuffer structuredBuffer;

    public BufferedStations(List<String> stringTable, ByteBuffer buffer){
        this.stringTable = stringTable;
        this.structuredBuffer = new StructuredBuffer(STATION_STRUCTURE, buffer);
    }

    @Override
    public String name(int id) {
        int nameIndex = structuredBuffer.getU16(NAME_ID, id);
        return stringTable.get(nameIndex);
    }

    @Override
    public double longitude(int id) {
        int rawLongitude = structuredBuffer.getS32(LON, id);
        return rawLongitude * UNIT_TO_DEGREES;
    }

    @Override
    public double latitude(int id) {
        int rawLatitude = structuredBuffer.getS32(LAT, id);
        return rawLatitude * UNIT_TO_DEGREES;
    }

    @Override
    public int size() {
        return structuredBuffer.size();
    }


}
