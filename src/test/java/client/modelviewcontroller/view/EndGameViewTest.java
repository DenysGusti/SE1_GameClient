package client.modelviewcontroller.view;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import client.data.fromserver.EPlayerGameState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

class EndGameViewTest {
    private EndGameView endGameView;
    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @BeforeEach
    void setUp() {
        endGameView = new EndGameView();
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @AfterEach
    void tearDown() {
        System.setOut(standardOut);
    }

    @Test
    void Update_NullPlayerGameState_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> endGameView.update(null));
    }

    @Test
    void Update_PlayerWon_PrintsVictoryMessage() {
        endGameView.update(EPlayerGameState.Won);

        String output = outputStreamCaptor.toString();
        assertThat(output, containsString("🥳 VICTORY!"));
        assertThat(output, containsString("GAME FINISHED"));
        assertThat(output, containsString("========================================"));
    }

    @Test
    void Update_PlayerLost_PrintsDefeatMessage() {
        endGameView.update(EPlayerGameState.Lost);

        String output = outputStreamCaptor.toString();
        assertThat(output, containsString("😭  DEFEAT!"));
        assertThat(output, containsString("GAME FINISHED"));
        assertThat(output, containsString("========================================"));
    }
}