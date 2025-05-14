// StopIndex.java
package ch.epfl.rechor;

import java.util.*;
import java.util.regex.*;

/**
 * Index de noms d’arrêts avec recherche par requête.
 */
public final class StopIndex {

    private static final char LBRACKET  = '[';
    private static final char RBRACKET  = ']';

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
     * @param altToMain table de correspondance alternatif → principal
     */
    public StopIndex(List<String> mainNames, Map<String, String> altToMain) {
        var tempMap = new HashMap<String, String>();
        mainNames.forEach(n -> tempMap.put(n, n));
        altToMain.forEach((alt, main) -> {
            if (tempMap.containsKey(main)) {
                tempMap.put(alt, main);
            }
        });
        nameToMain = Map.copyOf(tempMap);
        allNames   = List.copyOf(nameToMain.keySet());

//        var tempMap = new HashMap<String, String>();
//        // chaque nom principal pointe sur lui‑même
//        for (String main : mainNames) {
//            tempMap.put(main, main);
//        }
//        // chaque alternatif, si son principal existe, pointe sur le principal
//        for (var e : altToMain.entrySet()) {
//            if (tempMap.containsKey(e.getValue())) {
//                tempMap.put(e.getKey(), e.getValue());
//            }
//        }
//        nameToMain = Map.copyOf(tempMap);
//        allNames   = List.copyOf(nameToMain.keySet());
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

        String[] subs = query.trim().split("\\s+");
        List<Pattern> patterns = Arrays.stream(subs)
                .map(this::regexFor)
                .toList();

        Map<String, Integer> bestScore = new HashMap<>();
        for (String name : allNames) {
            if (patterns.stream().anyMatch(p -> !p.matcher(name).find())) {
                continue;
            }
            int total = scoreIfMatchesAll(name, subs, patterns);

            String main = nameToMain.get(name);
            bestScore.merge(main, total, Math::max);
        }

        return bestScore.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(maxResults)
                .map(Map.Entry::getKey)
                .toList();
    }


//    /**
//     * Recherche jusqu’à maxResults noms principaux d’arrêts
//     * dont chacun contient toutes les sous-chaînes de la requête {@code query}.
//     *
//     * @param query      requête utilisateur ; mots séparés par espaces
//     * @param maxResults nombre maximal de résultats à retourner
//     * @return liste de noms principaux, sans doublons,
//     *         ordonnée par pertinence décroissante
//     */
//    public List<String> stopsMatching(String query, int maxResults) {
//        String[] subs = query.trim().split("\\s+");
//
//        // 1) compiler une ER par sous‑requête
//        List<Pattern> patterns = Arrays.stream(subs)
//                                       .map(this::regexFor)
//                                       .toList();
//
//        // 2) pour chaque nom possible, tester toutes les ER
//        Map<String, Integer> bestScore = new HashMap<>();
//        for (String name : allNames) {
//            if (patterns.stream().anyMatch(p -> !p.matcher(name).find())) {
//                continue;
//            }
//
//            // 3) calculer le score total (somme des sous‑scores)
//            int total = totalScore(name, subs, patterns);
//
//
//            // 4) garder le meilleur score par nom principal
//            String main = nameToMain.get(name);
//            bestScore.merge(main, total, Math::max);
//        }
//
//        // 5) trier par score décroissant, puis clé croissante
//        List<String> topStops = bestScore.entrySet().stream()
//                .sorted(Map.Entry.<String,Integer>comparingByValue(Comparator.reverseOrder())
//                                .thenComparing(Map.Entry.comparingByKey()))
//                .limit(maxResults)
//                .map(Map.Entry::getKey)
//                .toList();
//
//        return topStops;
//    }

    /**
     * Calcule le score total d'un nom selon toutes les sous-requêtes,
     * ou retourne 0 si une sous-requête ne matche pas.
     *
     * @param name     nom complet testé
     * @param subs     tableau des sous-chaînes de la requête
     * @param patterns patterns compilés correspondant aux sous-chaînes
     * @return score total (≥ 0)
     */
    private int scoreIfMatchesAll(String name, String[] subs, List<Pattern> patterns) {
        int total = 0;
        for (int i = 0; i < subs.length; i++) {
            Pattern p = patterns.get(i);
            Matcher m = p.matcher(name);
            if (!m.find()) {
                return 0;
            }
            int start = m.start();
            int end   = m.end();
            int base  = subs[i].length() * 100 / name.length();
            if (start == 0 || !Character.isLetter(name.charAt(start - 1))) {
                base *= 4;
            }
            if (end == name.length() || !Character.isLetter(name.charAt(end))) {
                base *= 2;
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

        var sb = new StringBuilder();
        for (char c : sub.toCharArray()) {
            char low = Character.toLowerCase(c);
            if (ACCENT_EQUIVALENCES.containsKey(low)) {
                // classe de tous les équivalents
                sb.append(LBRACKET).append(ACCENT_EQUIVALENCES.get(low)).append(RBRACKET);
            } else {
                // char littéral, protégé
                sb.append(Pattern.quote(String.valueOf(c)));
            }
        }
        return Pattern.compile(sb.toString(), flags);
    }

//    /**
//     * Calcule le score d’une sous-requête pour un nom donné,
//     * selon la position et la couverture.
//     *
//     * @param name nom de l’arrêt
//     * @param sub  sous-chaîne recherchée
//     * @param p    Pattern compilé pour sub
//     * @return score calculé (≥ 0)
//     */
//    private int score(String name, String sub, Pattern p) {
//        Matcher m = p.matcher(name);
//        if (!m.find()) {
//            return 0;
//        }
//        int start = m.start(), end = m.end();
//        int base = sub.length() * 100 / name.length(); // floor par défaut
//
//        // x4 si au début d’un mot
//        if (start == 0 || !Character.isLetter(name.charAt(start - 1))) {
//            base *= 4;
//        }
//        // x2 si à la fin d’un mot
//        if (end == name.length() || !Character.isLetter(name.charAt(end))) {
//            base *= 2;
//        }
//        return base;
//    }



//    /**
//     * Calcule la somme des scores pour chaque sous-requête.
//     *
//     * @param name     nom complet testé
//     * @param subs     tableau des sous-chaînes de la requête
//     * @param patterns patterns compilés correspondant à subs
//     * @return score total (≥ 0)
//     */
//    private int totalScore(String name, String[] subs, List<Pattern> patterns) {
//        int total = 0;
//        for (int i = 0; i < subs.length; i++) {
//            total += score(name, subs[i], patterns.get(i));
//        }
//        return total;
//    }
}