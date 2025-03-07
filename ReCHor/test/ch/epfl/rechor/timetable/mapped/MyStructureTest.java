package ch.epfl.rechor.timetable.mapped;


import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static ch.epfl.rechor.timetable.mapped.Structure.FieldType.U16;
import static ch.epfl.rechor.timetable.mapped.Structure.field;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MyStructureTest {


    @Test
    void testStructureStations() {

        List<String> stringStationList = new ArrayList<>();
        stringStationList.add("  ");
        stringStationList.add("  ");
        stringStationList.add("  ");
        stringStationList.add("  ");
        stringStationList.add("Lausanne");
        stringStationList.add("  ");
        stringStationList.add("Zurich");

        ByteBuffer buffer = ByteBuffer.allocate(100);
        byte[] tableauBytes = {0x00, 0x04, 0x04, (byte) 0xb6, (byte) 0xca, 0x14, 0x21, 0x14, 0x1f, (byte) 0xa1, 0x00, 0x06, 0x04, (byte) 0xdc, (byte) 0xcc, 0x12, 0x21, 0x18, (byte) 0xda, 0x03};
        buffer.put(tableauBytes);


        BufferedStations stations = new BufferedStations(stringStationList, buffer);
        assertEquals(stations.name(0), "Lausanne");
        assertEquals(stations.name(1), "Zurich");
        assertEquals(stations.latitude(0), 46.5167919639498);
        assertEquals(stations.latitude(1), 46.54276396147907);
        assertEquals(stations.longitude(0), 6.629091985523701);
        assertEquals(stations.longitude(1), 6.837874967604876);
        assertEquals(stations.size(), 2);
    }
}
