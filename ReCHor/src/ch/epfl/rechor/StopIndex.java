// StopIndex.java
package ch.epfl.rechor;

import java.util.*;
import java.util.regex.*;

/**
 * Index de noms d’arrêts avec recherche par requête.
 */
public final class StopIndex {
    // table des équivalences accentuées
    private static final Map<Character, String> ACC_EQ = Map.of(
            'c', "cç",
            'a', "aáàâä",
            'e', "eéèêë",
            'i', "iíìîï",
            'o', "oóòôö",
            'u', "uúùûü"
    );

    private final Map<String, String> nameToMain;
    private final List<String> allNames;

    /**
     * Constructor.
     * @param mainNames liste des noms principaux
     * @param altToMain map des noms alternatifs → nom principal
     */
    public StopIndex(List<String> mainNames, Map<String, String> altToMain) {
        var m = new HashMap<String, String>();
        // chaque nom principal pointe sur lui‑même
        for (String main : mainNames) {
            m.put(main, main);
        }
        // chaque alternatif, si son principal existe, pointe sur le principal
        for (var e : altToMain.entrySet()) {
            if (m.containsKey(e.getValue())) {
                m.put(e.getKey(), e.getValue());
            }
        }
        nameToMain = Map.copyOf(m);
        allNames   = List.copyOf(nameToMain.keySet());
    }

    /**
     * Recherche les arrêts correspondant à la requête.
     * @param query la requête (mots séparés par espaces)
     * @param maxResults nombre maximal de résultats à retourner
     * @return liste de noms principaux, triés par pertinence décroissante
     */
    public List<String> stopsMatching(String query, int maxResults) {
        String[] subs = query.trim().split("\\s+");
        // 1) compiler une ER par sous‑requête
        List<Pattern> patterns = Arrays.stream(subs)
                .map(this::regexFor)
                .toList();

        // 2) pour chaque nom possible, tester toutes les ER
        Map<String, Integer> bestScore = new HashMap<>();
        for (String name : allNames) {
            boolean ok = patterns.stream()
                    .allMatch(p -> p.matcher(name).find());
            if (!ok) continue;

            // 3) calculer le score total (somme des sous‑scores)
            int total = 0;
            for (int i = 0; i < subs.length; i++) {
                total += score(name, subs[i], patterns.get(i));
            }

            // 4) garder le meilleur score par nom principal
            String main = nameToMain.get(name);
            bestScore.merge(main, total, Math::max);
        }

        // 5) trier par score décroissant, limiter, et ne retourner que les noms
        return bestScore.entrySet().stream()
                .sorted(Map.Entry.<String,Integer>comparingByValue(Comparator.reverseOrder()))
                .limit(maxResults)
                .map(Map.Entry::getKey)
                .toList();
    }

    // transforme une sous‑requête en Pattern Java, avec flags adaptés
    private Pattern regexFor(String sub) {
        boolean hasUpper = sub.chars().anyMatch(Character::isUpperCase);
        int flags = Pattern.UNICODE_CASE
                | (hasUpper ? 0 : Pattern.CASE_INSENSITIVE);

        StringBuilder sb = new StringBuilder();
        for (char c : sub.toCharArray()) {
            char low = Character.toLowerCase(c);
            if (ACC_EQ.containsKey(low)) {
                // classe de tous les équivalents
                sb.append('[').append(ACC_EQ.get(low)).append(']');
            } else {
                // char littéral, protégé
                sb.append(Pattern.quote(String.valueOf(c)));
            }
        }
        return Pattern.compile(sb.toString(), flags);
    }

    // calcule le score d’une sous‑requête sur un nom
    private int score(String name, String sub, Pattern p) {
        Matcher m = p.matcher(name);
        if (!m.find()) return 0;
        int start = m.start(), end = m.end();
        int base = sub.length() * 100 / name.length(); // floor par défaut

        // x4 si au début d’un mot
        if (start == 0 || !Character.isLetter(name.charAt(start - 1))) {
            base *= 4;
        }
        // x2 si à la fin d’un mot
        if (end == name.length() || !Character.isLetter(name.charAt(end))) {
            base *= 2;
        }
        return base;
    }
}