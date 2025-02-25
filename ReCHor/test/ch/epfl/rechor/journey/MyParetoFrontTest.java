package ch.epfl.rechor.journey;

import ch.epfl.rechor.Preconditions;
import ch.epfl.rechor.journey.ParetoFront.Builder;
import ch.epfl.rechor.journey.ParetoFront;
import ch.epfl.rechor.journey.PackedCriteria;
import org.junit.jupiter.api.Test;
import org.w3c.dom.ls.LSOutput;
import java.util.Arrays;

import java.util.List;
import java.util.NoSuchElementException;


import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;


import static org.junit.jupiter.api.Assertions.*;

public class MyParetoFrontTest {

    private static long[] frontierToLongArray(ParetoFront pf) {
        List<Long> list = new ArrayList<>();
        pf.forEach(list::add);
        long[] arr = new long[list.size()];
        for (int i = 0; i < list.size(); i++) {
            arr[i] = list.get(i);
        }
        return arr;
    }

    @Test
    void newBuilderIsEmpty() {
        Builder b = new Builder();
        assertTrue(b.isEmpty(), "Un builder tout neuf devrait être vide");

        ParetoFront pf = b.build();
        assertEquals(0, pf.size(), "La ParetoFront construite devrait être vide");
    }

    @Test
    void addSingleTuple() {
        Builder b = new Builder();
        assertTrue(b.isEmpty());

        // Ajout d'un tuple (arrMins=100, changes=2, payload=1234)
        b.add(100, 2, 1234);

        ParetoFront pf = b.build();
        assertFalse(b.isEmpty(), "Le builder ne doit plus être vide après un ajout");
        assertEquals(1, pf.size(), "La frontière devrait contenir exactement 1 élément");

        long[] frontier = frontierToLongArray(pf);
        long expected = PackedCriteria.pack(100, 2, 1234);
        assertEquals(expected, frontier[0], "Le tuple stocké n'est pas celui attendu");
    }

    @Test
    void addDominatedTupleIsIgnored() {
        Builder b = new Builder();
        // T1 : arrMins=200, changes=2
        long t1 = PackedCriteria.pack(200, 2, 9999);
        b.add(t1);

        // T2 : dominé par T1 => ex. arrMins=210, changes=3
        long t2 = PackedCriteria.pack(210, 3, 1111);
        b.add(t2);

        ParetoFront pf = b.build();
        assertEquals(1, pf.size(), "Un tuple dominé ne doit pas être inséré");

        long[] frontier = frontierToLongArray(pf);
        assertEquals(t1, frontier[0], "Seul le premier tuple doit subsister");
    }

    @Test
    void addDominatingTupleRemovesOldOne() {
        Builder b = new Builder();
        // T1 : arrMins=200, changes=2
        long t1 = PackedCriteria.pack(200, 2, 9999);
        b.add(t1);

        // T2 : arrMins=190, changes=2 => domine T1
        long t2 = PackedCriteria.pack(190, 2, 1234);
        b.add(t2);

        ParetoFront pf = b.build();
        assertEquals(1, pf.size(), "Le nouveau tuple dominant doit remplacer l'ancien");

        long[] frontier = frontierToLongArray(pf);
        assertEquals(t2, frontier[0], "Le tuple dominé aurait dû être supprimé");
    }

    @Test
    void addMoreThanInitialCapacity() {
        Builder b = new Builder();
        // On ajoute 5 tuples distincts pour forcer le redimensionnement
        for (int i = 0; i < 5; i++) {
            b.add(100 + i, 2, i);
        }
        ParetoFront pf = b.build();
        assertEquals(5, pf.size(), "On devrait avoir 5 tuples distincts dans la frontière");
    }

    @Test
    void checkLexicographicOrderInFinalArray() {
        Builder b = new Builder();
        long t1 = PackedCriteria.pack(150, 3, 999);
        long t2 = PackedCriteria.pack(120, 4, 111);
        long t3 = PackedCriteria.pack(180, 2, 222);

        // Ajout dans un ordre aléatoire
        b.add(t3);
        b.add(t1);
        b.add(t2);

        ParetoFront pf = b.build();
        long[] frontier = frontierToLongArray(pf);

        // Le tableau final doit être trié (ordre naturel sur long).
        // On peut vérifier en comparant à une version triée
        long[] sorted = frontier.clone();
        java.util.Arrays.sort(sorted);
        assertArrayEquals(sorted, frontier,
                "Le tableau final n'est pas trié par ordre naturel sur les longs");
    }

    @Test
    void addAllMergesTwoBuilders() {
        Builder b1 = new Builder();
        b1.add(150, 3, 999);
        b1.add(180, 2, 222);

        Builder b2 = new Builder();
        b2.add(120, 4, 111);
        b2.add(200, 2, 777);

        b1.addAll(b2);
        ParetoFront pf = b1.build();
        assertTrue(pf.size() > 0, "Après fusion, la frontière ne doit pas être vide");
    }

    @Test
    void testFullyDominatesTrue() {
        Builder b1 = new Builder();
        // T1 très bon => arrMins=100, changes=1
        b1.add(100, 1, 0);

        Builder b2 = new Builder();
        // T2 moins bon => arrMins=120, changes=2
        b2.add(120, 2, 0);
        b2.add(130, 1, 0);

        // fullyDominates => b1 doit dominer tout b2 en fixant depMins=0
        assertTrue(b1.fullyDominates(b2, 0), //impossible car il faudrait pouvoir mettre une depMin de 3855 pour avoir 12 bits de 0, car valeur inversée.
                "b1 devrait dominer tous les tuples de b2");
    }

    @Test
    void testFullyDominatesFalse() {
        Builder b1 = new Builder();
        // T1 => arrMins=200, changes=2
        b1.add(200, 2, 0);

        Builder b2 = new Builder();
        // T2 => arrMins=190, changes=2 (arrive plus tôt => meilleur)
        b2.add(190, 2, 0);

        // b1 ne peut pas dominer b2
        assertFalse(b1.fullyDominates(b2, 0), //impossible car il faudrait pouvoir mettre une depMin de 3855 pour avoir 12 bits de 0, car valeur inversée.
                "b1 ne devrait pas dominer le tuple de b2 qui est meilleur");
    }
}
