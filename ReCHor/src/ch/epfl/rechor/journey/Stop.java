package ch.epfl.rechor.journey;

import ch.epfl.rechor.Preconditions;

import java.util.Objects;

/**
 * Représente un arrêt de transport public Suisse.
 * L'arrêt peut être une gare (platformName nul) ou une voie/quai (platformName non nul).
 * Chaque arrêt est identifié par son nom et ses coordonnées géographiques.
 *
 * @param name         le nom de l’arrêt.
 * @param platformName le nom de la voie ou du quai (null si l’arrêt est une gare).
 * @param longitude    la longitude en degrés.
 * @param latitude     la latitude en degrés.
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */
public record Stop(String name, String platformName, double longitude, double latitude) {

    // Constantes géographiques
    private static final double MAX_LON = 180.0;
    private static final double MIN_LON = -180.0;
    private static final double MAX_LAT = 90.0;
    private static final double MIN_LAT = -90.0;

    /**
     * Constructeur compact de Stop.
     * Vérifie que le nom n’est pas nul
     * et que les coordonnées sont comprises dans les limites autorisées.
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

        Preconditions.checkArgument(
                longitude >= MIN_LON && longitude <= MAX_LON);
        Preconditions.checkArgument(
                latitude >= MIN_LAT && latitude <= MAX_LAT);
    }
}