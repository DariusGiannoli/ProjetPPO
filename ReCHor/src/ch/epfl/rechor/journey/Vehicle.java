package ch.epfl.rechor.journey;

import java.util.List;

/**
 * Enumération des types de transport public utilisés dans le système.
 * Chaque type correspond à un mode de transport distinct dans le réseau suisse.
 *
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */
public enum Vehicle {
    /** Tram */
    TRAM,
    /** Métro */
    METRO,
    /** Train */
    TRAIN,
    /** Bus/Car */
    BUS,
    /** Ferry/Bateau */
    FERRY,
    /** Télécabine/Transport aérien à câble */
    AERIAL_LIFT,
    /** Funiculaire */
    FUNICULAR;

    /**
     * Liste immuable de l'ensemble des types de véhicules, dans l'ordre de définition.
     * Utile pour itérer sur tous les types de véhicules disponibles.
     * */
    public static final List<Vehicle> ALL = List.of(values());
}