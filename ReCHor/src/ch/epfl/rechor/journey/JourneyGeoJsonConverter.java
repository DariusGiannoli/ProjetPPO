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
    private final static double roundingConstant = Math.pow(10, 5);


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
            addCoordinateToArray(journey.legs().get(i), coordinates);
        }

        Json.JArray array = new Json.JArray(coordinates);

        map.put("type", new Json.JString("LineString"));
        map.put("coordinates", array);

        Json.JObject geoJson = new Json.JObject(map);

        return geoJson.toString();
    }

    /**
     * Prend un index d'étape dans le voyage et ajoute les coordonnées des arrêts de départ
     * et d'arrivée ainsi que les arrêts intermédiaires au document GeoJSON
     * si ces coordonnées ne sont pas déjà présentes dans le document.
     *
     * @param leg l'étape dont on ajoute les arrêts au document GeoJSON
     * @param coordinates une list de tous les tableaux JSON de coordonnées
     *                    ajoutés au document GeoJSON
     */
    private static void addCoordinateToArray(Journey.Leg leg, List<Json> coordinates) {

        createArrayStop(leg.depStop(), coordinates);

        if(leg instanceof Journey.Leg.Transport) {
            for (Journey.Leg.IntermediateStop interStop : leg.intermediateStops()) {
                createArrayStop(interStop.stop(), coordinates);
            }
        }

        createArrayStop(leg.arrStop(), coordinates);

    }

    /**
     * Ajoute à la liste coordinates les coordonnées de l'arrêt donné en argument si elles ne sont
     * pas déja présentes dans la liste.
     * @param stop l'arrêt dont on ajoute les coordonnées à la liste.
     * @param coordinates la liste des coordonnées du document GeoJSON
     */
    private static void createArrayStop(Stop stop, List<Json> coordinates) {
        double longitude = Math.ceil(stop.longitude() * roundingConstant)
                / roundingConstant;
        double latitude = Math.ceil(stop.latitude() * roundingConstant)
                / roundingConstant;


        List<Json> depCoordinate = new ArrayList<>();

        depCoordinate.add(new Json.JNumber(longitude));
        depCoordinate.add(new Json.JNumber(latitude));

        Json.JArray array = new Json.JArray(depCoordinate);

        if(!coordinates.contains(array)) {
            coordinates.add(array);
        }

    }

}
