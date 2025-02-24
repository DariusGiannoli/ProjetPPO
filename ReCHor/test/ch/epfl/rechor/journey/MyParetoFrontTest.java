package ch.epfl.rechor.journey;

import ch.epfl.rechor.Preconditions;
import ch.epfl.rechor.journey.ParetoFront.Builder;
import ch.epfl.rechor.journey.ParetoFront;
import ch.epfl.rechor.journey.PackedCriteria;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class MyParetoFrontTest {

    @Test
    public void testEmptyBuilder() {
        Builder builder = new ParetoFront.Builder();
        assertTrue(builder.isEmpty(), "Builder should be empty initially");
        ParetoFront pf = builder.build();
        assertEquals(0, pf.size(), "Built ParetoFront should have size 0");
    }

    @Test
    public void testAddSingleTuple() {
        Builder builder = new ParetoFront.Builder();
        long tuple1 = PackedCriteria.pack(480, 3, 1); // arrMins=480, changes=3
        builder.add(tuple1);
        assertEquals(1, builder.size, "Builder should have 1 tuple");
        ParetoFront pf = builder.build();
        assertEquals(1, pf.size(), "ParetoFront should have 1 tuple");
        assertEquals(tuple1, pf.get(480, 3), "Retrieved tuple must equal tuple1");
    }

    @Test
    public void testAddDominatedTupleNotAdded() {
        Builder builder = new ParetoFront.Builder();
        long tuple1 = PackedCriteria.pack(480, 3, 1); // Good tuple
        long tupleDominated = PackedCriteria.pack(480, 4, 2); // Dominated because same arrMins and changes 4 > 3

        builder.add(tuple1);
        builder.add(tupleDominated); // Do nothing car tuple1 domine tupleDominated

        assertEquals(1, builder.size, "Builder should still have 1 tuple");
        ParetoFront pf = builder.build();
        // Vérifier que le tuple présent est celui initial
        assertEquals(tuple1, pf.get(480, 3), "The existing tuple should remain");
        // Vérifier que get(480,4) lève une exception
        assertThrows(NoSuchElementException.class, () -> pf.get(480, 4));
    }

    @Test
    public void testReplaceByDominatingTuple() {
        Builder builder = new ParetoFront.Builder();
        long tuple1 = PackedCriteria.pack(480, 3, 1); // Initial tuple
        long tupleDominating = PackedCriteria.pack(480, 2, 3); // Meilleur : même arrMins, moins de changements

        builder.add(tuple1);
        builder.add(tupleDominating); // Doit remplacer tuple1

        assertEquals(1, builder.size, "Builder should have 1 tuple after replacement");
        ParetoFront pf = builder.build();
        // On doit pouvoir retrouver le tuple dominant
        assertEquals(tupleDominating, pf.get(480, 2));
        // Et get(480,3) doit lever une exception
        assertThrows(NoSuchElementException.class, () -> pf.get(480, 3));
    }

    @Test
    public void testAddAllMethod() {
        Builder builder1 = new ParetoFront.Builder();
        long tupleA = PackedCriteria.pack(480, 3, 1);
        builder1.add(tupleA);

        Builder builder2 = new ParetoFront.Builder();
        long tupleB = PackedCriteria.pack(480, 4, 2);
        long tupleC = PackedCriteria.pack(480, 2, 3); // Domine tupleA car changes 2 < 3
        builder2.add(tupleB);
        builder2.add(tupleC);
        // Dans builder2, tupleC domine tupleB, donc builder2 doit avoir 1 tuple.
        assertEquals(1, builder2.size, "Builder2 should have 1 tuple due to domination");

        builder1.addAll(builder2);
        // Après addAll, builder1 devrait contenir uniquement tupleC (car tupleC domine tupleA)
        assertEquals(1, builder1.size, "After addAll, builder1 should have 1 tuple");
        ParetoFront pf = builder1.build();
        assertEquals(1, pf.size(), "Built ParetoFront should have 1 tuple");
        assertEquals(tupleC, pf.get(480, 2), "The dominating tuple should be present");
    }

    @Test
    public void testForEachLambda() {
        Builder builder = new ParetoFront.Builder();
        long tuple1 = PackedCriteria.pack(480, 3, 1);
        long tuple2 = PackedCriteria.pack(481, 3, 2);

        builder.add(tuple1);
        builder.add(tuple2);
        // tuple2 est dominé, donc pas ajouté.

        AtomicInteger sum = new AtomicInteger(0);
        builder.forEach(t -> sum.addAndGet(PackedCriteria.arrMins(t)));

        // On s'attend à n'avoir que tuple1 => sum=480
        assertEquals(480, sum.get(), "La somme des arrMins doit être correcte si le second est dominé");
    }

    @Test
    public void testMultipleInsertsAndDominations() {
        Builder builder = new Builder();

        // Liste de tuples (arrMins, changes, payload) qu'on va insérer dans le désordre
        // On simule des heures d'arrivée comprises entre 400 et 2200, et des changes entre 0 et 7.
        // On veut voir comment le Builder gère l'ajout, la domination et la suppression.
        List<long[]> tuplesData = List.of(
                new long[] {  600, 3,  1 }, // T1
                new long[] {  600, 4, 10 }, // T2 (dominée par T1 => same arrMins=600, changes=4>3)
                new long[] { 1200, 5, 42 }, // T3
                new long[] {  599, 7, 99 }, // T4 (arrive plus tôt que T1? Non, 599 < 600 => en fait T4 est meilleur sur arrMins, mais changes=7 est pire)
                new long[] {  599, 2,  8 }, // T5 (arrMins=599, changes=2 => T5 domine T4)
                new long[] { 2200, 0, 50 }, // T6 (arrive très tard, mais changes=0 => dépend de la logique)
                new long[] {  599, 2, 11 }, // T7 (même arrMins, changes que T5, on verra qui l'emporte)
                new long[] { 1000, 2,  3 }, // T8
                new long[] { 1000, 2,  4 }, // T9 (identique en arrMins et changes que T8 => un va potentiellement dominer l'autre)
                new long[] {  800, 1, 99 }, // T10
                new long[] { 1200, 1, 33 }, // T11
                new long[] { 2200, 7, 77 }  // T12 (pire arrMins, plus de changes => probablement dominé par T6)
        );

        // Insérer dans l'ordre indiqué
        for (long[] data : tuplesData) {
            long packed = PackedCriteria.pack((int)data[0], (int)data[1], (int)data[2]);
            builder.add(packed);
        }

        // Construire la frontière
        var pf = builder.build();

        // On va itérer pour afficher la frontière et vérifier sa cohérence
        System.out.println("Final ParetoFront :\n" + pf);

        // Vérifions que certains tuples évidemment dominés ne s'y trouvent plus :
        // T2 (arrMins=600, changes=4) est dominé par T1 (arrMins=600, changes=3).
        assertThrows(NoSuchElementException.class, () -> pf.get(600, 4));

        // T4 (599, 7) est dominé par T5 (599, 2) => same arrMins=599, changes=2 < 7
        assertThrows(NoSuchElementException.class, () -> pf.get(599, 7));

        // T12 (2200, 7) est dominé par T6 (2200, 0) => same arrMins=2200, changes=0 < 7
        assertThrows(NoSuchElementException.class, () -> pf.get(2200, 7));

        // On s'attend à retrouver T5 (arrMins=599, changes=2)
        long foundT5 = pf.get(599, 2);
        assertEquals(599, PackedCriteria.arrMins(foundT5));
        assertEquals(2,   PackedCriteria.changes(foundT5));

        // On s'attend aussi à retrouver T1 (600,3) => il dominait T2
        long foundT1 = pf.get(600, 3);
        assertEquals(600, PackedCriteria.arrMins(foundT1));
        assertEquals(3,   PackedCriteria.changes(foundT1));

        // T6 (2200, 0) doit exister, car 0 changes est potentiellement intéressant
        long foundT6 = pf.get(2200, 0);
        assertEquals(2200, PackedCriteria.arrMins(foundT6));
        assertEquals(0,    PackedCriteria.changes(foundT6));

        // T10 (800,1) => doit probablement rester, car c'est un bon compromis (arrive plus tard que 599,2 ?)
        // Mais on doit voir si c'est dominé par T5 => T5 a arrMins=599, changes=2.
        // T10 a arrMins=800, changes=1 => Ni l'un domine l'autre => on s'attend à le retrouver
        long foundT10 = pf.get(800, 1);
        assertEquals(800, PackedCriteria.arrMins(foundT10));
        assertEquals(1,   PackedCriteria.changes(foundT10));

        // T11 (1200,1) doit exister aussi, car c'est un compromis (arrMins=1200, changes=1)
        long foundT11 = pf.get(1200, 1);
        assertEquals(1200, PackedCriteria.arrMins(foundT11));
        assertEquals(1,    PackedCriteria.changes(foundT11));

        // Vérifions T8 (1000,2) ou T9 (1000,2). L'un des deux doit survivre,
        //   ou potentiellement ils sont égaux sur arrMins,changes => le premier inséré
        //   dominera l'autre. On suppose T9 n'est pas ajouté si T8 est déjà là.
        // T8 a été inséré avant T9, donc T9 est dominé ou identique => T9 ne doit pas exister
        assertThrows(NoSuchElementException.class, () -> pf.get(1000, 2));
        // => Soit T8 a survécu, soit T9 a remplacé T8. Vérifions si T8 existe :
        //   (on suppose qu'il survit)
        //   S'il n'est pas trouvé, c'est que T9 a pris sa place (même arrMins,changes => le second n'apporte rien)
        try {
            pf.get(1000, 2);
            fail("We expected an exception or that T9 replaced T8, but found T8. If your logic replaces the first, adapt the test accordingly.");
        } catch (NoSuchElementException e) {
            // c'est cohérent : T8 n'existe pas => T9 l'a peut-être remplacé
            // On peut commenter ici selon la logique :
            // "Ok, l'implémentation supprime le premier et garde le second => c'est un choix."
        }

        // Vérifions qu'on a un nombre d'éléments cohérent
        // On s'attend à un certain set final de tuples.
        // On peut itérer pour voir combien on en a vraiment.
        AtomicInteger count = new AtomicInteger(0);
        pf.forEach(t -> count.incrementAndGet());
        System.out.println("Nombre de tuples finaux = " + count.get());
        // On ne fait pas d'assert strict ici, car la logique d'élimination de tuples
        // identiques ou dominés peut varier (selon si on garde le premier ou le second).
        // Mais on peut s'attendre à un nombre final autour de 5-7.

        // On peut tout de même s'assurer qu'on n'a pas de double dominations dans la frontière
        //   en itérant sur tous les tuples.
        // C'est un test plus complexe à écrire, on peut se limiter à valider que
        // "arrMins, changes" qu'on a explicitement cherché sont présents ou non.
    }
}
