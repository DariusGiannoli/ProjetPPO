package ch.epfl.rechor.journey;

import java.util.List;

/**
 * Type énuméré pour le type de transport utilisé.
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */
public enum Vehicle {
    TRAM,
    METRO,
    TRAIN,
    BUS,
    FERRY,
    AERIAL_LIFT,
    FUNICULAR;

    /**
     * Liste de tous les véhicules.
     */
    public static final List<Vehicle> ALL = List.of(Vehicle.values());

}
