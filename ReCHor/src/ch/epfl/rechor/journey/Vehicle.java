package ch.epfl.rechor.journey;

import java.util.List;

/**
 * Enumération des types de transport public
 *
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
     * Liste immuable de l’ensemble des types de véhicules, dans l’ordre de définition.
     */
    public static final List<Vehicle> ALL = List.of(Vehicle.values());
}
