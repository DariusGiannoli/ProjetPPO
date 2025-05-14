package ch.epfl.rechor;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * L'interface Json représente un document JSON.
 * Elle est implémentée par 4 enregistrements imbriqués dans elle,
 * qui représentent les types de données JSON utiles à ce projet.
 *
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */
public sealed interface Json {
    // Ponctuation JSON
    String COMMA     = ",";
    String COLON     = ":";
    String LBRACE    = "{";
    String RBRACE    = "}";
    String LBRACKET  = "[";
    String RBRACKET  = "]";
    String QUOTE     = "\"";

    /**
     * Représente un tableau JSON
     * et possède comme unique attribut une liste de valeurs de type Json.
     * @param list les éléments du tableau de type Json
     */
    record JArray(List<Json> list) implements Json {
        @Override
        public String toString() {
            StringJoiner sj = new StringJoiner(COMMA, LBRACKET, RBRACKET);
            for (Json e : list) {
                sj.add(e.toString());
            }
            return sj.toString();
        }
    }

    /**
     * Représente un objet JSON et possède comme unique attribut une table associative
     * qui associe des valeurs de type Json à des chaînes de caractères.
     * @param map table associative qui associe des valeurs de type Json à des chaînes de caractères
     */
    record JObject(Map<String, Json> map) implements Json {
        @Override
        public String toString() {
            StringJoiner sj = new StringJoiner(COMMA, LBRACE, RBRACE);
            for (Map.Entry<String, Json> e : map.entrySet()) {
                sj.add(QUOTE + e.getKey() + QUOTE
                        + COLON
                        + e.getValue().toString());
            }
            return sj.toString();
        }
    }

    /**
     * Représente une chaîne JSON et possède comme unique attribut une chaîne de caractères
     * dont le contenu est celui de la chaîne JSON.
     * @param string la chaine de caractères dont le contenu est celle de la chaine JSON
     */
    record JString(String string) implements Json {
        @Override
        public String toString() {
            return QUOTE + string + QUOTE;
        }
    }

    /**
     * représente un nombre JSON
     * et possède comme attribut une valeur de type double dont la valeur est celle du nombre JSON.
     * @param number double dont la valeur est celle du nombre JSON
     */
    record JNumber(double number) implements Json {
        @Override
        public String toString() {
            return Double.toString(number);
        }
    }

}