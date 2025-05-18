// StopIndex.java
package ch.epfl.rechor;

import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * StopIndex représente un index de nom d'arrêts dans lequel il est possible d'effectuer
 * des recherches.
 *
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */
public final class StopIndex {

    private static final char LBRACKET  = '[';
    private static final char RBRACKET  = ']';

    //Match score
    private static final int SCORE_MULTIPLIER = 100;
    private static final int START_WORD_BONUS = 4;
    private static final int END_WORD_BONUS = 2;

    /** Table des équivalences accentuées (minuscule → variantes). */
    private static final Map<Character, String> ACCENT_EQUIVALENCES = Map.of(
            'c', "cç",
            'a', "aáàâä",
            'e', "eéèêë",
            'i', "iíìîï",
            'o', "oóòôö",
            'u', "uúùûü"
    );

    /** Map des noms (principaux et alternatifs) vers leur nom principal. */
    private final Map<String, String> nameToMain;
    /** Liste immuable de tous les noms indexés. */
    private final List<String> allNames;

    /**
     * Construit un index à partir des noms principaux et de leurs alternatives.
     *
     * @param mainNames liste des noms « officiels »
     * @param altToMain table de correspondance entre nom alternatif → nom principal
     */
    public StopIndex(List<String> mainNames, Map<String, String> altToMain) {
        HashMap<String, String> tempMap = new HashMap<>();
        mainNames.forEach(n -> tempMap.put(n, n));
        altToMain.forEach((alt, main) -> {
            if (tempMap.containsKey(main)) {
                tempMap.put(alt, main);
            }
        });
        nameToMain = Map.copyOf(tempMap);
        allNames   = List.copyOf(nameToMain.keySet());
    }

    /**
     * Recherche jusqu’à {@code maxResults} noms principaux d’arrêts
     * dont chacun contient toutes les sous-chaînes de la requête {@code query}.
     *
     * @param query      requête utilisateur ; mots séparés par espaces
     * @param maxResults nombre maximal de résultats à retourner
     * @return liste de noms principaux, sans doublons,
     *         ordonnée par pertinence décroissante
     * @throws NullPointerException     si query est null
     * @throws IllegalArgumentException si maxResults est négatif
     */
    public List<String> stopsMatching(String query, int maxResults) {
        // Définition de l'enregistrement représentant la paire (score, nom d'arrêt)
        record StopMatch(String stopName, int score) {}

        String[] subs = query.trim().split("\\s+");

        // Cas où la requête est vide
        if (subs.length == 0 || subs[0].isEmpty()) {
            return nameToMain.values().stream()
                    .distinct()
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .limit(maxResults)
                    .toList();
        }

        // Précalcul des longueurs
        int[] subLengths = new int[subs.length];
        for (int i = 0; i < subs.length; i++) {
            subLengths[i] = subs[i].length();
        }

        List<Pattern> patterns = Arrays.stream(subs)
                .map(this::regexFor)
                .toList();

        // Utiliser des flots pour créer et manipuler les paires (score, nom d'arrêt)
        return allNames.stream()
                // Évaluer chaque nom et créer un StopMatch
                .flatMap(name -> {
                    int score = scoreIfMatchesAll(name, subLengths, patterns);
                    if (score > 0) {
                        String mainName = nameToMain.get(name);
                        return Stream.of(new StopMatch(mainName, score));
                    }
                    return Stream.empty();
                })
                // Garder le score le plus élevé pour chaque nom principal
                .collect(Collectors.toMap(
                        StopMatch::stopName,
                        StopMatch::score,
                        Math::max))
                // Convertir le Map en Stream d'enregistrements
                .entrySet().stream()
                .map(e -> new StopMatch(e.getKey(), e.getValue()))
                // Trier par score décroissant puis par nom
                .sorted(Comparator.<StopMatch>comparingInt(sm -> -sm.score)
                        .thenComparing(sm -> sm.stopName, String.CASE_INSENSITIVE_ORDER))
                .limit(maxResults)
                .map(StopMatch::stopName)
                .toList();
    }

    /**
     * Calcule le score total d'un nom selon toutes les sous-requêtes,
     * ou retourne 0 si une sous-requête ne matche pas.
     *
     * @param name     nom complet testé
     * @param subLengths     tableau des sous-chaînes de la requête
     * @param patterns patterns compilés correspondant aux sous-chaînes
     * @return score total (≥ 0)
     */
    private int scoreIfMatchesAll(String name, int[] subLengths, List<Pattern> patterns) {
        int total = 0;
        int nlen = name.length();
        for (int i = 0; i < patterns.size(); i++) {
            Pattern p = patterns.get(i);
            Matcher m = p.matcher(name);
            if (!m.find()) {
                return 0;
            }
            int start = m.start();
            int end   = m.end();
            int length = subLengths[i];

            int base  = length * SCORE_MULTIPLIER / nlen;

            if (start == 0 || !Character.isLetter(name.charAt(start - 1))) {
                base *= START_WORD_BONUS;
            }
            if (end == name.length() || !Character.isLetter(name.charAt(end))) {
                base *= END_WORD_BONUS;
            }
            total += base;
        }
        return total;
    }

    /**
     * Construit un Pattern Java pour une sous-requête,
     * tenant compte des variantes accentuées et de la casse.
     *
     * @param sub sous-chaîne de la requête
     * @return Pattern correspondant à la recherche de sub
     */
    private Pattern regexFor(String sub) {
        boolean hasUpper = sub.chars().anyMatch(Character::isUpperCase);
        int flags = Pattern.UNICODE_CASE | (hasUpper ? 0 : Pattern.CASE_INSENSITIVE);

        StringBuilder sb = new StringBuilder();
        for (char c : sub.toCharArray()) {
            char low = Character.toLowerCase(c);
            if (ACCENT_EQUIVALENCES.containsKey(low)) {
                String variants = ACCENT_EQUIVALENCES.get(low);
                if (Character.isUpperCase(c)) {
                    variants = variants.toUpperCase();
                }
                sb.append(LBRACKET).append(variants).append(RBRACKET);
            } else {
                sb.append(Pattern.quote(String.valueOf(c)));
            }
        }
        return Pattern.compile(sb.toString(), flags);
    }
}