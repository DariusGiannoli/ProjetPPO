package ch.epfl.rechor;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MyPackedRangeTest {

    @Test
    void testPackWithIntegers() {

        int start = 0b1100100;
        int end = 0b11111111;
        int length = 0b10011011;
        int value = PackedRange.pack(start, end);
        int expected = 0b110010010011011;

        assertEquals(expected, value);
    }

    @Test
    void testExceptionPackIfLengthMoreThan8Bits() {
        int start = 0b100101100;
        int end = 0b1001011000;

        assertThrows(IllegalArgumentException.class, () -> {
            PackedRange.pack(start, end);
        });
    }

    @Test
    void testLength() {
        int expected = 0b10011011;
        int interval = 0b110010010011011;
        int value = PackedRange.length(interval);
        assertEquals(expected, value);

    }

    @Test
    void testStartInclusive() {
        int expected = 0b1100100;
        int interval = 0b110010010011011;
        int value = PackedRange.startInclusive(interval);
        assertEquals(value, expected);
    }

    @Test
    void testEndExclusive() {
        int interval = 0b110010010011011;
        int expected = 0b11111111;
        int value = PackedRange.endExclusive(interval);
        assertEquals(expected, value);
    }

    @Test
    void testPackWithNegativeLength(){
        int start = 900;
        int end = 800;

        assertThrows(IllegalArgumentException.class, () -> {
            PackedRange.pack(start, end);
        });
    }

    @Test
    void testPackWithStartMoreThan24bits() {
        int start = 1 << 24;
        int end = (1 << 24) + 2 ;

        assertThrows(IllegalArgumentException.class, () -> {
            PackedRange.pack(start, end);
        });
    }

}
