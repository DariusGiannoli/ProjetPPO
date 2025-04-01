package ch.epfl.rechor.journey;

import ch.epfl.rechor.Bits32_24_8;
import ch.epfl.rechor.timetable.Connections;
import ch.epfl.rechor.timetable.Stations;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.Trips;
import ch.epfl.rechor.timetable.mapped.FileTimeTable;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MyProfileTest {
    Path path = Path.of("ReChor/timetable");

    Profile readProfile(TimeTable timeTable,
                        LocalDate date,
                        int arrStationId) throws IOException {

        Path path =
                Path.of("ReChor","profile_" + date + "_" + arrStationId + ".txt");
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
    void connectionsTest() throws IOException {
        TimeTable table = FileTimeTable.in(path);
        LocalDate date = LocalDate.of(2025, 3, 18);
       Profile profile = readProfile(table, date, 11486);

       ParetoFront front = profile.forStation(0);
       int i = Bits32_24_8.unpack24(PackedCriteria.payload(front.get(22*60+59,4)));
       int i2 = Bits32_24_8.unpack8(PackedCriteria.payload(front.get(22*60+59,4)));
//       System.out.println(i);
//       System.out.println(i2);

       Trips trips = profile.trips();
       for(int j = 0; j < trips.size(); j++) {
           System.out.println(trips.destination(j));
       }

    }

    @Test
    void tripsTest() throws IOException {
        TimeTable t = FileTimeTable.in(Path.of("ReChor/timetable"));
        LocalDate date = LocalDate.of(2025, Month.MARCH, 18);
        Profile p = readProfile(t, date, 11486);
        List<Journey> js = JourneyExtractor.journeys(p, 7872);

        for(int i = 0; i < js.size(); i++) {
            String j = JourneyIcalConverter.toIcalendar(js.get(i));
            System.out.println(j);
        }
//        String j = JourneyIcalConverter.toIcalendar(js.get(32));
//        System.out.println(j);

    }

    @Test
    void forStationTest() throws IOException {
        TimeTable t = FileTimeTable.in(Path.of("ReChor/timetable"));
        LocalDate date = LocalDate.of(2025, Month.MARCH, 18);
        Profile p = readProfile(t, date, 11486);
        List<Journey> js = JourneyExtractor.journeys(p, 7874);
        String j = JourneyIcalConverter.toIcalendar(js.get(32));
        System.out.println(j);
    }

    @Test
    void barbatruc() throws IOException {


        TimeTable t = FileTimeTable.in(Path.of("ReChor/timetable"));
        Profile.Builder profileB = new Profile.Builder(t, LocalDate.of(2025, 3, 18), 7874);
        ParetoFront.Builder frontB = new ParetoFront.Builder();
        frontB.add(PackedCriteria.withDepMins(PackedCriteria.pack(1488, 0, Bits32_24_8.pack(5328, 0)), 1486));
        profileB.setForStation(7860, frontB);
        Profile p = profileB.build();
        List<Journey> js = JourneyExtractor.journeys(p, 7860);
        String j = JourneyIcalConverter.toIcalendar(js.get(0));
        System.out.println(j);
    }


        @Test
    void timeTableTest() {
    }

    @Test
    void dateTest() {
    }

    @Test
    void arrStationIdTest() {
    }

    @Test
    void stationFrontTest() {
    }
}