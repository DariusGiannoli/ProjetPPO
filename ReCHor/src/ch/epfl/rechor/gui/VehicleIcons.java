package gui;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

import ch.epfl.rechor.journey.Vehicle;
import javafx.scene.image.Image;

/**
 * Classe utilitaire non instanciable fournissant les icônes des véhicules.
 * Chaque image est chargée une seule fois depuis les ressources
 * (TRAIN.png, BUS.png, etc.) et stockée en cache pour réutilisation.
 */
public final class VehicleIcons {

    // Ne doit jamais être instanciée
    private VehicleIcons() {
        throw new AssertionError("VehicleIcons ne doit pas être instanciée");
    }

    // Cache des images : clé = Vehicle, valeur = Image JavaFX
    private static final Map<Vehicle, Image> CACHE = new EnumMap<>(Vehicle.class);

    /**
     * Retourne l’icône JavaFX correspondant au véhicule donné.
     * La première fois, l’image est chargée depuis le classpath via
     * new Image("VEHICLE_NAME.png") ; puis conservée en cache.
     *
     * @param vehicle le type de véhicule (non null)
     * @return l’instance unique d’Image pour ce véhicule
     * @throws NullPointerException si vehicle est null
     */
    public static Image iconFor(Vehicle vehicle) {
        // computeIfAbsent en une ligne, grâce à EnumMap et au nom d’énumération
        return CACHE.computeIfAbsent(
                Objects.requireNonNull(vehicle, "vehicle ne peut pas être null"),
                v -> new Image(v.name() + ".png")
        );
    }
}
