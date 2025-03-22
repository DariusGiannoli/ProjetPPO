package ch.epfl.rechor.timetable.mapped;


import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MyBufferedConnectionsTest {

    @Test
    void testBufferedConnection() {
        List<String> stringStationList = new ArrayList<>();
        stringStationList.add("  ");
        stringStationList.add("  ");
        stringStationList.add("M1 Lausanne Flon-Renens VD");
        stringStationList.add("  ");
        stringStationList.add("M2");
        stringStationList.add("RE");

        byte[] tableauBytes = {0x00, 0x02, 0x01, 0x00, 0x04, 0x01, 0x00, 0x05, 0x02};
        ByteBuffer buffer = ByteBuffer.wrap(tableauBytes);
        BufferedRoutes routes = new BufferedRoutes(stringStationList, buffer);

        List<String> stringStationList2 = new ArrayList<>();
        stringStationList2.add("  ");
        stringStationList2.add("  ");
        stringStationList2.add("Coppet");
        stringStationList2.add("  ");
        stringStationList2.add("Scoubydoubydou");
        stringStationList2.add("Renens VD");

        byte[] tableauBytes2 = {0x00, 0x02, 0x00, 0x02, 0x00, 0x01, 0x00, 0x05, 0x00, 0x00, 0x00, 0x04};
        ByteBuffer buffer2 = ByteBuffer.wrap(tableauBytes2);
        BufferedTrips trips = new BufferedTrips(stringStationList2, buffer2);

        List<String> stringStationList3 = new ArrayList<>();
        stringStationList2.add("  ");
        stringStationList2.add("  ");
        stringStationList2.add("Zurich");
        stringStationList2.add("  ");
        stringStationList2.add("ScibidiFortnite");
        stringStationList2.add("La Daronne de Steph");
        byte[] tableauBytes3 = {0x00, 0x02, 0x00, 0x02, 0x00, 0x01, 0x00, 0x05, 0x00, 0x00, 0x00, 0x04};

    }

    public static HexFormat hexFormat = HexFormat.ofDelimiter(" ");
    //768
    // 384 U8
    public static byte[] bytes = hexFormat.parseHex("aa aa aa aa a3 5f 9c 12 d7 8b 44 ee 39 2a 71 c4 06 f8 01 29 99 b2 3c a1 47 50 68 1f e3 7d 82 94 ab cd 09 36 bf 22 48 5a 77 ec d5 1b 63 fa 4c 0d 81 92 ae 3f 6b 28 55 cc 13 f7 09 74 b0 5e d9 8a 41 2d 97 e6 3a 01 7b 52 c8 19 f0 85 64 aa 3d 57 ef 0c 32 4f 98 dc 26 7e 14 b9 43 60 a5 8f 1d e2 75 c0 09 fb 30 56 d4 87 21 9b 5c ae 42 78 0f 69 c7 34 fa 98 27 b1 5d 03 e6 7a 50 cc 12 89 46 bf 3e 72 1c d5 8d 69 04 2a f3 58 96 e0 7b 11 43 bd 27 c9 8f 65 02 3c da 51 78 a3 0e 4d bf 29 97 60 c8 13 f4 82 5b 36 70 ac 01 d9 8f 45 e2 19 67 bd 32 4a 01 2C ce 7f 20 58 b3 6c 81 2d da 47 f9 10 63 c5 34 ab 75 02 e8 3c 51 96 0f d4 8a 29 7e b1 40 63 fc 19 75 2a 89 d7 4e b5 03 6c 58 a9 32 ef 10 c3 8d 57 21 4b a0 6d 3e 95 08 dc 72 51 bf 14 e3 49 60 a5 8f 1d c7 5a 28 9e 43 f1 07 6d 52 b9 30 84 ce 15 79 24 a3 58 9c 07 d6 4e 1f 75 b0 2a 89 34 fc 10 63 c5 5a ab 01 6d 48 9e 37 f1 0c 52 8d 26 bf 75 03 ea 41 5d 98 20 c7 6b 34 af 12 79 d5 48 9e 07 53 b0 2c 6d 41 fa 19 75 c5 3a 87 d2 5b a9 04 60 a1 ab e9 01 14 9b dd a4 9d 2e 9e 1a 3e 09 11 33 3e b8 1d d0 98 7f be a9 d7 71 10 0e bf bf a3 5f 9c 12 d7 8b 44 ee 39 2a 71 c4 06 f8 5d 0e 99 b2 3c a1 01 00 68 1f d7 be af af af b0");
    public static byte[] succBytes = hexFormat.parseHex("ee 39 2a 71 c4 06 f8 01 29 99 b2 3c a1 47 50 68 1f e3 5d 8c 4a f1 32 d7 90 ab 7e 14 63 bd 25 fa 88 0b c9 6e 0f 52 1d 79 40 3e ca 86 9b f5 2c 07 e8 33 d0 5a 91 bc 62 ad 48 15 7f 2e c3 98 50 fa 3a f7 9d 24 b1 6c 58 0e a3 d2 7b 45 0e 19 80 cb 36 5e 01 af 92 e8 4d 73 c7 2f 5a 86 d1 3e 64 bd 0c 97 42 fa 28 59 ac e3 1f 78 d6 40 93 b7 25 8e 5d ca 0a f4 31 69 bc 07 53 e2 9f 48 1d 70 df 3b");
    public static ByteBuffer yAPlusdeFromage = ByteBuffer.wrap(bytes);
    public static ByteBuffer succBuffer = ByteBuffer.wrap(succBytes);


    @Test
    void MaledictionDeLaRaclette_CE() {
        BufferedConnections yAPlusDePatate = new BufferedConnections(yAPlusdeFromage, succBuffer);

        assertEquals(32, yAPlusDePatate.size());
        assertEquals(236552395, yAPlusDePatate.nextConnectionId(19));
        assertEquals(0xCE7F, yAPlusDePatate.depStopId(15));
        assertEquals(0x2058, yAPlusDePatate.depMins(15));
        assertEquals(0xB36C, yAPlusDePatate.arrStopId(15));
        assertEquals(0x812D, yAPlusDePatate.arrMins(15));
        assertEquals(0xda47f9, yAPlusDePatate.tripId(15));
        assertEquals(0x10, yAPlusDePatate.tripPos(15));
    }
    @Test
    void minutesBetweenReturnsExpectedValues() {
        ByteBuffer testBuffer = ByteBuffer.allocate(10);
        testBuffer.putShort((short) 10).putShort((short) 20).put((byte) 5); // First transfer
        testBuffer.putShort((short) 15).putShort((short) 20).put((byte) 8); // Second transfer
        testBuffer.flip();


        BufferedTransfers transfers = new BufferedTransfers(testBuffer);
        assertEquals(5, transfers.minutesBetween(10, 20));
        assertEquals(8, transfers.minutesBetween(15, 20));
    }




}
