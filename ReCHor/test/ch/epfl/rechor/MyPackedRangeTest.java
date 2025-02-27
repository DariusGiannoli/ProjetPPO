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

    @Test
    void testPack_ValidValues_CE() {
        int packed = PackedRange.pack(100, 120);
        assertEquals(100, PackedRange.startInclusive(packed));
        assertEquals(120, PackedRange.endExclusive(packed));
        assertEquals(20, PackedRange.length(packed));
    }

    @Test
    void testPack_EdgeCase_MaxValues_CE() {
        int packed = PackedRange.pack(0xFFFFFF,0xFFFFFF  + 0xFF);
        assertEquals(0xFFFFFF, PackedRange.startInclusive(packed));
        assertEquals( 0xFFFFFF +  0xFF, PackedRange.endExclusive(packed));
        assertEquals(0xFF, PackedRange.length(packed));
    }

    @Test
    void testPack_EdgeCase_SameValues_CE() {
        int packed = PackedRange.pack(13345,13345);
        assertEquals(13345, PackedRange.startInclusive(packed));
        assertEquals( 13345, PackedRange.endExclusive(packed));
        assertEquals(0, PackedRange.length(packed));
    }

    @Test
    void testPack_InvalidStartValue_CE() {
        assertThrows(IllegalArgumentException.class, () -> PackedRange.pack(1000, 999));
    }

    @Test
    void testPack_InvalidEndValue_CE() {
        assertThrows(IllegalArgumentException.class, () -> PackedRange.pack(100, 1000));
    }

    @Test
    void testLength_ValidInterval_CE() {
        int packed = PackedRange.pack(5100, 5150);
        assertEquals(50, PackedRange.length(packed));
    }

    @Test
    void testStartInclusive_ValidInterval_CE() {
        int packed = PackedRange.pack(300, 350);
        assertEquals(300, PackedRange.startInclusive(packed));
    }


    @Test
    void testStartInclusive_EdgeCase_Zero_CE() {
        int packed = PackedRange.pack(0, 255);
        assertEquals(255, PackedRange.length(packed));
    }

    @Test
    void testEndExclusive_ValidInterval_CE() {
        int packed = PackedRange.pack(150, 180);
        assertEquals(180, PackedRange.endExclusive(packed));
    }

    @Test
    void testEndExclusive_SingleElementRange_CE() {
        int packed = PackedRange.pack(75, 76);
        assertEquals(76, PackedRange.endExclusive(packed));
        assertEquals(75, PackedRange.startInclusive(packed));
        assertEquals(1, PackedRange.length(packed));
    }


    @Test
    void testPackValidRangeBd() {
        int start = 0xABCDEF;
        int end = start + 0x12;
        int expected = (start << 8) | (end - start);
        assertEquals(expected, PackedRange.pack(start, end));
    }


    @Test
    void testPackInvalidStartBd() {
        int start = 0x1000000; // Exceeds 24 bits
        int end = start + 10;
        assertThrows(IllegalArgumentException.class, () -> PackedRange.pack(start, end));
    }


    @Test
    void testPackInvalidLengthBd() {
        int start = 0x123456;
        int end = start + 0x100; // Exceeds 8-bit length
        assertThrows(IllegalArgumentException.class, () -> PackedRange.pack(start, end));
    }


    @Test
    void testLengthBd() {
        int start = 0xABCDEF;
        int end = start + 0x12;
        int packed = PackedRange.pack(start, end);
        assertEquals(0x12, PackedRange.length(packed));
    }


    @Test
    void testStartInclusiveBd() {
        int start = 0xABCDEF;
        int end = start + 0x12;
        int packed = PackedRange.pack(start, end);
        assertEquals(start, PackedRange.startInclusive(packed));
    }


    @Test
    void testEndExclusiveBd() {
        int start = 0xABCDEF;
        int end = start + 0x12;
        int packed = PackedRange.pack(start, end);
        assertEquals(end, PackedRange.endExclusive(packed));
    }



}
