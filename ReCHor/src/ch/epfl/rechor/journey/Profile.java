package ch.epfl.rechor.journey;

import ch.epfl.rechor.timetable.Connections;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.Trips;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public record Profile(TimeTable timeTable, LocalDate date, int arrStationId, List<ParetoFront> stationFront) {

    public Profile {
        stationFront = List.copyOf(stationFront);
    }

    public Connections connections() {
        return timeTable.connectionsFor(date);
    }

    public Trips trips(){
        return timeTable.tripsFor(date);
    }

    public ParetoFront forStation(int stationId) {
        if(stationId >= stationFront.size()) {
            throw new IndexOutOfBoundsException();
        }else {
            return stationFront.get(stationId);
        }
    }
    public static final class Builder {

        private TimeTable timeTable;
        private LocalDate date;
        private int arrStationId;
        ParetoFront.Builder[] stationsParetoFront;
        ParetoFront.Builder[] tripsParetoFront;

        public Builder(TimeTable timeTable, LocalDate date, int arrStationId) {
            this.arrStationId = arrStationId;
            this.date = date;
            this.timeTable = timeTable;

            stationsParetoFront = new ParetoFront.Builder[timeTable.stations().size()];
            tripsParetoFront = new ParetoFront.Builder[timeTable.tripsFor(date).size()];
        }

        public ParetoFront.Builder forStation(int stationId){
            if(stationId >= stationsParetoFront.length){
                throw new IndexOutOfBoundsException();
            } else {
                return stationsParetoFront[stationId];
            }
        }

        public void setForStation(int stationId, ParetoFront.Builder builder){
            stationsParetoFront[stationId] = builder;
        }

        public void setForTrip(int tripId, ParetoFront.Builder builder){
            tripsParetoFront[tripId] = builder;
        }

        public Profile build(){
            List<ParetoFront> stationFront = new ArrayList<>();
            for (int i = 0; i < stationsParetoFront.length; i++) {
                if(stationsParetoFront[i] == null) {
                    stationFront.add(ParetoFront.EMPTY);
                } else{
                stationFront.add(stationsParetoFront[i].build());
                }
            }
            return new Profile(timeTable, date, arrStationId, stationFront);
        }
    }
}
