package ch.epfl.rechor;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

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

}
