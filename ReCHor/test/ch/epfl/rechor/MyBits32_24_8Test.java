package ch.epfl.rechor;

/*
 *	Author:      Antoine Lepin
 *	Date:
 */

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
        assertEquals(0b11111111111111111111111011111100, Bits32_24_8.pack(0b111111111111111111111110, 0b11111100));
    }

    @Test
    void TestUnpack24() {
        assertEquals(0b111111111111011111101110, Bits32_24_8.unpack24(0b11111111111101111110111011101110));
    }

    @Test
    void TestUnpack8() {
        assertEquals(0b11101110, Bits32_24_8.unpack8(0b11111111111101111110111011101110));

    }

}
