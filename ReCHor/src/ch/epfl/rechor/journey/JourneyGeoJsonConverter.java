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

        List<Json> coordinates = new ArrayList<>();

        Map<String, Json> map = new LinkedHashMap<>();

        for(int i = 0; i < journey.legs().size(); i++){
            addCoordinateToArray(journey, coordinates, i);
        }

        Json.JArray array = new Json.JArray(coordinates);

        map.put("type", new Json.JString("LineString"));
        map.put("coordinates", array);

        Json.JObject geoJson = new Json.JObject(map);

        return geoJson.toString();
    }

    /**
     * Prend un index d'étape dans le voyage et ajoute les coordonnées des arrêts de départ
     * et d'arrivée au document GeoJSON
     * si ces coordonnées ne sont pas déjà présentes dans le document.
     *
     * @param journey le voyage à convertir en document GeoJSON
     * @param coordinates une list de tous les tableaux JSON de coordonnées
     *                    ajoutés au document GeoJSON
     * @param i l'index de l'étape dont on veut extraire les coordonnées.
     */
    private static void addCoordinateToArray(Journey journey, List<Json> coordinates, int i) {
        double roundingConstant = Math.pow(10, 5);

        double longitude = Math.ceil(journey.legs().get(i).depStop().longitude() * roundingConstant)
                / roundingConstant;
        double latitude = Math.ceil(journey.legs().get(i).depStop().latitude() * roundingConstant)
                / roundingConstant;

        Json.JNumber JsonLongitude = new Json.JNumber(longitude);
        Json.JNumber JsonLatitude = new Json.JNumber(latitude);

        List<Json> depCoordinate = new ArrayList<>();

        depCoordinate.add(JsonLongitude);
        depCoordinate.add(JsonLatitude);

        Json.JArray CoordinateArray = new Json.JArray(depCoordinate);

        if(!coordinates.contains(CoordinateArray)) {
            coordinates.add(CoordinateArray);
        }


        longitude = Math.ceil(journey.legs().get(i).arrStop().longitude() * roundingConstant)
                / roundingConstant;
        latitude = Math.ceil(journey.legs().get(i).arrStop().latitude() * roundingConstant)
                / roundingConstant;

        JsonLongitude = new Json.JNumber(longitude);
        JsonLatitude = new Json.JNumber(latitude);

        List<Json> arrCoordinate = new ArrayList<>();

        arrCoordinate.add(JsonLongitude);
        arrCoordinate.add(JsonLatitude);

        CoordinateArray = new Json.JArray(arrCoordinate);

        if(!coordinates.contains(CoordinateArray)) {
            coordinates.add(CoordinateArray);
        }

    }
}
