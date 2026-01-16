package client.cli;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import client.data.XYPair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

class ScreenBufferTest {
    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @AfterEach
    void tearDown() {
        System.setOut(standardOut);
    }

    @Test
    void Constructor_NullDimensions_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new ScreenBuffer(null));
    }

    @Test
    void DrawTile_NullOffset_ThrowsIllegalArgumentException() {
        var buffer = new ScreenBuffer(new XYPair(1, 1));
        assertThrows(IllegalArgumentException.class, () -> buffer.drawTile(null, new String[]{"abc"}));
    }

    @Test
    void DrawTile_NullLines_ThrowsIllegalArgumentException() {
        var buffer = new ScreenBuffer(new XYPair(1, 1));
        assertThrows(IllegalArgumentException.class, () -> buffer.drawTile(new XYPair(0, 0), null));
    }

    @Test
    void Print_EmptyBuffer_OutputsEmptyStringsWithoutSpaces() {
        var buffer = new ScreenBuffer(new XYPair(1, 1));
        buffer.print();

        String output = outputStreamCaptor.toString().trim();
        assertThat(output, emptyString());
    }

    @Test
    void DrawTile_ValidTile_CorrectlyPlacesAndPrintsContent() {
        var buffer = new ScreenBuffer(new XYPair(2, 2));

        String[] tile = {
                "AAA",
                "BBB",
                "CCC"
        };

        buffer.drawTile(new XYPair(1, 1), tile);
        buffer.print();

        String output = outputStreamCaptor.toString();
        assertThat(output, containsString("AAA"));
        assertThat(output, containsString("BBB"));
        assertThat(output, containsString("CCC"));
    }

    @Test
    void Print_WithSpaces_ReplacesSpacesCorrectly() {
        var buffer = new ScreenBuffer(new XYPair(1, 1));

        String[] tile = {
                "A B",
                "C D",
                "E F"
        };

        buffer.drawTile(new XYPair(0, 0), tile);
        buffer.print();

        String output = outputStreamCaptor.toString();
        assertThat(output, containsString("AB"));
        assertThat(output, containsString("CD"));
        assertThat(output, containsString("EF"));
        assertThat(output, not(containsString("A B")));
    }
}