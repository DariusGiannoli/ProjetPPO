package ch.epfl.rechor.timetable.mapped;


import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class MyBufferedConnectionsTest {

    @Test
    void testBufferedConnection() {
        List<String> stringStationList = new ArrayList<>();
        stringStationList.add("  ");
        stringStationList.add("  ");
        stringStationList.add("M1 Lausanne Flon-Renens VD");
        stringStationList.add("  ");
        stringStationList.add("M2");
        stringStationList.add("RE");

        byte[] tableauBytes = {0x00, 0x02, 0x01, 0x00, 0x04, 0x01, 0x00, 0x05, 0x02};
        ByteBuffer buffer = ByteBuffer.wrap(tableauBytes);
        BufferedRoutes routes = new BufferedRoutes(stringStationList, buffer);

        List<String> stringStationList2 = new ArrayList<>();
        stringStationList2.add("  ");
        stringStationList2.add("  ");
        stringStationList2.add("Coppet");
        stringStationList2.add("  ");
        stringStationList2.add("Scoubydoubydou");
        stringStationList2.add("Renens VD");

        byte[] tableauBytes2 = {0x00, 0x02, 0x00, 0x02, 0x00, 0x01, 0x00, 0x05, 0x00, 0x00, 0x00, 0x04};
        ByteBuffer buffer2 = ByteBuffer.wrap(tableauBytes2);
        BufferedTrips trips = new BufferedTrips(stringStationList2, buffer2);

        List<String> stringStationList3 = new ArrayList<>();
        stringStationList2.add("  ");
        stringStationList2.add("  ");
        stringStationList2.add("Zurich");
        stringStationList2.add("  ");
        stringStationList2.add("ScibidiFortnite");
        stringStationList2.add("La Daronne de Steph");
        byte[] tableauBytes3 = {0x00, 0x02, 0x00, 0x02, 0x00, 0x01, 0x00, 0x05, 0x00, 0x00, 0x00, 0x04};

    }
}
