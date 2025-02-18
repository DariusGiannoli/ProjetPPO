package ch.epfl.rechor.journey;

import ch.epfl.rechor.Preconditions;

import java.util.Objects;

/**
 * @author Antoine Lepin
 * @author
 * Un arrêt de transport public.
 * @param name le nom de l'arrêt.
 * @param platformName le nom de la voie ou du quai, ou null si l'arrêt correspond à une gare
 * @param longitude la longitude de la position de l'arrêt, en degrés.
 * @param latitude la latitude de la position de l'arrêt, en degrés.
 */
public record Stop(String name, String platformName, double longitude, double latitude) {

    /**
     * Vérifie que le nom de l'arrêt ne soit pas null et que la longitude soit comprise entre -180 et 180(inclus) et la latitude entre -90 et 90(inclus)
     * sinon une exception est lancée par les méthodes requireNonNull (NullPointerException) et checkArgument (IllegalArgumentException).
     */
    public Stop {
        Objects.requireNonNull(name);
        Preconditions.checkArgument(longitude >= -180 && longitude <= 180 && latitude <= 90 && latitude >= -90);
    }
}
