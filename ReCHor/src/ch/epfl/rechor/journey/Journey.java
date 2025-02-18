package ch.epfl.rechor.journey;

import java.util.List;

public record Journey(List<Leg> legs) {
    public interface Leg {
        public record IntermediateStop(){

        }
        public record Transport(){

        }public record Foot(){

        }
    }
    public Journey {

    }
}
