package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.journey.ParetoFront;
import ch.epfl.rechor.journey.Profile;
import ch.epfl.rechor.timetable.*;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MyFileTimeTableTest {

    @Test
    void TestTimeTable() throws IOException {
        TimeTable table = FileTimeTable.in(Path.of("ReChor/timetable"));
        Stations stations = table.stations();
        int j = 0;
        for(int i = 0; i < stations.size(); i++) {
            System.out.println(stations.name(i));
            j++;
            if(stations.name(i).equals("Ecublens VD, EPFL (bus)")) {
                System.out.println(j);
            }
        }
//        Routes routes = table.routes();
//        for(int i = 0; i < routes.size(); i++) {
//           System.out.println(routes.name(i));
//           }

//        LocalDate date = LocalDate.of(2025, 3, 18);
//        Trips trips = table.tripsFor(date);
//                for(int i = 0; i < trips.size(); i++) {
//           System.out.println(trips.destination(i));
//           }
//
//        StationAliases aliases = table.stationAliases();
//        for(int i = 0; i < aliases.size(); i++) {
//            System.out.println(aliases.alias(i));
//        }

//        LocalDate date = LocalDate.of(2025, 3, 18);
//        Transfers transfers = table.transfers();
//        for(int i = 0; i < transfers.size(); i++) {
//            System.out.println(transfers.arrivingAt(7872));
//        }

    }

    @Test
    void someGeneralKnowledge() throws IOException {
        // this is just a sanity check, ie do the name make sense ??
        TimeTable t = FileTimeTable.in(Path.of("ReChor/timetable"));
        Stations stations = t.stations();
        StationAliases stationAliases = t.stationAliases();
        for (int i = 0; i < stationAliases.size(); i++) {
            String name = stationAliases.stationName(i);
            String alias = stationAliases.alias(i);
            System.out.printf("%s, AKA %s%n", name, alias);
        }


        for (int i = 0; i < stations.size(); i++) {
            String name = stations.name(i);
            System.out.printf("%s\n", name);
        }
    }

    private static Path directory = Path.of("ReChor/timetable2");

    Profile readProfile(TimeTable timeTable,
                        LocalDate date,
                        int arrStationId) throws IOException {
        Path path =
                Path.of("profile_" + date + "_" + arrStationId + ".txt");
        try (BufferedReader r = Files.newBufferedReader(path)) {
            Profile.Builder profileB =
                    new Profile.Builder(timeTable, date, arrStationId);
            int stationId = -1;
            String line;
            while ((line = r.readLine()) != null) {
                stationId += 1;
                if (line.isEmpty()) continue;
                ParetoFront.Builder frontB = new ParetoFront.Builder();
                for (String t : line.split(","))
                    frontB.add(Long.parseLong(t, 16));
                profileB.setForStation(stationId, frontB);
            }
            return profileB.build();
        }

    }
    @Test
    void FortniteTestsTimeTable_CE() {
        try  {
            TimeTable timeTable = FileTimeTable.in(directory);
            timeTable.stations().name(222);
            //222 = 01DE -> 8C4D
            assertEquals("Adliswil, Krone" , timeTable.stations().name(222));
            assertEquals(47.31308097951114, timeTable.stations().latitude(222));
            assertEquals(8.527644937857985, timeTable.stations().longitude(222));
        } catch(IOException e) {
            System.out.println(" la D");
        }
    }

    @Test
    void croustiTest_CE() {

        try  {
            TimeTable timeTable = FileTimeTable.in(directory);
            Trips trips =  timeTable.tripsFor(LocalDate.of(2025, 3, 29));
            assertEquals("Interlaken Ost" , trips.destination(222));
            assertEquals( 2535 , trips.routeId(222));

        } catch(IOException e) {
            System.out.println(" pipi und kaki in pipi kakaland");
        }
    }




}
