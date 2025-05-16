package ch.epfl.rechor.gui;

import ch.epfl.rechor.journey.Vehicle;
import javafx.scene.image.Image;

import java.util.EnumMap;
import java.util.Map;

/**
 * Fournit l'accès aux icônes JavaFX des véhicules et assure leur chargement
 * et mise en cache uniques afin d'éviter tout gaspillage de mémoire.
 *
 * Utilise EnumMap<Vehicle,Image> et computeIfAbsent.
 *
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */
public final class VehicleIcons {
    private static final String ICON_EXTENSION = ".png";
    private static final Map<Vehicle, Image> CACHE = new EnumMap<>(Vehicle.class);

    /** Constructeur privé, classe non instanciable */
    private VehicleIcons() {}

    /**
     * Retourne l'icône JavaFX correspondante au type de véhicule donné.
     * Pour chaque Vehicle, on charge «VEHICLENAME.png» depuis le dossier resources
     * et on ne le fait qu’une seule fois grâce au cache.
     *
     * @param vehicle le type de véhicule
     * @return l’Image associée (toujours la même instance pour un même vehicle)
     */
    public static Image iconFor(Vehicle vehicle) {
        return CACHE.computeIfAbsent(vehicle,
                v -> new Image(v.name() + ICON_EXTENSION)
        );
    }
}