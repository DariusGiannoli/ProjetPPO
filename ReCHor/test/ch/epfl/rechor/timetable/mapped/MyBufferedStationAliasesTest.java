package ch.epfl.rechor.timetable.mapped;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MyBufferedStationAliasesTest {


    @Test
    void testBufferedStationsAlias() {
        List<String> stringStationList = new ArrayList<>();
        stringStationList.add("  ");
        stringStationList.add("  ");
        stringStationList.add("Anet");
        stringStationList.add("Ins");
        stringStationList.add("Lausanne");
        stringStationList.add("Losanna");


        byte[] tableauBytes = {00, 05, 00, 04, 00, 02, 00, 03};
        ByteBuffer buffer = ByteBuffer.wrap(tableauBytes);


        BufferedStationAliases stations = new BufferedStationAliases(stringStationList, buffer);
        assertEquals(stations.stationName(0), "Lausanne");
        assertEquals(stations.stationName(1), "Ins");
        assertEquals(stations.alias(0), "Losanna");
        assertEquals(stations.alias(1), "Anet");
        assertEquals(stations.size(), 2);
    }
    static public byte[] toByte(int[] xs) {
        byte[] res = new byte[xs.length];
        for (int i = 0; i < xs.length; i++) {
            res[i] = (byte) xs[i];
        }
        return res;
    }


    public static ByteBuffer byteBufferOfLength(int n) {
        int[] aliasesByteAsInt = new int[n];
        byte[] aliasesByte = toByte(aliasesByteAsInt);
        return ByteBuffer.wrap(aliasesByte);
    }


    @Test
    void givenExempleBd() {
        List<String> stringTable = new ArrayList<>();
        stringTable.add("1");
        stringTable.add("70");
        stringTable.add("Anet");
        stringTable.add("Ins");
        stringTable.add("Lausanne");
        stringTable.add("Losanna");
        stringTable.add("Palézieux");


        int[] aliasesByteAsInt = {0x00, 0x05, 0x00, 0x04, 0x00, 0x02, 0x00, 0x03,};
        byte[] aliasesByte = toByte(aliasesByteAsInt);
        ByteBuffer aliasesByteBuffer = ByteBuffer.wrap(aliasesByte);
        BufferedStationAliases bufferedStationAliases = new BufferedStationAliases(stringTable, aliasesByteBuffer);




        assertEquals(2, bufferedStationAliases.size());
        assertEquals("Losanna", bufferedStationAliases.alias(0));
        assertEquals("Anet", bufferedStationAliases.alias(1));
        assertEquals("Lausanne", bufferedStationAliases.stationName(0));
        assertEquals("Ins", bufferedStationAliases.stationName(1));
    }


    @Test
    void testSizeStationAliasesBd() {
        List<String> stringTable = new ArrayList<>();
        stringTable.add("1");
        stringTable.add("70");
        stringTable.add("Anet");
        stringTable.add("Ins");
        stringTable.add("Lausanne");
        stringTable.add("Losanna");
        stringTable.add("Palézieux");


        int bytePerUnit = 4;
        for (int i = 0; i < 100; i++) {
            ByteBuffer byteBuffer = byteBufferOfLength(i * bytePerUnit);
            BufferedStationAliases bufferedStationAliases = new BufferedStationAliases(stringTable, byteBuffer);
            assertEquals(i, bufferedStationAliases.size());
        }
    }


    @Test
    void testAliasGettersStationsAliasesBd() {
        int n = 100;
        List<String> stringTable = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append("yolo");
            stringTable.add(sb.toString());
        }


        int bytePerUnit = 4;
        int offset = 1;
        for (int i = 0; i < n; i++) {
            int[] byteAsInt = new int[i * bytePerUnit];
            for (int j = 0; j < i; j++) {
                byteAsInt[j * bytePerUnit + offset] = j;
            }
            byte[] aliasesByte = toByte(byteAsInt);
            ByteBuffer byteBuffer = ByteBuffer.wrap(aliasesByte);


//            System.out.println(Arrays.toString(aliasesByte));


            BufferedStationAliases bufferedStationAliases = new BufferedStationAliases(stringTable, byteBuffer);
            for (int j = 0; j < i; j++) {
//                System.out.printf("here's my j: %d; size: %d\n", j, bufferedStations.size());
                assertEquals(stringTable.get(j), bufferedStationAliases.alias(j));
            }
        }
    }


    @Test
    void testStationNameGettersStationsAliasesBd() {
        int n = 100;
        List<String> stringTable = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append("yolo");
            stringTable.add(sb.toString());
        }


        int bytePerUnit = 4;
        int offset = 3;
        for (int i = 0; i < n; i++) {
            int[] byteAsInt = new int[i * bytePerUnit];
            for (int j = 0; j < i; j++) {
                byteAsInt[j * bytePerUnit + offset] = j;
            }
            byte[] aliasesByte = toByte(byteAsInt);
            ByteBuffer byteBuffer = ByteBuffer.wrap(aliasesByte);


//            System.out.println(Arrays.toString(aliasesByte));


            BufferedStationAliases bufferedStationAliases = new BufferedStationAliases(stringTable, byteBuffer);
            for (int j = 0; j < i; j++) {
//                System.out.printf("here's my j: %d; size: %d\n", j, bufferedStations.size());
                assertEquals(stringTable.get(j), bufferedStationAliases.stationName(j));
            }
        }
    }


    @Test
    void testInvalidLengthStationsAliasesBd() {
        List<String> stringTable = new ArrayList<>();
        stringTable.add("1");
        stringTable.add("70");
        stringTable.add("Anet");
        stringTable.add("Ins");
        stringTable.add("Lausanne");
        stringTable.add("Losanna");
        stringTable.add("Palézieux");


        int bytePerUnit = 4;
        for (int i = 0; i < 100; i++) {
            for (int j = 1; j < bytePerUnit; j++) {
                ByteBuffer byteBuffer = byteBufferOfLength(i * bytePerUnit + j);
                assertThrows(IllegalArgumentException.class, () -> {
                    BufferedStationAliases bufferedStationAliases = new BufferedStationAliases(stringTable, byteBuffer);
                });
            }
        }
    }

}
