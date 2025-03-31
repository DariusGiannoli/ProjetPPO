package ch.epfl.rechor.journey;

import ch.epfl.rechor.Preconditions;
import java.util.Objects;

/**
 * Représente un arrêt de transport public.
 * L’arrêt peut être une gare (platformName nul) ou une voie/quai (platformName non nul).
 *
 * @param name         le nom de l’arrêt.
 * @param platformName le nom de la voie ou du quai (null si l’arrêt est une gare).
 * @param longitude    la longitude en degrés.
 * @param latitude     la latitude en degrés.
 *
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */
public record Stop(String name, String platformName, double longitude, double latitude) {

    /**
     * Constructeur compact de Stop.
     * Vérifie que le nom n’est pas nul et que les coordonnées sont comprises dans les limites autorisées.
     *
     * @param name         le nom de l’arrêt (non nul).
     * @param platformName le nom de la voie/quai (peut être nul).
     * @param longitude    la longitude, comprise entre -180 et 180 (inclus).
     * @param latitude     la latitude, comprise entre -90 et 90 (inclus).
     * @throws NullPointerException     si name est nul.
     * @throws IllegalArgumentException si les coordonnées sont invalides.
     */
    public Stop {
        Objects.requireNonNull(name);
        Preconditions.checkArgument(longitude >= -180 && longitude <= 180 && latitude <= 90 && latitude >= -90);
    }
}
