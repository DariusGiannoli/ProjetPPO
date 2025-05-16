package ch.epfl.rechor.journey;

import ch.epfl.rechor.Json;
import java.util.*;

/**
 * La classe JourneyGeoJsonConverter offre une méthode,
 * nommée toGeoJson, permettant de convertir un voyage
 * en un document GeoJSON représentant son tracé.
 *
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */
public class JourneyGeoJsonConverter {
    /** Facteur pour arrondir les coordonnées à 5 décimales */
    private static final double roundingConstant = Math.pow(10, 5);

    /**
     * Constructeur privé pour que la classe soit non instanciable.
     */
    private JourneyGeoJsonConverter() {}

    /**
     * Convertit un voyage en un document GeoJSON représentant son tracé.
     * @param journey le voyage à convertir en document GeoJSON
     * @return une chaine de caractère, qui est la representation du voyage passé en argument,
     * au format GeoJSON
     */
    public static String toGeoJson(Journey journey) {
        List<Json> coords = new ArrayList<>();
        for (Journey.Leg leg : journey.legs()) {
            addCoordinates(leg, coords);
        }

        Json.JArray array = new Json.JArray(coords);
        var obj = new LinkedHashMap<String, Json>();
        obj.put("type", new Json.JString("LineString"));
        obj.put("coordinates", array);

        return new Json.JObject(obj).toString();


    }

    /**
     * Prend un index d'étape dans le voyage et ajoute les coordonnées des arrêts de départ
     * et d'arrivée ainsi que les arrêts intermédiaires au document GeoJSON
     * si ces coordonnées ne sont pas déjà présentes dans le document.
     *
     * @param leg l'étape dont on ajoute les arrêts au document GeoJSON
     * @param coords une list de tous les tableaux JSON de coordonnées
     *                    ajoutés au document GeoJSON
     */
    private static void addCoordinates(Journey.Leg leg, List<Json> coords) {
        addStop(leg.depStop(), coords);

        if(leg instanceof Journey.Leg.Transport) {
            for (Journey.Leg.IntermediateStop interStop : leg.intermediateStops()) {
                addStop(interStop.stop(), coords);
            }
        }
        addStop(leg.arrStop(), coords);
    }

    /**
     * Ajoute à la liste coords les coordonnées de l'arrêt donné en argument si elles ne sont
     * pas déja présentes dans la liste.
     * @param stop l'arrêt dont on ajoute les coordonnées à la liste.
     * @param coords la liste des coordonnées du document GeoJSON
     */
    private static void addStop(Stop stop, List<Json> coords) {
        double lon = Math.ceil(stop.longitude() * roundingConstant) / roundingConstant;
        double lat = Math.ceil(stop.latitude() * roundingConstant) / roundingConstant;

        Json point = new Json.JArray(List.of(new Json.JNumber(lon), new Json.JNumber(lat)));
        if (!coords.contains(point)) {
            coords.add(point);
        }

    }
}
