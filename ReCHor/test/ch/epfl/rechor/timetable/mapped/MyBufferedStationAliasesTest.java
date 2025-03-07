package ch.epfl.rechor.timetable.mapped;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MyBufferedStationAliasesTest {


    @Test
    void testBufferedStationsAlias() {
        List<String> stringStationList = new ArrayList<>();
        stringStationList.add("  ");
        stringStationList.add("  ");
        stringStationList.add("Anet");
        stringStationList.add("Ins");
        stringStationList.add("Lausanne");
        stringStationList.add("Losanna");


        byte[] tableauBytes = {00, 05, 00, 04, 00, 02, 00, 03};
        ByteBuffer buffer = ByteBuffer.wrap(tableauBytes);


        BufferedStationAliases stations = new BufferedStationAliases(stringStationList, buffer);
        assertEquals(stations.stationName(0), "Lausanne");
        assertEquals(stations.stationName(1), "Ins");
        assertEquals(stations.alias(0), "Losanna");
        assertEquals(stations.alias(1), "Anet");
        assertEquals(stations.size(), 2);
    }
}
