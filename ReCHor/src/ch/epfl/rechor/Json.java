package ch.epfl.rechor;

import java.util.List;
import java.util.Map;

/**
 * L'interface Json représente un document JSON.
 * Elle est implémentée par 4 enregistrements imbriqués dans elle,
 * qui représentent les types de données JSON utiles à ce projet.
 *
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */
public sealed interface Json {
    String comma = ",";
    String quote = "\"";

    /**
     * Représente un tableau JSON
     * et possède comme unique attribut une liste de valeurs de type Json.
     * @param list les éléments du tableau de type Json
     */
    record JArray(List<Json> list) implements Json {
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for(int i = 0; i < list.size(); i++) {
                if(i == 0) {
                    sb.append(list.get(i).toString());
                } else {
                    sb.append(comma).append(list.get(i).toString());
                }
            }
            sb.append("]");
            return sb.toString();
        }
    }


    /**
     * Représente un objet JSON et possède comme unique attribut une table associative
     * qui associe des valeurs de type Json à des chaînes de caractères.
     * @param map table associative qui associe des valeurs de type Json à des chaînes de caractères
     */
    record JObject(Map<String, Json> map) implements Json {
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            for (Map.Entry<String, Json> e : map.entrySet()) {
                String k = e.getKey();
                Json v = e.getValue();
                sb.append(quote).append(k).append(quote)
                        .append(":").append(v.toString()).append(comma);
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.append("}");
            return sb.toString();
        }
    }

    /**
     * Représente une chaîne JSON et possède comme unique attribut une chaîne de caractères
     * dont le contenu est celui de la chaîne JSON.
     * @param string la chaine de caractères dont le contenu est celle de la chaine JSON
     */
    record JString(String string) implements Json {
        public String toString() {
            return quote + string + quote;
        }
    }


    /**
     * représente un nombre JSON
     * et possède comme attribut une valeur de type double dont la valeur est celle du nombre JSON.
     * @param number double dont la valeur est celle du nombre JSON
     */
    record JNumber(double number) implements Json {
        public String toString() {
            return Double.toString(number);
        }
    }

}
