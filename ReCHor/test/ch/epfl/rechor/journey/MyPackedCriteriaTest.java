package ch.epfl.rechor.journey;

import ch.epfl.rechor.Bits32_24_8;
import ch.epfl.rechor.PackedRange;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MyPackedCriteriaTest {

    @Test
    void testExceptionIfMoreThan7BitsInChanges() {

        int arrMins = 0b010010101001;
        int changes = 0b11010100;
        int payload = 1 << 31;

        assertThrows(IllegalArgumentException.class, () -> {
            PackedCriteria.pack(arrMins, changes, payload);
        });
    }
    @Test
    void testExceptionIfMoreThan12BitsInArrMins() {

        int arrMins = 0b1010010101001;
        int changes = 0b1010100;
        int payload = 1 << 31;

        assertThrows(IllegalArgumentException.class, () -> {
            PackedCriteria.pack(arrMins, changes, payload);
        });
    }

    /**
    @Test
    void testPack() {

        long arrMins = 0b010010101001L;
        long changes = 0b1010100L;
        long payload = 0xffffffffL;

        int arrMinsInt = 0b010010101001;
        int changesInt = 0b1010100;
        int payloadInt = 0xffffffff;


        long expected =  payload | changes << 32 | arrMins << 39;
        long value = PackedCriteria.pack(arrMinsInt, changesInt, payloadInt);

        System.out.println("Expected: " + Long.toBinaryString(expected));
        System.out.println("Actual:   " + Long.toBinaryString(value));

        assertEquals(Long.toBinaryString(expected), Long.toBinaryString(value));
    }
*/
    @Test
    void testPack() {
        // Suppose these are well within range
        int arrMinsInt = 0b010010101001; // 12 bits
        int changesInt = 0b1010100;      // 7 bits
        int payloadInt = 0xFFFFFFFF;

        // Build the "expected" using the same bit manipulation
        long expected = ((long) (arrMinsInt + 240) << 39)
                | ((long) changesInt << 32)
                | Integer.toUnsignedLong(payloadInt);

        long actual = PackedCriteria.pack(arrMinsInt, changesInt, payloadInt);

        assertEquals(Long.toBinaryString(expected), Long.toBinaryString(actual));
    }

    @Test
    void TestHasDepMinsTrue() {
        long criteria = 0b1l << 51;

        assertTrue(PackedCriteria.hasDepMins(criteria));
    }

    @Test
    void TestHasDepMinsFalse(){
        long criteria = 0b1l << 50;

        assertFalse(PackedCriteria.hasDepMins(criteria));
    }

    @Test
    void testExceptionDepMins() {
        long criteria = 0b1l << 50;

        assertThrows(IllegalArgumentException.class, () -> {
            PackedCriteria.depMins(criteria);
        });
    }

    @Test
    void testDepMins() {
        long criteria = 0b111100000101L << 51;

        int expected = 10;
        int value = PackedCriteria.depMins(criteria);
        assertEquals(expected, value);
    }

    @Test
    void testArrMins() {
        long criteria = 0b11000011111010L << 39;

        int expected = 10;
        int value = PackedCriteria.arrMins(criteria);
        assertEquals(expected, value);
    }

    @Test
    void testChanges() {
        long criteria = 0b110001101L << 32;

        int expected = 13;
        int value = PackedCriteria.changes(criteria);
        assertEquals(expected, value);
    }

    @Test
    void testPayload() {
        long criteria = 0b11L << 31;
        long expected = -2147483648L;

        int value = PackedCriteria.payload(criteria);
        assertEquals(expected, value);
    }

    @Test
    void testWithoutDepMins() {
        long criteria = 0b111001L << 51;

        long value = PackedCriteria.withoutDepMins(criteria);
        boolean val = PackedCriteria.hasDepMins(value);
        assertFalse(val);
    }

    @Test
    void testWithDepMins() {
        long criteria = 4000L << 51 | 1000L << 39;
        int depMins = -235;
        criteria = PackedCriteria.withDepMins(criteria, depMins);
        int value = PackedCriteria.depMins(criteria);
        int expected = -235;
        assertEquals(expected, value);
    }

    @Test
    void testWithAdditionalChange() {
        long criteria = 0b1101L << 32;
        criteria = PackedCriteria.withAdditionalChange(criteria);
        int value = PackedCriteria.changes(criteria);
        int expexted = 14;
        assertEquals(expexted, value);
    }

    @Test
    void testWithPayloadPositive() {
        long criteria = 0b11010100011101L;
        int payload = 0b1101;
        criteria = PackedCriteria.withPayload(criteria, payload);
        int value = PackedCriteria.payload(criteria);
        int expected = 13;
        assertEquals(expected, value);
    }
    @Test
    void testWithPayloadNegative() {
        long criteria = 0b1L << 31;
        int payload = 0b11 << 30;
        criteria = PackedCriteria.withPayload(criteria, payload);
        int value = PackedCriteria.payload(criteria);
        int expected = 0b11 << 30;
        assertEquals(expected, value);
    }

    @Test
    void testPackValid() {
        // Test de base pour pack : on passe des valeurs valides.
        // arrMins est en minutes réelles [-240, 2880), ici 0 par exemple.
        int arrMins = 0; // heure réelle = 0, stockée = 0 + 240 = 240
        int changes = 50; // valide (<128)
        int payload = 0x12345678; // payload quelconque (32 bits)
        long criteria = PackedCriteria.pack(arrMins, changes, payload);

        long expected = (((long)(arrMins + 240)) << 39)
                | (((long) changes) << 32)
                | Integer.toUnsignedLong(payload);
        assertEquals(Long.toBinaryString(expected), Long.toBinaryString(criteria));
    }

    @Test
    void testPackEdgeValues() {
        // Test avec les valeurs extrêmes de arrMins et changes.
        int arrMinsMin = -240;  // => stored = -240 + 240 = 0
        int arrMinsMax = 2879;  // => stored = 2879 + 240 = 3119 (<4096)
        int changesMin = 0;
        int changesMax = 127;
        int payload = 0; // payload simple

        long criteriaMin = PackedCriteria.pack(arrMinsMin, changesMin, payload);
        long expectedMin = (((long)(arrMinsMin + 240)) << 39)
                | (((long) changesMin) << 32)
                | Integer.toUnsignedLong(payload);
        assertEquals(Long.toBinaryString(expectedMin), Long.toBinaryString(criteriaMin));

        long criteriaMax = PackedCriteria.pack(arrMinsMax, changesMax, payload);
        long expectedMax = (((long)(arrMinsMax + 240)) << 39)
                | (((long) changesMax) << 32)
                | Integer.toUnsignedLong(payload);
        assertEquals(Long.toBinaryString(expectedMax), Long.toBinaryString(criteriaMax));
    }

    @Test
    void testPackInvalidArrMins() {
        // arrMins hors intervalle : inférieur à -240 ou >= 2880
        int validChanges = 0;
        int validPayload = 0;
        int invalidLow = -241;
        int invalidHigh = 2880;
        assertThrows(IllegalArgumentException.class, () -> {
            PackedCriteria.pack(invalidLow, validChanges, validPayload);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            PackedCriteria.pack(invalidHigh, validChanges, validPayload);
        });
    }

    @Test
    void testPackInvalidChanges() {
        // changes >= 128
        int arrMins = 0;
        int invalidChanges = 128;
        int payload = 0;
        assertThrows(IllegalArgumentException.class, () -> {
            PackedCriteria.pack(arrMins, invalidChanges, payload);
        });
    }

    @Test
    void testArrMinsUnpack() {
        // Vérifie que la méthode arrMins renvoie bien la valeur d'origine.
        int originalArrMins = 100; // valeur réelle
        int changes = 10;
        int payload = 12345;
        long criteria = PackedCriteria.pack(originalArrMins, changes, payload);
        assertEquals(originalArrMins, PackedCriteria.arrMins(criteria));
    }

    @Test
    void testChangesUnpack() {
        int arrMins = 0;
        int originalChanges = 75;
        int payload = 0;
        long criteria = PackedCriteria.pack(arrMins, originalChanges, payload);
        assertEquals(originalChanges, PackedCriteria.changes(criteria));
    }

    @Test
    void testPayloadUnpack() {
        int arrMins = 0;
        int changes = 0;
        int payload = 0xDEADBEEF;
        long criteria = PackedCriteria.pack(arrMins, changes, payload);
        assertEquals(payload, PackedCriteria.payload(criteria));
    }

    @Test
    void testWithDepMinsValid() {
        // Test de withDepMins : on ajoute une heure de départ valide et on vérifie
        int arrMins = 50;
        int changes = 20;
        int payload = 0xCAFEBABE;
        long criteria = PackedCriteria.pack(arrMins, changes, payload);
        int depMins = 20; // heure de départ réelle
        long newCriteria = PackedCriteria.withDepMins(criteria, depMins);
        assertTrue(PackedCriteria.hasDepMins(newCriteria));
        assertEquals(depMins, PackedCriteria.depMins(newCriteria));
    }

    @Test
    void testWithDepMinsInvalid() {
        // Test avec des heures de départ hors intervalle
        int arrMins = 0;
        int changes = 0;
        int payload = 0;
        long criteria = PackedCriteria.pack(arrMins, changes, payload);
        int invalidDepLow = -241;
        int invalidDepHigh = 2880;
        assertThrows(IllegalArgumentException.class, () -> {
            PackedCriteria.withDepMins(criteria, invalidDepLow);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            PackedCriteria.withDepMins(criteria, invalidDepHigh);
        });
    }

    @Test
    void TtestWithoutDepMins() {
        // Test que withoutDepMins supprime l'heure de départ.
        int arrMins = 100;
        int changes = 10;
        int payload = 54321;
        long criteria = PackedCriteria.pack(arrMins, changes, payload);
        long criteriaWithDep = PackedCriteria.withDepMins(criteria, 100);
        assertTrue(PackedCriteria.hasDepMins(criteriaWithDep));
        long criteriaCleared = PackedCriteria.withoutDepMins(criteriaWithDep);
        assertFalse(PackedCriteria.hasDepMins(criteriaCleared));
    }
    @Test
    void TestWithDepMinsExceptionIfDepSupArr() {
        // Test que withoutDepMins supprime l'heure de départ.
        int arrMins = 100;
        int changes = 10;
        int payload = 54321;
        long criteria = PackedCriteria.pack(arrMins, changes, payload);
        assertThrows(IllegalArgumentException.class, () -> {
            long criteriaWithDep = PackedCriteria.withDepMins(criteria, 101);

        });

    }

    @Test
    void testDominatesOrIsEqualWithoutDep() {
        // Cas sans heure de départ
        int arrMins1 = 0;   // correspond à une arrivée traduite = 240
        int arrMins2 = 50;
        int changes1 = 5;
        int changes2 = 10;
        int payload = 0;
        long criteria1 = PackedCriteria.pack(arrMins1, changes1, payload);
        long criteria2 = PackedCriteria.pack(arrMins2, changes2, payload);
        // On veut minimiser l'heure d'arrivée et les changements
        assertTrue(PackedCriteria.dominatesOrIsEqual(criteria1, criteria2));
        assertFalse(PackedCriteria.dominatesOrIsEqual(criteria2, criteria1));
    }

    @Test
    void testDominatesOrIsEqualWithoutDepEdge() {
        // Cas sans heure de départ
        int arrMins1 = 0;   // correspond à une arrivée traduite = 240
        int arrMins2 = 0;
        int changes1 = 5;
        int changes2 = 5;
        int payload = 0;
        long criteria1 = PackedCriteria.pack(arrMins1, changes1, payload);
        long criteria2 = PackedCriteria.pack(arrMins2, changes2, payload);
        // On veut minimiser l'heure d'arrivée et les changements
        assertTrue(PackedCriteria.dominatesOrIsEqual(criteria1, criteria2));
        assertTrue(PackedCriteria.dominatesOrIsEqual(criteria2, criteria1));

    }

    @Test
    void testDominatesOrIsEqualWithDepEdge() {
        // Cas avec heure de départ
        int arrMins1 = 100;
        int arrMins2 = 100;
        int changes1 = 5;
        int changes2 = 5;
        int payload = 0;
        long base1 = PackedCriteria.pack(arrMins1, changes1, payload);
        long base2 = PackedCriteria.pack(arrMins2, changes2, payload);
        int dep1 = 90; // meilleure heure de départ (plus tard)
        int dep2 = 90;
        long criteria1 = PackedCriteria.withDepMins(base1, dep1);
        long criteria2 = PackedCriteria.withDepMins(base2, dep2);
        assertTrue(PackedCriteria.dominatesOrIsEqual(criteria1, criteria2));
        assertTrue(PackedCriteria.dominatesOrIsEqual(criteria2, criteria1));
    }

    @Test
    void testDominatesOrIsEqualWithDepWithOnlyOne() {
        // Cas avec heure de départ
        int arrMins1 = 100;
        int arrMins2 = 100;
        int changes1 = 5;
        int changes2 = 6;
        int payload = 0;
        long base1 = PackedCriteria.pack(arrMins1, changes1, payload);
        long base2 = PackedCriteria.pack(arrMins2, changes2, payload);
        int dep1 = 90; // meilleure heure de départ (plus tard)
        int dep2 = 90;
        long criteria1 = PackedCriteria.withDepMins(base1, dep1);
        long criteria2 = PackedCriteria.withDepMins(base2, dep2);
        assertTrue(PackedCriteria.dominatesOrIsEqual(criteria1, criteria2));
        assertFalse(PackedCriteria.dominatesOrIsEqual(criteria2, criteria1));
    }

    @Test
    void testDominatesOrIsEqualWithDepNotDominateEachOther() {
        // Cas avec heure de départ
        int arrMins1 = 90;
        int arrMins2 = 100;
        int changes1 = 6;
        int changes2 = 5;
        int payload = 0;
        long base1 = PackedCriteria.pack(arrMins1, changes1, payload);
        long base2 = PackedCriteria.pack(arrMins2, changes2, payload);
        int dep1 = 90; // meilleure heure de départ (plus tard)
        int dep2 = 90;
        long criteria1 = PackedCriteria.withDepMins(base1, dep1);
        long criteria2 = PackedCriteria.withDepMins(base2, dep2);
        assertFalse(PackedCriteria.dominatesOrIsEqual(criteria1, criteria2));
        assertFalse(PackedCriteria.dominatesOrIsEqual(criteria2, criteria1));
    }



    @Test
    void testDominatesOrIsEqualWithDep() {
        // Cas avec heure de départ
        int arrMins1 = 100;
        int arrMins2 = 150;
        int changes1 = 5;
        int changes2 = 10;
        int payload = 0;
        long base1 = PackedCriteria.pack(arrMins1, changes1, payload);
        long base2 = PackedCriteria.pack(arrMins2, changes2, payload);
        int dep1 = 95; // meilleure heure de départ (plus tard)
        int dep2 = 90;
        long criteria1 = PackedCriteria.withDepMins(base1, dep1);
        long criteria2 = PackedCriteria.withDepMins(base2, dep2);
        assertTrue(PackedCriteria.dominatesOrIsEqual(criteria1, criteria2));
        assertFalse(PackedCriteria.dominatesOrIsEqual(criteria2, criteria1));
    }

    @Test
    void testDominatesOrIsEqualInconsistentDep() {
        // Test qu'une incohérence (l'un a une heure de départ et pas l'autre) lève une exception
        int arrMins = 100;
        int changes = 5;
        int payload = 0;
        long criteriaWithDep = PackedCriteria.withDepMins(PackedCriteria.pack(arrMins, changes, payload), 99);
        long criteriaWithoutDep = PackedCriteria.pack(arrMins, changes, payload);
        assertThrows(IllegalArgumentException.class, () -> {
            PackedCriteria.dominatesOrIsEqual(criteriaWithDep, criteriaWithoutDep);
        });
    }

    @Test
    void testWithAdditionalChangeValid() {
        // Test que withAdditionalChange incrémente le nombre de changements
        int arrMins = 0;
        int changes = 50;
        int payload = 0;
        long criteria = PackedCriteria.pack(arrMins, changes, payload);
        long newCriteria = PackedCriteria.withAdditionalChange(criteria);
        assertEquals(changes + 1, PackedCriteria.changes(newCriteria));
    }

    @Test
    void testWithAdditionalChangeInvalid() {
        // Test que l'incrémentation échoue si le nombre de changements atteint 127
        int arrMins = 0;
        int changes = 127;
        int payload = 0;
        long criteria = PackedCriteria.pack(arrMins, changes, payload);
        assertThrows(IllegalArgumentException.class, () -> {
            PackedCriteria.withAdditionalChange(criteria);
        });
    }

    @Test
    void testWithPayload() {
        // Test que withPayload remplace correctement le payload
        int arrMins = 0;
        int changes = 10;
        int payload = 0x12345678;
        long criteria = PackedCriteria.pack(arrMins, changes, payload);
        int newPayload = 0xDEADBEEF;
        long newCriteria = PackedCriteria.withPayload(criteria, newPayload);
        assertEquals(newPayload, PackedCriteria.payload(newCriteria));
    }

    @Test
    void testWithPayloadEdgeOriginalPayload() {
        // Test que withPayload remplace correctement le payload
        int arrMins = 0;
        int changes = 10;
        int payload = 0xffffffff;
        long criteria = PackedCriteria.pack(arrMins, changes, payload);
        int newPayload = 0x1101;
        long newCriteria = PackedCriteria.withPayload(criteria, newPayload);
        assertEquals(newPayload, PackedCriteria.payload(newCriteria));
    }
    @Test
    void testWithPayloadEdgeNewPayload() {
        // Test que withPayload remplace correctement le payload
        int arrMins = 0;
        int changes = 10;
        int payload = 0x12345678;
        long criteria = PackedCriteria.pack(arrMins, changes, payload);
        int newPayload = 0xffffffff;
        long newCriteria = PackedCriteria.withPayload(criteria, newPayload);
        assertEquals(newPayload, PackedCriteria.payload(newCriteria));
    }



    @Test
    public void testDominatesOrIsEqualEqual() {
        long criteria1 = PackedCriteria.pack(500, 2, 100);
        long criteria2 = PackedCriteria.pack(500, 2, 100);
        assertTrue(PackedCriteria.dominatesOrIsEqual(criteria1, criteria2));
    }
    @Test
    void testPack_ValidValues_CE() {
        int arrMins = 100;
        int changes = 5;
        int payload = 123456;
        long result = PackedCriteria.pack(arrMins, changes, payload);
        assertEquals((Integer.toUnsignedLong(arrMins + 240) << 39) | (Integer.toUnsignedLong(changes) << 32) | Integer.toUnsignedLong(payload), result);
    }

    @Test
    void testPack_InvalidArrMins_CE() {
        assertThrows(IllegalArgumentException.class, () -> PackedCriteria.pack(-241, 5, 123456));
        assertThrows(IllegalArgumentException.class, () -> PackedCriteria.pack(2880, 5, 123456));
    }

    @Test
    void testPack_InvalidChanges_CE() {
        assertThrows(IllegalArgumentException.class, () -> PackedCriteria.pack(100, 128, 123456));
    }

    @Test
    void testHasDepMins_CE() {
        long criteriaWithDepMins = 1L << 51;
        long criteriaWithoutDepMins = 0L;
        assertTrue(PackedCriteria.hasDepMins(criteriaWithDepMins));
        assertFalse(PackedCriteria.hasDepMins(criteriaWithoutDepMins));
    }

    @Test
    void testDepMins_Invalid_CE() {
        assertThrows(IllegalArgumentException.class, () -> PackedCriteria.depMins(0L));
    }

    @Test
    void testArrMins_CE() {
        long criteria = PackedCriteria.pack(300, 5, 123456);
        assertEquals(300, PackedCriteria.arrMins(criteria));
    }

    @Test
    void testChanges_CE() {
        long criteria = PackedCriteria.pack(300, 5, 123456);
        assertEquals(5, PackedCriteria.changes(criteria));
    }

    @Test
    void testPayload_CE() {
        long criteria = PackedCriteria.pack(300, 5, 123456);
        assertEquals(123456, PackedCriteria.payload(criteria));
    }

    @Test
    void testDominatesOrIsEqual_CE() {
        long criteria1 = PackedCriteria.pack(200, 3, 10000);
        long criteria2 = PackedCriteria.pack(100, 5, 20000);
        assertTrue(PackedCriteria.dominatesOrIsEqual(criteria1, criteria1));
        assertFalse(PackedCriteria.dominatesOrIsEqual(criteria2, criteria1));
        assertFalse(PackedCriteria.dominatesOrIsEqual(criteria1, criteria2));
    }

    @Test
    void testDominatesOrIsEqualWithDepTime_CE() {
        long criteria1 = PackedCriteria.pack(200, 3, 10000);
        long criteria2 = PackedCriteria.pack(200, 3, 20000);
        criteria1 = PackedCriteria.withDepMins(criteria1, 100);
        criteria2 = PackedCriteria.withDepMins(criteria2, 0);
        assertTrue(PackedCriteria.dominatesOrIsEqual(criteria1, criteria1));
        assertFalse(PackedCriteria.dominatesOrIsEqual(criteria2, criteria1));
        assertTrue(PackedCriteria.dominatesOrIsEqual(criteria1, criteria2));
    }
    @Test
    void testWithoutDepMins_CE() {
        long criteria = PackedCriteria.withDepMins(0L, -240);
        assertFalse(PackedCriteria.hasDepMins(PackedCriteria.withoutDepMins(criteria)));
    }


    @Test
    void testWithAdditionalChange_CE() {
        long criteria = PackedCriteria.pack(100, 3, 123456);
        long updatedCriteria = PackedCriteria.withAdditionalChange(criteria);
        assertEquals(3 + 1, PackedCriteria.changes(updatedCriteria));
    }

    @Test
    void testWithAdditionalChanges_CE() {
        long criteria = PackedCriteria.pack(100, 3, 123456);
        long updatedCriteria = PackedCriteria.withAdditionalChange(criteria);
        assertEquals(4, PackedCriteria.changes(updatedCriteria));
        updatedCriteria = PackedCriteria.withAdditionalChange(updatedCriteria);
        assertEquals(5, PackedCriteria.changes(updatedCriteria));
        updatedCriteria = PackedCriteria.withAdditionalChange(updatedCriteria);
        assertEquals(6, PackedCriteria.changes(updatedCriteria));
        updatedCriteria = PackedCriteria.withAdditionalChange(updatedCriteria);
        updatedCriteria = PackedCriteria.withAdditionalChange(updatedCriteria);
        assertEquals(8, PackedCriteria.changes(updatedCriteria));
    }

    @Test
    void testWithPayload_CE() {
        long criteria = PackedCriteria.pack(100, 3, 123456);
        long updatedCriteria = PackedCriteria.withPayload(criteria, 654321);
        assertEquals(654321, PackedCriteria.payload(updatedCriteria));
    }

    @Test
    void testThrowCompare_CE() {
        long criteria1 = PackedCriteria.pack(230, 8, 4554);
        long criteria2 = PackedCriteria.pack(331, 110, 1);
        assertThrows(IllegalArgumentException.class, () -> PackedCriteria.dominatesOrIsEqual(criteria1, PackedCriteria.withDepMins(criteria2, 334)));
    }

    @Test
    void totalChangeTest_CE() {
        long criteria1 = PackedCriteria.pack(1023, 72, 11334);
        criteria1 = PackedCriteria.withDepMins(criteria1, 892);
        criteria1 = PackedCriteria.withAdditionalChange(criteria1);
        criteria1 = PackedCriteria.withPayload(criteria1, 55345);
        criteria1 = PackedCriteria.withDepMins(criteria1, 4);
        criteria1 = PackedCriteria.withPayload(criteria1, -9221);
        criteria1 = PackedCriteria.withAdditionalChange(criteria1);
        criteria1 = PackedCriteria.withAdditionalChange(criteria1);
        assertEquals(PackedCriteria.payload(criteria1), -9221);
        criteria1 = PackedCriteria.withPayload(criteria1, -221);
        criteria1 = PackedCriteria.withPayload(criteria1, 0);
        criteria1 = PackedCriteria.withPayload(criteria1, -99208);
        assertEquals(PackedCriteria.payload(criteria1), -99208);
    }








}
