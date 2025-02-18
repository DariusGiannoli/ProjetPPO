package ch.epfl.rechor.journey;

import java.time.LocalDateTime;
import java.util.List;
import java.time.Duration;
import java.util.Objects;


public record Journey(List<Leg> legs) {

    public Journey {
        if (legs.isEmpty()) {
            throw new IllegalArgumentException("La liste des étapes ne doit pas être vide.");
        }

        // Copie pour l’immuabilité
        legs = List.copyOf(legs);

        for (int i = 0; i < legs.size(); i++) {
            Leg current = legs.get(i);

            // Vérifications pour toutes les étapes sauf la première
            if (i > 0) {
                Leg previous = legs.get(i - 1);

                // 2) Alternance pied/transport
                if ((previous instanceof Leg.Foot && current instanceof Leg.Foot)
                        || (previous instanceof Leg.Transport && current instanceof Leg.Transport)) {
                    throw new IllegalArgumentException(
                            "Deux étapes de même type (pied/transport) se suivent."
                    );
                }

                // 3) L’instant de départ ne précède pas celui d’arrivée de la précédente
                if (current.depTime().isBefore(previous.arrTime())) {
                    throw new IllegalArgumentException(
                            "Heure de départ avant l'heure d'arrivée de l'étape précédente."
                    );
                }

                // 4) L'arrêt de départ est identique à l'arrêt d'arrivée de la précédente
                if (!current.depStop().equals(previous.arrStop())) {
                    throw new IllegalArgumentException(
                            "L'arrêt de départ n'est pas identique à l'arrêt d'arrivée de l'étape précédente."
                    );
                }
            }


        }
    }

    public sealed interface Leg {

        Stop depStop();

        Stop arrStop();

        LocalDateTime depTime();

        LocalDateTime arrTime();

        default Duration duration() {
            return Duration.between(depTime(), arrTime());
        };

        List<IntermediateStop> intermediateStops();

        public record IntermediateStop(Stop stop, LocalDateTime arrTime, LocalDateTime depTime) {

            public IntermediateStop {
                Objects.requireNonNull(stop, "Le stop ne peut pas être null.");

                if (depTime.isBefore(arrTime)) {
                    throw new IllegalArgumentException(
                            "L'heure de départ ne peut pas précéder l'heure d'arrivée."
                    );
                }
            }
        }
        public record Transport(Stop depStop, LocalDateTime depTime,  Stop arrStop, LocalDateTime arrTime, List<IntermediateStop> intermediateStops, Vehicle vehicle, String route, String destination) implements Leg {

            public Transport{
                Objects.requireNonNull(depStop,       "depStop ne peut pas être null.");
                Objects.requireNonNull(depTime,       "depTime ne peut pas être null.");
                Objects.requireNonNull(arrStop,       "arrStop ne peut pas être null.");
                Objects.requireNonNull(arrTime,       "arrTime ne peut pas être null.");
                //Pas nécessaire cf conseils
                //Objects.requireNonNull(intermediateStops, "intermediateStops ne peut pas etre null.");
                Objects.requireNonNull(vehicle,       "vehicle ne peut pas être null.");
                Objects.requireNonNull(route,          "type ne peut pas être null.");
                Objects.requireNonNull(destination,   "destination ne peut pas être null.");

                if (arrTime.isBefore(depTime)) {
                    throw new IllegalArgumentException(
                            "La date/heure d'arrivée ne peut pas précéder la date/heure de départ."
                    );
                }
                intermediateStops = List.copyOf(intermediateStops);

            }

        }

        public record Foot(Stop depStop, LocalDateTime depTime, Stop arrStop, LocalDateTime arrTime) implements Leg{

            public Foot{
                Objects.requireNonNull(depStop, "depStop ne peut pas être null.");
                Objects.requireNonNull(depTime, "depTime ne peut pas être null.");
                Objects.requireNonNull(arrStop, "arrStop ne peut pas être null.");
                Objects.requireNonNull(arrTime, "arrTime ne peut pas être null.");

                if (arrTime.isBefore(depTime)) {
                    throw new IllegalArgumentException(
                            "La date/heure d'arrivée ne peut pas précéder la date/heure de départ."
                    );
                }
            }
            @Override
            public List<IntermediateStop> intermediateStops() {
                return List.of();
            }

            public boolean isTransfer() {
                return depStop.name().equals(arrStop.name());
            }
        }
    }

    public Stop depStop(){
        return legs.getFirst().depStop();
    }

    public Stop arrStop(){
        return legs.getLast().arrStop();
    }

    public LocalDateTime depTime(){
        return legs.getFirst().depTime();
    }

    public LocalDateTime arrTime(){
        return legs.getLast().arrTime();
    }

    public Duration duration() {
        return Duration.between(depTime(), arrTime());
    }


}