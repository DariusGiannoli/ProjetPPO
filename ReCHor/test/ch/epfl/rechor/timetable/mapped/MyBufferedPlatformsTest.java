package ch.epfl.rechor.timetable.mapped;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MyBufferedPlatformsTest {

    @Test
    void testBufferedStationsAlias() {
        List<String> stringStationList = new ArrayList<>();
        stringStationList.add("1");
        stringStationList.add("70");


        byte[] tableauBytes = {00, 00, 00, 00, 00, 01, 00, 00, 00, 00, 00, 01};
        ByteBuffer buffer = ByteBuffer.wrap(tableauBytes);


        BufferedPlatforms stations = new BufferedPlatforms(stringStationList, buffer);
        assertEquals(stations.name(0), "1");
        assertEquals(stations.name(1), "70");
        assertEquals(stations.name(2), "1");
        assertEquals(stations.stationId(0), 0);
        assertEquals(stations.stationId(1), 0);
        assertEquals(stations.stationId(2), 1);
        assertEquals(stations.size(), 3);
    }
}
