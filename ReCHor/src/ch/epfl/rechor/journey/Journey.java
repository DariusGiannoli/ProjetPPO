package ch.epfl.rechor.journey;

import java.time.LocalDateTime;
import java.util.List;
import java.time.Duration;
import java.util.Objects;

import static ch.epfl.rechor.Preconditions.checkArgument;


/**
 * Journey est un voyage.
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 * @param legs la liste des étapes du voyage.
 */
public record Journey(List<Leg> legs) {

    /**Constructeur de Journey. Il vérifie que la liste legs n'est pas vide, que les étapes à pied alternent avec celles en transport, que pour toutes les étapes(sauf la première), l'instant
     * de départ ne précède pas celui d'arrivée de la précédente, et que pour toutes les étapes(sauf la première), l'arrêt de départ est identique à l'arrêt d'arrivée de la précédente.
     * Si une de ces conditions n'est pas respectée, elle lance une exception IllegalArgumentException.
     * @param legs la liste des étapes du voyage.
     */
    public Journey {
        checkArgument(!(legs.isEmpty()));

        // Copie pour l’immuabilité
        legs = List.copyOf(legs);

        for (int i = 0; i < legs.size(); i++) {
            Leg current = legs.get(i);

            // Vérifications pour toutes les étapes sauf la première
            if (i > 0) {
                Leg previous = legs.get(i - 1);

                // 2) Alternance pied/transport
                checkArgument(!((previous instanceof Leg.Foot && current instanceof Leg.Foot)
                        || (previous instanceof Leg.Transport && current instanceof Leg.Transport)));

                // 3) L’instant de départ ne précède pas celui d’arrivée de la précédente
                checkArgument(!(current.depTime().isBefore(previous.arrTime())));

                // 4) L'arrêt de départ est identique à l'arrêt d'arrivée de la précédente
                checkArgument(current.depStop().equals(previous.arrStop()));
            }


        }
    }

    /**
     * Représente une étape du voyage.
     */
    public sealed interface Leg {

        /**
         * @return retourne l'arrêt de départ de l'étape.
         */
        Stop depStop();

        /**
         * @return retourne l'arrêt d'arrivée de l'étape.
         */
        Stop arrStop();

        /**
         * @return retourne la date/heure de départ de l'étape.
         */
        LocalDateTime depTime();

        /**
         * @return retourne la date/heure d'arrivée de l'étape.
         */
        LocalDateTime arrTime();

        /**
         * @return retourne la durée de l'étape.
         */
        default Duration duration() {
            return Duration.between(depTime(), arrTime());
        }

        /**
         * @return retourne la liste des arrêts intermédiaires de l'étape.
         */
        List<IntermediateStop> intermediateStops();

        /**
         * Est un arrêt intermédiaire d'une étape.
         * @param stop l'arrêt intermédiaire en question.
         * @param arrTime la date/heure d'arrivée à l'arrêt.
         * @param depTime la date/heure de départ de l'arrêt.
         */
        public record IntermediateStop(Stop stop, LocalDateTime arrTime, LocalDateTime depTime) {

            /**
             * Constructeur, qui vérifie que stop n'est pas null et que la date/heure de départ n'est pas antérieure à celle d'arrivée.
             */
            public IntermediateStop {
                Objects.requireNonNull(stop, "Le stop ne peut pas être null.");

                checkArgument(!(depTime.isBefore(arrTime)));
            }
        }

        /**
         * Transport est une étape effectuée en transport public.
         * @param depStop l'arrêt de départ de l'étape.
         * @param depTime la date/heure de départ de l'étape.
         * @param arrStop l'arrêt d'arrivée de l'étape.
         * @param arrTime la date/heure d'arrivée de l'étape.
         * @param intermediateStops les éventuels arrêts intermédiaires de l'étape.
         * @param vehicle le type de véhicule utilisé pour cette étape.
         * @param route le nom de la ligne sur laquelle circule le véhicule.
         * @param destination le nom de la destination finale du véhicule utilisé pour cette étape.
         */
        public record Transport(Stop depStop, LocalDateTime depTime,  Stop arrStop, LocalDateTime arrTime, List<IntermediateStop> intermediateStops, Vehicle vehicle, String route, String destination) implements Leg {

            /**
             *Le constructeur, qui vérifie avec requireNonNull qu'aucun des arguments ne soit null (sauf pour intermediateStops), sinon une NullPointerException est lancée par la methode requireNonNull.
             * Il vérifie aussi que la date/heure d'arrivée n'est pas antérieure à celle de départ, sinon il lance une IllegalArgumentException.
             */
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

                checkArgument(!(arrTime.isBefore(depTime)));
                intermediateStops = List.copyOf(intermediateStops);

            }

        }

        /**
         * Foot est une étape effectuée à pied.
         * @param depStop l'arrêt de départ de l'étape.
         * @param depTime la date/heure de départ de l'étape.
         * @param arrStop l'arrêt d'arrivée de l'étape.
         * @param arrTime la date/heure d'arrivée de l'étape.
         */
        public record Foot(Stop depStop, LocalDateTime depTime, Stop arrStop, LocalDateTime arrTime) implements Leg{

            /**
             * Le constructeur de Foot, qui vérifie avec requireNonNull qu'aucun des arguments ne soit null, sinon une NullPointerException est lancée par la methode requireNonNull.
             * Il vérifie aussi que la date/heure d'arrivée n'est pas antérieure à celle de départ, sinon il lance une IllegalArgumentException.
             */
            public Foot{
                Objects.requireNonNull(depStop, "depStop ne peut pas être null.");
                Objects.requireNonNull(depTime, "depTime ne peut pas être null.");
                Objects.requireNonNull(arrStop, "arrStop ne peut pas être null.");
                Objects.requireNonNull(arrTime, "arrTime ne peut pas être null.");

                checkArgument(!(arrTime.isBefore(depTime)));
            }

            /**
             * @return retourne une liste vide, car une étape à pied ne comporte jamais d'arrêts intermédiaires.
             */
            @Override
            public List<IntermediateStop> intermediateStops() {
                return List.of();
            }

            /**
             * @return retourne vrai si et seulement si l'étape est un changement au sein de la même gare,
             * donc vérifie si le nom de l'arrêt de départ est le même que celui d'arrivée.
             */
            public boolean isTransfer() {
                return depStop.name().equals(arrStop.name());
            }
        }
    }

    /**
     * @return retourne l'arrêt de départ du voyage.
     */
    public Stop depStop(){
        return legs.getFirst().depStop();
    }

    /**
     * @return retourne l'arrêt d'arrivée du voyage.
     */
    public Stop arrStop(){
        return legs.getLast().arrStop();
    }

    /**
     * @return retourne la date/heure de début du voyage.
     */
    public LocalDateTime depTime(){
        return legs.getFirst().depTime();
    }

    /**
     * @return retourne la date/heure de fin du voyage.
     */
    public LocalDateTime arrTime(){
        return legs.getLast().arrTime();
    }

    /**
     * @return retourne la durée totale du voyage.
     */
    public Duration duration() {
        return Duration.between(depTime(), arrTime());
    }


}