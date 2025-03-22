package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.Transfers;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.HexFormat;
import java.util.NoSuchElementException;

import static ch.epfl.rechor.PackedRange.pack;
import static ch.epfl.rechor.timetable.mapped.MyBufferedConnectionsTest.succBuffer;
import static ch.epfl.rechor.timetable.mapped.MyBufferedConnectionsTest.yAPlusdeFromage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MyBufferedTransfersTest {
    public static HexFormat hexFormat = HexFormat.ofDelimiter(" ");


    public static byte[] bytes = hexFormat.parseHex("aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa");
    public static byte[] succBytes = hexFormat.parseHex("aa aa 00 22 f2 aa aa 00 22 aa aa aa 00 22 aa aa aa 00 22 aa aa aa 00 22 aa aa aa 00 22 aa aa aa 00 22 aa 7a 99 00 21 7b aa aa 00 21 aa aa aa 00 2b aa 10 0e 00 28 e9 aa aa 00 28 aa aa aa 00 28 91 e8 21 aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa");
    public static ByteBuffer yAPlusdeFromage = ByteBuffer.wrap(bytes);
    public static ByteBuffer succBuffer2 = ByteBuffer.wrap(succBytes);


    @Test
    void Nicolas_CramTest_CE() {
        Transfers TankTrouble = new BufferedTransfers(yAPlusdeFromage);
        assertEquals( 77, TankTrouble.size());
        //hex "aa aa" = 43690
        assertEquals( pack(0,77), TankTrouble.arrivingAt(43690));

    }

    @Test
    void PampleMousseMyrtille_CE() {
        Transfers Entropy = new BufferedTransfers(succBuffer2);
        assertEquals(77, Entropy.size());
        assertEquals(pack(0,7), Entropy.arrivingAt(34));
        assertEquals(pack(7,9), Entropy.arrivingAt(33));
        assertEquals(pack(9,10), Entropy.arrivingAt(43));
        assertEquals(pack(10,13), Entropy.arrivingAt(40));
        assertEquals(145, Entropy.minutes(12));
        assertEquals(170, Entropy.minutes(4));
        assertEquals(242, Entropy.minutes(0));
        assertThrows(NoSuchElementException.class , () ->Entropy.minutesBetween(21, 212));
        assertEquals(59425, Entropy.depStationId(13));
        assertEquals(4110, Entropy.depStationId(10));
        assertEquals(233, Entropy.minutesBetween(4110, 40));
        assertEquals(123, Entropy.minutesBetween(31385, 33));
    }

    public static byte[] bytes2 = hexFormat.parseHex("aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa");
    public static byte[] succBytes2 = hexFormat.parseHex("aa aa 00 22 f2 aa aa 00 22 aa aa aa 00 22 aa aa aa 00 22 aa aa aa 00 22 aa aa aa 00 22 aa aa aa 00 22 aa 7a 99 00 21 7b aa aa 00 21 aa aa aa 00 2b aa 10 0e 00 28 e9 aa aa 00 28 aa aa aa 00 28 91 e8 21 aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa");
    public static ByteBuffer yAPlusdeFromage2 = ByteBuffer.wrap(bytes2);
    public static ByteBuffer succBuffer3 = ByteBuffer.wrap(succBytes2);



}
