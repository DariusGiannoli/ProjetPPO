package ch.epfl.rechor.journey;

import static ch.epfl.rechor.Preconditions.checkArgument;

import java.time.LocalDateTime;
import java.util.List;
import java.time.Duration;
import java.util.Objects;

/**
 * Représente un voyage composé d’une liste d’étapes (legs).
 * Le voyage est valide si :
 * <ul>
 *   <li>la liste d’étapes n’est pas vide</li>
 *   <li>les étapes à pied et en transport public alternent,</li>
 *   <li>pour chaque étape (sauf la première), l’instant de départ n’est pas antérieur à celui
 *   d’arrivée de l’étape précédente,</li>
 *   <li>pour chaque étape (sauf la première), l’arrêt de départ est identique à l’arrêt d’arrivée
 *   de l’étape précédente.</li>
 * </ul>
 *
 * @param legs la liste des étapes du voyage.
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */
public record Journey(List<Leg> legs) {

    /**
     * Constructeur compact de Journey.
     * Copie la liste des étapes afin d’assurer l’immuabilité et vérifie l’ensemble des
     * préconditions.
     *
     * @param legs la liste des étapes du voyage.
     * @throws IllegalArgumentException si les conditions de validité ne sont pas respectées.
     */
    public Journey {
        checkArgument(!legs.isEmpty());

        // Copie pour l’immuabilité
        legs = List.copyOf(legs);

        // Vérification des conditions de validité pour les étapes consécutives
        for (int i = 1; i < legs.size(); i++) {
            Leg current = legs.get(i);
            Leg previous = legs.get(i - 1);

            // Vérifie l'alternance pied/transport
            checkArgument(((previous instanceof Leg.Foot) ^ (current instanceof Leg.Foot)));

            // Vérifie que l'instant de départ ne précède pas celui d'arrivée de la précédente
            checkArgument(!current.depTime().isBefore(previous.arrTime()));

            // Vérifie que l'arrêt de départ est identique à l'arrêt d'arrivée de la précédente
            checkArgument(current.depStop().equals(previous.arrStop()));
        }
    }

    /**
     * Retourne l’arrêt de départ du voyage, correspondant à celui de la première étape.
     *
     * @return l'arrêt de départ.
     */
    public Stop depStop() {
        return legs.getFirst().depStop();
    }

    /**
     * Retourne l’arrêt d’arrivée du voyage, correspondant à celui de la dernière étape.
     *
     * @return l'arrêt d’arrivée.
     */
    public Stop arrStop() {
        return legs.getLast().arrStop();
    }

    /**
     * Retourne la date/heure de départ du voyage, correspondant à celle de la première étape.
     *
     * @return la date/heure de départ.
     */
    public LocalDateTime depTime() {
        return legs.getFirst().depTime();
    }

    /**
     * Retourne la date/heure d’arrivée du voyage, correspondant à celle de la dernière étape.
     *
     * @return la date/heure d’arrivée.
     */
    public LocalDateTime arrTime() {
        return legs.getLast().arrTime();
    }

    /**
     * Retourne la durée totale du voyage.
     *
     * @return la durée totale.
     */
    public Duration duration() {
        return Duration.between(depTime(), arrTime());
    }

    /**
     * Interface scellée représentant une étape du voyage.
     * Seules les étapes à pied et en transport public (Foot et Transport) peuvent l’implémenter.
     */
    public sealed interface Leg {

        /**
         * Retourne l’arrêt de départ de l’étape.
         *
         * @return l'arrêt de départ.
         */
        Stop depStop();

        /**
         * Retourne l’arrêt d’arrivée de l’étape.
         *
         * @return l'arrêt d’arrivée.
         */
        Stop arrStop();

        /**
         * Retourne la date/heure de départ de l’étape.
         *
         * @return la date/heure de départ.
         */
        LocalDateTime depTime();

        /**
         * Retourne la date/heure d’arrivée de l’étape.
         *
         * @return la date/heure d’arrivée.
         */
        LocalDateTime arrTime();

        /**
         * Retourne la durée de l’étape.
         *
         * @return la durée de l’étape.
         */
        default Duration duration() {
            return Duration.between(depTime(), arrTime());
        }

        /**
         * Retourne la liste des arrêts intermédiaires de l’étape.
         *
         * @return la liste des arrêts intermédiaires.
         */
        List<IntermediateStop> intermediateStops();

        /**
         * Méthode pour effectuer des vérifications afin d'éviter redondance + lever exception
         *
         * @param depStop l’arrêt de départ.
         * @param depTime la date/heure de départ.
         * @param arrStop l’arrêt d’arrivée.
         * @param arrTime la date/heure d’arrivée.
         * @throws NullPointerException     si des paramètres sont nuls
         * @throws IllegalArgumentException si la date/heure d'arrivée est avant date/heure départ
         */
        private static void validateCommonLegParameters(Stop depStop, LocalDateTime depTime,
                                                        Stop arrStop, LocalDateTime arrTime) {
            Objects.requireNonNull(depStop);
            Objects.requireNonNull(depTime);
            Objects.requireNonNull(arrStop);
            Objects.requireNonNull(arrTime);
            checkArgument(!arrTime.isBefore(depTime));
        }

