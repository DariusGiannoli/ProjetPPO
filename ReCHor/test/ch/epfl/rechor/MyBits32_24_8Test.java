package ch.epfl.rechor;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MyBits32_24_8Test {

    @Test
    void ThrowExceptionIfMoreThan24Bits() {

        assertThrows(IllegalArgumentException.class, () -> {
            Bits32_24_8.pack(0b1111111111111111111111111, 0b11111111);
        });

    }

    @Test
    void ThrowExceptionIfMoreThan8Bits() {
        assertThrows(IllegalArgumentException.class, () -> {
            Bits32_24_8.pack(0b111111111111111111111111, 0b111111111);
        });
    }

    @Test
    void TestPack() {
        int result = Bits32_24_8.pack(0b111111111111111111111110, 0b11111100);
        int expected = 0b11111111111111111111111011111100;
        assertEquals(expected, result);
    }

    @Test
    void TestUnpack24() {
        int result = Bits32_24_8.unpack24(0b11111111111101111110111011101110);
        int expected = 0b111111111111011111101110;
        assertEquals(expected, result);
    }

    @Test
    void TestUnpack8() {
        int result = Bits32_24_8.unpack8(0b11111111111101111110111011101110);
        int expected = 0b11101110;
        assertEquals(expected, result);

    }

}
