package ch.epfl.rechor;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

class IcalBuilderTest {

    // Même terminaison de ligne que dans IcalBuilder
    private static final String CRLF = "\r\n";

    // ----------------------------------------------------
    // Tests pour la méthode textAdd (gestion du pliage)
    // ----------------------------------------------------

    @Test
    void testTextAddEmptyValue() {
        IcalBuilder builder = new IcalBuilder();
        String result = builder.textAdd("NAME", "");
        String expected = "NAME:" + CRLF;
        assertEquals(expected, result,
                "Si la valeur est vide, la ligne doit être 'NAME:' suivie de CRLF.");
    }

    @Test
    void testTextAddLogicalLineExactly75() {
        IcalBuilder builder = new IcalBuilder();
        // "NAME:" fait 5 caractères, on crée donc une valeur de 70 caractères pour atteindre 75.
        String value = "A".repeat(70);
        String result = builder.textAdd("NAME", value);
        String expected = "NAME:" + value + CRLF;
        assertEquals(expected, result,
                "Une ligne logique de 75 caractères ne doit pas être pliée.");
    }

    @Test
    void testTextAddLogicalLine76Characters() {
        IcalBuilder builder = new IcalBuilder();
        // Pour une ligne logique de 76 caractères, avec "NAME:" (5 caractères), il faut une valeur de 71 caractères.
        String value = "B".repeat(71);
        String result = builder.textAdd("NAME", value);
        String logical = "NAME:" + value; // Total = 5 + 71 = 76
        // La première ligne doit contenir 75 caractères, puis la suite sur une deuxième ligne commencant par un espace.
        String expected = logical.substring(0, 75) + CRLF +
                " " + logical.substring(75) + CRLF;
        assertEquals(expected, result,
                "Une ligne logique de 76 caractères doit être pliée en deux lignes.");
    }

    @Test
    void testBeginEndVCALENDAR() {
        IcalBuilder builder = new IcalBuilder();

        String result = builder.begin(IcalBuilder.Component.VCALENDAR).end().build();
        String expected = new StringBuilder().append("BEGIN:").append(IcalBuilder.Component.VCALENDAR).append(CRLF)
                .append("END:").append(IcalBuilder.Component.VCALENDAR).append(CRLF).toString();

        assertEquals(result, expected);
    }

    @Test
    void testExceptionIfNotEnded() {
        IcalBuilder builder = new IcalBuilder();

        assertThrows(IllegalArgumentException.class, () -> {
            builder.begin(IcalBuilder.Component.VCALENDAR).build();
        });

    }

    @Test
    void testExceptionIfNotBegun() {
        IcalBuilder builder = new IcalBuilder();

        assertThrows(IllegalArgumentException.class, () -> {
            builder.end();
        });

    }

    @Test
    void testAddStringLogicalLineExactly75() {
        IcalBuilder builder = new IcalBuilder();
        String value = "A".repeat(69);
        String result = builder.add(IcalBuilder.Name.DTEND, value).build();
        String expected = "DTEND:" + value + CRLF;
        assertEquals(expected, result);
    }

    @Test
    void testAddStringLogicalLine76Characters() {
        IcalBuilder builder = new IcalBuilder();
        String value = "B".repeat(70);
        String result = builder.add(IcalBuilder.Name.DTEND, value).build();
        String logical = "DTEND:" + value; // Total = 6 + 70 = 76
        String expected = logical.substring(0, 75) + CRLF +
                " " + logical.substring(75) + CRLF;
        assertEquals(expected, result);
    }

    @Test
    void testAddDateLogicalLineLessThan75() {
        IcalBuilder builder = new IcalBuilder();
        LocalDateTime value = LocalDateTime.now();
        String result = builder.add(IcalBuilder.Name.DTSTAMP, value).build();
        DateTimeFormatter ICAL_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
        String expected = "DTSTAMP:" + value.format(ICAL_DATE_TIME_FORMAT) + CRLF;
        assertEquals(expected, result);
    }







}
