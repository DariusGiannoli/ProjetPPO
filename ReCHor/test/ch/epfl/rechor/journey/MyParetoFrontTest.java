package ch.epfl.rechor.journey;

import ch.epfl.rechor.Bits32_24_8;
import ch.epfl.rechor.Preconditions;
import ch.epfl.rechor.journey.ParetoFront.Builder;
import ch.epfl.rechor.journey.ParetoFront;
import ch.epfl.rechor.journey.PackedCriteria;
import org.junit.jupiter.api.Test;
import org.w3c.dom.ls.LSOutput;

import java.util.*;


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
    void addDominatedTuple() {
        Builder b = new Builder();
        // On ajoute 5 tuples mais 4 sont dominés par le premier donc pas ajoutés.
        for (int i = 0; i < 5; i++) {
            b.add(100 + i, 2, i);
        }
        ParetoFront pf = b.build();
        assertEquals(1, pf.size(), "On devrait avoir 1 tuples distincts dans la frontière");
    }

    @Test
    void addMoreThanInitialCapacity() {
        Builder b = new Builder();
        // On ajoute 5 tuples distincts pour forcer le redimensionnement
        for (int i = 0; i < 5; i++) {
            b.add(100 - i, 2 + i, i); // on ajoute des elements qui ne se dominent pas entre eux en ayant un meilleur heure d'arrivée mais un moins bon nombre de changements.
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

    /*@Test
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

     */

    @Test
    void testFullyDominatesTrue() {
        Builder b1 = new Builder();
        // T1 très bon => arrMins=100, changes=1
        long criteria = PackedCriteria.pack(100, 1, 0);
        b1.add(PackedCriteria.withDepMins(criteria, 0));

        Builder b2 = new Builder();
        // T2 moins bon => arrMins=120, changes=2
        b2.add(120, 2, 0);
        b2.add(130, 1, 0);

        // fullyDominates => b1 doit dominer tout b2 en fixant depMins=0
        assertTrue(b1.fullyDominates(b2, 0),
                "b1 devrait dominer tous les tuples de b2");
    }

    @Test
    void testFullyDominatesFalse() {
        Builder b1 = new Builder();
        // T1 => arrMins=200, changes=2
        long criteria = PackedCriteria.pack(200, 2, 0);
        b1.add(PackedCriteria.withDepMins(criteria, 0));

        Builder b2 = new Builder();
        // T2 => arrMins=190, changes=2 (arrive plus tôt => meilleur)
        b2.add(190, 2, 0);

        // fullyDominates => b1 doit dominer tout b2 en fixant depMins=0
        assertFalse(b1.fullyDominates(b2, 0),
                "b1 ne devrait pas dominer le tuple de b2 qui est meilleur");
    }

    @Test
    void addDominatingTupleRemovesOldOnes() {
        Builder b = new Builder();
        // T1 : arrMins=200, changes=2
        long t1 = PackedCriteria.pack(200, 5, 9999);
        long t2 = PackedCriteria.pack(201, 4, 9999);
        long t3 = PackedCriteria.pack(202, 3, 9999);

        b.add(t1);
        b.add(t2);
        b.add(t3);

        // Tç : arrMins=190, changes=2 => domine T1
        long t4 = PackedCriteria.pack(200, 4, 9999);
        b.add(t4);

        ParetoFront pf = b.build();
        assertEquals(2, pf.size());

        long[] frontier = frontierToLongArray(pf);
        assertEquals(t4, frontier[0]);
        assertEquals(t3, frontier[1]);
    }

    @Test
    void addDominatingTupleRemovesOldOne2() {
        Builder b = new Builder();
        long t1 = PackedCriteria.pack(200, 5, 9999);
        long t2 = PackedCriteria.pack(201, 4, 9999);
        long t3 = PackedCriteria.pack(202, 3, 9999);

        b.add(t1);
        b.add(t2);
        b.add(t3);

        long t4 = PackedCriteria.pack(202, 2, 9999);
        b.add(t4);

        ParetoFront pf = b.build();
        assertEquals(3, pf.size());

        long[] frontier = frontierToLongArray(pf);
        assertEquals(t1, frontier[0]);
        assertEquals(t2, frontier[1]);
        assertEquals(t4, frontier[2]);
    }

    @Test
    void addDominatingTupleRemovesOldOneInTheMiddle() {
        Builder b = new Builder();
        long t1 = PackedCriteria.pack(200, 5, 9999);
        long t2 = PackedCriteria.pack(201, 4, 9999);
        long t3 = PackedCriteria.pack(202, 2, 9999);

        b.add(t1);
        b.add(t2);
        b.add(t3);

        long t4 = PackedCriteria.pack(201, 3, 9999);
        b.add(t4);

        ParetoFront pf = b.build();
        assertEquals(3, pf.size());

        long[] frontier = frontierToLongArray(pf);
        assertEquals(t1, frontier[0]);
        assertEquals(t4, frontier[1]);
        assertEquals(t3, frontier[2]);
    }

    @Test
    void addTupleNotRemovesOldOne() {
        Builder b = new Builder();
        long t1 = PackedCriteria.pack(200, 5, 9999);
        long t2 = PackedCriteria.pack(201, 4, 9999);
        long t3 = PackedCriteria.pack(202, 2, 9999);

        b.add(t1);
        b.add(t2);
        b.add(t3);

        long t4 = PackedCriteria.pack(203, 1, 9999);
        b.add(t4);

        ParetoFront pf = b.build();
        assertEquals(4, pf.size());

        long[] frontier = frontierToLongArray(pf);
        assertEquals(t1, frontier[0]);
        assertEquals(t2, frontier[1]);
        assertEquals(t3, frontier[2]);
        assertEquals(t4, frontier[3]);
    }

    @Test
    void addSingleTuple0() {
        Builder b = new Builder();
        assertTrue(b.isEmpty());

        // Ajout d'un tuple (arrMins=100, changes=2, payload=1234)
        b.add(-240, 0, 0);

        ParetoFront pf = b.build();
        assertFalse(b.isEmpty(), "Le builder ne doit plus être vide après un ajout");
        assertEquals(1, pf.size(), "La frontière devrait contenir exactement 1 élément");

        long[] frontier = frontierToLongArray(pf);
        long expected = PackedCriteria.pack(-240, 0, 0);
        assertEquals(expected, frontier[0], "Le tuple stocké n'est pas celui attendu");
    }

    @Test
    void addTuple0NotRemovesOldOne() {
        Builder b = new Builder();
        long t1 = PackedCriteria.pack(200, 5, 9999);
        long t2 = PackedCriteria.pack(201, 4, 9999);
        long t3 = PackedCriteria.pack(202, 2, 9999);

        b.add(t1);
        b.add(t2);
        b.add(t3);

        long t4 = PackedCriteria.pack(-240, 0, 0);
        b.add(t4);

        ParetoFront pf = b.build();
        assertEquals(1, pf.size());

        long[] frontier = frontierToLongArray(pf);
        assertEquals(t4, frontier[0]);
    }

    @Test
    void testGet() {
        Builder b = new Builder();
        long t1 = PackedCriteria.pack(200, 5, 9999);
        long t2 = PackedCriteria.pack(201, 4, 9999);
        long t3 = PackedCriteria.pack(202, 2, 9999);

        b.add(t1);
        b.add(t2);
        b.add(t3);
        long expected = (441L << 39) | (4L << 32) | 9999L;
        ParetoFront p = b.build();

        assertEquals(expected, p.get(201, 4));
    }

    @Test
    void testGetException() {
        Builder b = new Builder();
        long t1 = PackedCriteria.pack(200, 5, 9999);
        long t2 = PackedCriteria.pack(201, 4, 9999);
        long t3 = PackedCriteria.pack(202, 2, 9999);

        b.add(t1);
        b.add(t2);
        b.add(t3);
        ParetoFront p = b.build();

        assertThrows(NoSuchElementException.class, () -> {
            p.get(202, 3);
        });
    }

    @Test
    void testClear() {
        Builder b = new Builder();
        long t1 = PackedCriteria.pack(200, 5, 9999);
        long t2 = PackedCriteria.pack(201, 4, 9999);
        long t3 = PackedCriteria.pack(202, 2, 9999);

        b.add(t1);
        b.add(t2);
        b.add(t3);

        b.clear();
        ParetoFront pf = b.build();
        assertEquals(0, pf.size());

    }

    @Test
    void testAddAll() {
        Builder b = new Builder();
        Builder c = new Builder();
        long t1 = PackedCriteria.pack(200, 5, 9999);
        long t2 = PackedCriteria.pack(201, 4, 9999);
        long t3 = PackedCriteria.pack(202, 2, 9999);

        b.add(t1);
        b.add(t2);
        c.add(t3);

        c.addAll(b);

        ParetoFront pf = c.build();
        assertEquals(3, pf.size());

        long[] frontier = frontierToLongArray(pf);
        assertEquals(t1, frontier[0]);
        assertEquals(t2, frontier[1]);
        assertEquals(t3, frontier[2]);
    }


    @Test
    void testAddAllEmptyBuilder() {
        Builder b = new Builder();
        Builder c = new Builder();
        long t1 = PackedCriteria.pack(200, 5, 9999);
        long t2 = PackedCriteria.pack(201, 4, 9999);
        long t3 = PackedCriteria.pack(202, 2, 9999);

        b.add(t1);
        b.add(t2);
        b.add(t3);

        c.addAll(b);

        ParetoFront pf = c.build();
        assertEquals(3, pf.size());

        long[] frontier = frontierToLongArray(pf);
        assertEquals(t1, frontier[0]);
        assertEquals(t2, frontier[1]);
        assertEquals(t3, frontier[2]);
    }

    @Test
    void testAddAllEmptyBuilderAdded() {
        Builder b = new Builder();
        Builder c = new Builder();
        long t1 = PackedCriteria.pack(200, 5, 9999);
        long t2 = PackedCriteria.pack(201, 4, 9999);
        long t3 = PackedCriteria.pack(202, 2, 9999);

        b.add(t1);
        b.add(t2);
        b.add(t3);

        c.addAll(b);

        ParetoFront pf = b.build();
        assertEquals(3, pf.size());

        long[] frontier = frontierToLongArray(pf);
        assertEquals(t1, frontier[0]);
        assertEquals(t2, frontier[1]);
        assertEquals(t3, frontier[2]);
    }

    @Test
    void testAddAllDominantTuples() {
        Builder b = new Builder();
        Builder c = new Builder();
        long t1 = PackedCriteria.pack(200, 5, 9999);
        long t2 = PackedCriteria.pack(201, 3, 9999);
        long t3 = PackedCriteria.pack(200, 4, 9999);
        long t4 = PackedCriteria.pack(202, 1, 9999);


        b.add(t1);
        b.add(t2);
        c.add(t3);
        c.add(t4);

        c.addAll(b);

        ParetoFront pf = c.build();
        assertEquals(3, pf.size());

        long[] frontier = frontierToLongArray(pf);
        assertEquals(t3, frontier[0]);
        assertEquals(t2, frontier[1]);
        assertEquals(t4, frontier[2]);
    }
    @Test
    void testAddAllDominatedTuples() {
        Builder b = new Builder();
        Builder c = new Builder();
        long t1 = PackedCriteria.pack(200, 5, 9999);
        long t2 = PackedCriteria.pack(201, 3, 9999);
        long t3 = PackedCriteria.pack(201, 4, 9999);
        long t4 = PackedCriteria.pack(202, 1, 9999);


        b.add(t1);
        b.add(t2);
        c.add(t3);
        c.add(t4);

        c.addAll(b);

        ParetoFront pf = c.build();
        assertEquals(3, pf.size());

        long[] frontier = frontierToLongArray(pf);
        assertEquals(t1, frontier[0]);
        assertEquals(t2, frontier[1]);
        assertEquals(t4, frontier[2]);
    }

    @Test
    void testFullyDominatesEmpty() {
        Builder b1 = new Builder();
        long criteria = PackedCriteria.pack(200, 2, 0);
        b1.add(PackedCriteria.withDepMins(criteria, 0));

        Builder b2 = new Builder();

        assertTrue(b1.fullyDominates(b2, 0));
    }

    @Test
    void testFullyDominatesEmptyB2() {
        Builder b1 = new Builder();
        long criteria = PackedCriteria.pack(200, 2, 0);
        b1.add(criteria);

        Builder b2 = new Builder();

        assertFalse(b2.fullyDominates(b1, 0));
    }

    @Test
    void testFullyDominatesWithMoreElementsFalse() {
        Builder b1 = new Builder();
        long criteria = PackedCriteria.pack(200, 2, 0);
        b1.add(PackedCriteria.withDepMins(criteria, 0));

        Builder b2 = new Builder();
        b2.add(190, 2, 0);
        b2.add(210, 2, 0);

        assertFalse(b1.fullyDominates(b2, 0));
    }

    @Test
    void testFullyDominatesWithMoreElementsFalse2() {
        Builder b1 = new Builder();
        long criteria = PackedCriteria.pack(100, 1, 0);
        b1.add(PackedCriteria.withDepMins(criteria, 0));

        Builder b2 = new Builder();
        b2.add(120, 2, 0);
        b2.add(90, 1, 0);

        assertFalse(b1.fullyDominates(b2, 0));
    }

    @Test
    void testFullyDominatesWithMoreElementsTrue() {
        Builder b1 = new Builder();
        long criteria = PackedCriteria.pack(100, 1, 0);
        b1.add(PackedCriteria.withDepMins(criteria, 0));
        long criteria2 = PackedCriteria.pack(125, 1, 0);
        b1.add(PackedCriteria.withDepMins(criteria2, 0));

        Builder b2 = new Builder();
        b2.add(120, 2, 0);
        b2.add(130, 1, 0);

        assertTrue(b1.fullyDominates(b2, 0));
    }

    @Test
    void testFullyDominatesOnlyOneElementFalse() {
        Builder b1 = new Builder();
        long criteria = PackedCriteria.pack(100, 2, 0);
        b1.add(PackedCriteria.withDepMins(criteria, 0));

        Builder b2 = new Builder();
        b2.add(120, 1, 0);
        b2.add(130, 2, 0);

        assertFalse(b1.fullyDominates(b2, 0));
    }

    @Test
    void testAddSameValues() {
        Builder b = new Builder();
        long t1 = PackedCriteria.pack(201, 4, 9998);
        long t2 = PackedCriteria.pack(200, 5, 9999);
        long t3 = PackedCriteria.pack(203, 4, 9997);

        b.add(t1);
        b.add(t2);
        b.add(t3);

        ParetoFront pf = b.build();
        assertEquals(2, pf.size());
        long[] frontier = frontierToLongArray(pf);
        assertEquals(t1, frontier[1]);
        assertEquals(t2, frontier[0]);
    }

    @Test
    void testAddWithDepMinsOrder() {
        Builder b = new Builder();
        long t1 = PackedCriteria.pack(201, 4, 9998);
        long t2 = PackedCriteria.pack(200, 5, 9999);
        long t3 = PackedCriteria.pack(201, 4, 9997);

        b.add(PackedCriteria.withDepMins(t1, 240));
        b.add(PackedCriteria.withDepMins(t2, 240));
        b.add(PackedCriteria.withDepMins(t3, 250));

        ParetoFront pf = b.build();
        assertEquals(2, pf.size());
        long[] frontier = frontierToLongArray(pf);
        assertEquals(PackedCriteria.withDepMins(t3, 250), frontier[0]);
        assertEquals(PackedCriteria.withDepMins(t2, 240), frontier[1]);
    }


    @Test
    void testAddWithDepMinsOrder2() {
        Builder b = new Builder();
        long t1 = PackedCriteria.pack(201, 3, 9998);
        long t2 = PackedCriteria.pack(201, 5, 9999);
        long t3 = PackedCriteria.pack(201, 4, 9997);

        b.add(PackedCriteria.withDepMins(t1, 240));
        b.add(PackedCriteria.withDepMins(t2, 240));
        b.add(PackedCriteria.withDepMins(t3, 250));

        ParetoFront pf = b.build();
        assertEquals(2, pf.size());
        long[] frontier = frontierToLongArray(pf);
        assertEquals(PackedCriteria.withDepMins(t3, 250), frontier[0]);
        assertEquals(PackedCriteria.withDepMins(t1, 240), frontier[1]);
    }

    @Test
    void testAddWithDepMinsOrder3() {
        Builder b = new Builder();
        long t1 = PackedCriteria.pack(201, 3, 9998);
        long t2 = PackedCriteria.pack(200, 5, 9999);
        long t3 = PackedCriteria.pack(201, 4, 9997);
        long t4 = PackedCriteria.pack(200, 5, 9997);

        b.add(PackedCriteria.withDepMins(t1, 240));
        b.add(PackedCriteria.withDepMins(t2, 240));
        b.add(PackedCriteria.withDepMins(t3, 250));
        b.add(PackedCriteria.withDepMins(t4, 260));

        ParetoFront pf = b.build();
        assertEquals(3, pf.size());
        long[] frontier = frontierToLongArray(pf);
        assertEquals(PackedCriteria.withDepMins(t4, 260), frontier[0]);
        assertEquals(PackedCriteria.withDepMins(t3, 250), frontier[1]);
        assertEquals(PackedCriteria.withDepMins(t1, 240), frontier[2]);
    }



    @Test
    void testAddWithDepMinsError() {
        Builder b = new Builder();
        long t1 = PackedCriteria.pack(201, 4, 9998);
        //long t2 = PackedCriteria.pack(200, 5, 9999);
        long t3 = PackedCriteria.pack(201, 4, 9997);

        b.add(PackedCriteria.withDepMins(t1, 240));
        //b.add(PackedCriteria.withDepMins(t2, 240));
        b.add(PackedCriteria.withDepMins(t3, 250));

        ParetoFront pf = b.build();
        assertEquals(1, pf.size());
        long[] frontier = frontierToLongArray(pf);
        //assertEquals(PackedCriteria.withDepMins(t2, 240), frontier[1]);
        assertEquals(PackedCriteria.withDepMins(t3, 250), frontier[0]);
    }

    @Test
    void testAddWithDepMins() {
        Builder b = new Builder();
        long t1 = PackedCriteria.pack(201, 4, 9998);
        long t2 = PackedCriteria.pack(202, 3, 9999);
        long t3 = PackedCriteria.pack(201, 4, 9997);

        b.add(PackedCriteria.withDepMins(t1, 240));
        b.add(PackedCriteria.withDepMins(t2, 240));
        b.add(PackedCriteria.withDepMins(t3, 250));

        ParetoFront pf = b.build();
        assertEquals(2, pf.size());
        long[] frontier = frontierToLongArray(pf);
        //assertEquals(PackedCriteria.withDepMins(t2, 240), frontier[1]);
        assertEquals(PackedCriteria.withDepMins(t3, 250), frontier[0]);
    }
    @Test
    void testAddWithDepMinsClear() {
        Builder b = new Builder();
        long t1 = PackedCriteria.pack(201, 4, 9998);
        long t2 = PackedCriteria.pack(202, 3, 9999);
        long t3 = PackedCriteria.pack(201, 4, 9997);
        long t4 = PackedCriteria.pack(202, 2, 9997);
        long t44 = PackedCriteria.pack(202, 1, 9997);

        b.add(PackedCriteria.withDepMins(t1, 240));
        b.add(PackedCriteria.withDepMins(t2, 240));
        b.add(PackedCriteria.withDepMins(t3, 250));
        b.add(PackedCriteria.withDepMins(t4, 240));
        b.add(PackedCriteria.withDepMins(t44, 240));
        b.clear();

        long t5 = PackedCriteria.pack(203, 4, 9998);
        long t6 = PackedCriteria.pack(204, 3, 9999);
        b.add(PackedCriteria.withDepMins(t5, 230));
        b.add(PackedCriteria.withDepMins(t6, 230));
        ParetoFront pf = b.build();
        assertEquals(2, pf.size());
        long[] frontier = frontierToLongArray(pf);
        //assertEquals(PackedCriteria.withDepMins(t2, 240), frontier[1]);
        assertEquals(PackedCriteria.withDepMins(t5, 230), frontier[0]);
        assertEquals(PackedCriteria.withDepMins(t6, 230), frontier[1]);
    }

    @Test
    void removeManyBd() {
        Builder builder = new Builder();
        for (int i = 1; i <= 100; i++) {
            long t = PackedCriteria.pack(100-i, i, 0);
            builder.add(t);
            ParetoFront paretoFront = builder.build();
            assertEquals(i, paretoFront.size());
        }
        long t = PackedCriteria.pack(0, 0, 0);
        builder.add(t);
        ParetoFront paretoFront = builder.build();
        assertEquals(1, paretoFront.size());
    }


    @Test
    void repetitionsAreNotOkayBd() {
        Builder builder = new Builder();
        for (int i = 1; i <= 100; i++) {
            long t = PackedCriteria.pack(1, 1, 0);
            builder.add(t);
            ParetoFront paretoFront = builder.build();
            assertEquals(1, paretoFront.size());
        }
        long t = PackedCriteria.pack(0, 0, 0);
    }


    @Test
    void waitIsThatSoloLevelingBd() {
        Builder builder = new Builder();
        for (int i = 10; i>= 1; i--) {
            for (int j = 1; j <= 10; j++) {
                long t = PackedCriteria.pack(10*i + 10-j, 10*i + j, 0);
                builder.add(t);
                ParetoFront paretoFront = builder.build();
                assertEquals(j, paretoFront.size());
            }
        }
    }


    @Test
    void killingChainBd() {
        Builder builder = new Builder();
        for (int i = 1000; i >= 1; i--) {
            long t = PackedCriteria.pack(i, 1, 0);
            builder.add(t);
            ParetoFront paretoFront = builder.build();
            assertEquals(1, paretoFront.size());
        }
    }


    @Test
    void aThousandUselessTriesBd() {
        Builder builder = new Builder();
        long t0 = PackedCriteria.pack(-240, 0, 0);
        builder.add(t0);
        Random random = new Random(0);
        for (int i = 0; i <= 1000; i++) {
            long t = PackedCriteria.pack(random.nextInt(2280+240) - 240, random.nextInt(127), 0);
            builder.add(t);
            ParetoFront paretoFront = builder.build();
            assertEquals(1, paretoFront.size());
        }
    }


    @Test
    void outputInjection0Bd() {
        Builder builder = new Builder();
        Random random = new Random(0);
        long[] expectedValues = {22643067584512L, 335930867056640L, 1031917432471552L};
        int expectedLength = 3;
        for (int i = 0; i <= 10; i++) {
            long t = PackedCriteria.pack(random.nextInt(2280+240) - 240, random.nextInt(127), 0);
            builder.add(t);
        }


//        System.out.println(builder);


        ParetoFront paretoFront = builder.build();
        assertEquals(expectedLength, paretoFront.size());
        long[] frontier = frontierToLongArray(paretoFront);
        for (int i = 0; i < expectedLength; i++) {
            assertEquals(expectedValues[i], frontier[i]);
        }
    }


    @Test
    void outputInjection1Bd() {
        Builder builder = new Builder();
        Random random = new Random(1);
        long[] expectedValues = {69831873265664L, 559651418537984L};
        int expectedLength = 2;
        for (int i = 0; i <= 10; i++) {
            long t = PackedCriteria.pack(random.nextInt(2280+240) - 240, random.nextInt(127), 0);
            builder.add(t);
        }


//        System.out.println(builder);


        ParetoFront paretoFront = builder.build();
        assertEquals(expectedLength, paretoFront.size());
        long[] frontier = frontierToLongArray(paretoFront);
        for (int i = 0; i < expectedLength; i++) {
            assertEquals(expectedValues[i], frontier[i]);
        }
    }


    @Test
    void outputInjection2Bd() {
        Builder builder = new Builder();
        Random random = new Random(2);
        long[] expectedValues = {110015587287040L, 900555857723392L};
        int expectedLength = 2;
        for (int i = 0; i <= 10; i++) {
            long t = PackedCriteria.pack(random.nextInt(2280+240) - 240, random.nextInt(127), 0);
            builder.add(t);
        }


//        System.out.println(builder);


        ParetoFront paretoFront = builder.build();
        assertEquals(expectedLength, paretoFront.size());
        long[] frontier = frontierToLongArray(paretoFront);
        for (int i = 0; i < expectedLength; i++) {
            assertEquals(expectedValues[i], frontier[i]);
        }
    }


    @Test
    void outputInjection3Bd() {
        Builder builder = new Builder();
        Random random = new Random(3);
        long[] expectedValues = {91800630984704L, 92547955294208L, 799387903066112L};
        int expectedLength = 3;
        for (int i = 0; i <= 10; i++) {
            long t = PackedCriteria.pack(random.nextInt(2280+240) - 240, random.nextInt(127), 0);
            builder.add(t);
        }


//        System.out.println(builder);


        ParetoFront paretoFront = builder.build();
        assertEquals(expectedLength, paretoFront.size());
        long[] frontier = frontierToLongArray(paretoFront);
        for (int i = 0; i < expectedLength; i++) {
            assertEquals(expectedValues[i], frontier[i]);
        }
    }



}
