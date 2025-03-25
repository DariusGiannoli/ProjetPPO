package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;

public class MyFileTimeTableTest {

    @Test
    void TestTimeTable() throws IOException {
        TimeTable table = FileTimeTable.in(Path.of("timetable"));
//        Stations stations = table.stations();
//        for(int i = 0; i < stations.size(); i++) {
//            System.out.println(stations.name(i));
//        }
//        Routes routes = table.routes();
//        for(int i = 0; i < routes.size(); i++) {
//           System.out.println(routes.name(i));
//           }

//        LocalDate date = LocalDate.of(2025, 3, 18);
//        Trips trips = table.tripsFor(date);
//                for(int i = 0; i < trips.size(); i++) {
//           System.out.println(trips.destination(i));
//           }

        StationAliases aliases = table.stationAliases();
        for(int i = 0; i < aliases.size(); i++) {
            System.out.println(aliases.alias(i));
        }

    }
}