        /**
         * Enregistrement représentant un arrêt intermédiaire d’une étape.
         *
         * @param stop    l’arrêt intermédiaire.
         * @param arrTime la date/heure d’arrivée à cet arrêt.
         * @param depTime la date/heure de départ de cet arrêt.
         */
        record IntermediateStop(Stop stop, LocalDateTime arrTime, LocalDateTime depTime) {

            /**
             * Constructeur compact qui valide les arguments.
             *
             * @param stop    l’arrêt intermédiaire (non nul).
             * @param arrTime la date/heure d’arrivée.
             * @param depTime la date/heure de départ (ne doit pas précéder arrTime).
             * @throws NullPointerException     si stop est nul.
             * @throws IllegalArgumentException si depTime est antérieur à arrTime.
             */
            public IntermediateStop {
                Objects.requireNonNull(stop);
                checkArgument(!depTime.isBefore(arrTime));
            }
        }

        /**
         * Enregistrement représentant une étape effectuée en transport public.
         *
         * @param depStop           l’arrêt de départ.
         * @param depTime           la date/heure de départ.
         * @param arrStop           l’arrêt d’arrivée.
         * @param arrTime           la date/heure d’arrivée.
         * @param intermediateStops la liste des arrêts intermédiaires.
         * @param vehicle           le type de véhicule utilisé.
         * @param route             le nom de la ligne.
         * @param destination       la destination finale.
         */
        record Transport(Stop depStop, LocalDateTime depTime,
                         Stop arrStop, LocalDateTime arrTime,
                         List<IntermediateStop> intermediateStops, Vehicle vehicle,
                         String route, String destination)
                implements Leg {

            /**
             * Constructeur compact qui valide l’ensemble des arguments et utilise méthode
             * auxiliaire qui lève des NullPointerException et IllegalArgumentException
             *
             * @param depStop           l’arrêt de départ (non nul).
             * @param depTime           la date/heure de départ (non nul).
             * @param arrStop           l’arrêt d’arrivée (non nul).
             * @param arrTime           la date/heure d’arrivée (non nul et <depTime).
             * @param intermediateStops la liste des arrêts intermédiaires (immuabilité).
             * @param vehicle           le véhicule utilisé (non nul).
             * @param route             le nom de la ligne (non nul).
             * @param destination       la destination (non nul).
             * @throws NullPointerException     si un argument requis est nul.
             * @throws IllegalArgumentException si date/heure arrivée est avant date/heure départ
             */
            public Transport {
                validateCommonLegParameters(depStop, depTime, arrStop, arrTime);

                Objects.requireNonNull(vehicle);
                Objects.requireNonNull(route);
                Objects.requireNonNull(destination);

                // Copie défensive pour garantir l'immuabilité
                intermediateStops = List.copyOf(intermediateStops);
            }
        }

        /**
         * Enregistrement représentant une étape effectuée à pied.
         *
         * @param depStop l’arrêt de départ.
         * @param depTime la date/heure de départ.
         * @param arrStop l’arrêt d’arrivée.
         * @param arrTime la date/heure d’arrivée.
         */
        record Foot(Stop depStop, LocalDateTime depTime,
                    Stop arrStop, LocalDateTime arrTime)
                implements Leg {

            /**
             * Constructeur compact qui valide les arguments en appelant une méthode auxiliaire.
             *
             * @param depStop l’arrêt de départ (non nul).
             * @param depTime la date/heure de départ (non nul).
             * @param arrStop l’arrêt d’arrivée (non nul).
             * @param arrTime la date/heure d’arrivée (non nul et ne doit pas précéder depTime).
             * @throws NullPointerException     si un argument requis est nul.
             * @throws IllegalArgumentException si arrTime précède depTime.
             */
            public Foot {
                validateCommonLegParameters(depStop, depTime, arrStop, arrTime);
            }

            /**
             * Une étape à pied ne comporte jamais d’arrêts intermédiaires.
             *
             * @return une liste vide.
             */
            @Override
            public List<IntermediateStop> intermediateStops() {
                return List.of();
            }

            /**
             * Indique si l’étape représente un changement au sein de la même gare,
             * c’est-à-dire si le nom de l’arrêt de départ est identique à celui d’arrivée.
             *
             * @return true s’il s’agit d’un changement, false sinon.
             */
            public boolean isTransfer() {
                return depStop.name().equals(arrStop.name());
            }
        }
    }
}