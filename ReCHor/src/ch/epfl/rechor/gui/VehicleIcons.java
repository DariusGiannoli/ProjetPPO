
package ch.epfl.rechor.gui;

import ch.epfl.rechor.journey.Vehicle;
import javafx.scene.image.Image;

import java.util.EnumMap;
import java.util.Map;

/**
 * Classe publique et non instanciable,
 * charge et met en cache une seule fois chaque icône de véhicule.
 *
 * Utilise EnumMap<Vehicle,Image> et computeIfAbsent .
 */
public final class VehicleIcons {
    // cache puissant et efficace pour un enum
    private static final Map<Vehicle, Image> CACHE =
            new EnumMap<>(Vehicle.class);

    // pas d'instances
    private VehicleIcons() {}

    /**
     * Retourne l'icône JavaFX correspondante au type de véhicule donné.
     * Pour chaque Vehicle, on charge « VEHICLENAME.png » depuis le dossier resources
     * et on ne le fait qu’une seule fois grâce au cache.
     *
     * @param vehicle le type de véhicule
     * @return l’Image associée (toujours la même instance pour un même vehicle)
     */
    public static Image iconFor(Vehicle vehicle) {
        return CACHE.computeIfAbsent(vehicle,
                v -> new Image(v.name() + ".png")
        );
    }
}
