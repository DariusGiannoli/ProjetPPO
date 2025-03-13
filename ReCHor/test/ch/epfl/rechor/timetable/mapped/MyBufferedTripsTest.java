package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.journey.Vehicle;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MyBufferedTripsTest {

    @Test
    void testBufferedTrips() {

        List<String> stringStationList = new ArrayList<>();
        stringStationList.add("  ");
        stringStationList.add("  ");
        stringStationList.add("M1 Lausanne Flon-Renens VD");
        stringStationList.add("  ");
        stringStationList.add("M2");
        stringStationList.add("RE33");

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

        assertEquals(routes.name(0), "M1 Lausanne Flon-Renens VD");
        assertEquals(routes.name(1), "M2");
        assertEquals(routes.name(2), "RE33");
        assertEquals(routes.size(), 3);
        assertEquals(routes.vehicle(0), Vehicle.METRO);
        assertEquals(routes.vehicle(1), Vehicle.METRO);
        assertEquals(routes.vehicle(2), Vehicle.TRAIN);
        assertEquals(trips.routeId(0), 2);
        assertEquals(trips.routeId(1), 1);
        assertEquals(trips.routeId(2), 0);
        assertEquals(routes.name(trips.routeId(0)), "RE33");
        assertEquals(routes.name(trips.routeId(1)), "M2");
        assertEquals(routes.name(trips.routeId(2)), "M1 Lausanne Flon-Renens VD");
        assertEquals(trips.destination(0), "Coppet");
        assertEquals(trips.destination(1), "Renens VD");
        assertEquals(trips.destination(2), "Scoubydoubydou");
    }
}
