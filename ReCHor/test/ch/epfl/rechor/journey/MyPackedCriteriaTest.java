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

        assertEquals(Long.toBinaryString(expected), Long.toBinaryString(value));
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
        long criteria = 0b111111110000 << 51;
        int depMins = 0b111111111010;
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

}
