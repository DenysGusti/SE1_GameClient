package client.modelviewcontroller.view;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import client.data.PlayerInformation;
import client.data.fromserver.EPlayerGameState;
import client.data.fromserver.PlayerState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

class PlayerViewTest {
    private PlayerView playerView;
    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    private final String testHeader = "MY_PLAYER";

    @BeforeEach
    void setUp() {
        playerView = new PlayerView(testHeader);
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @AfterEach
    void tearDown() {
        System.setOut(standardOut);
    }

    @Test
    void Constructor_NullHeader_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new PlayerView(null));
    }

    @Test
    void Update_NullPlayerState_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> playerView.update(null));
    }

    @Test
    void Update_MustActWithNoTreasure_PrintsCorrectInfoAndEmojis() {
        var playerInformation = new PlayerInformation("John", "Doe", "jdoe1");
        var state = mock(PlayerState.class);

        when(state.playerInformation()).thenReturn(playerInformation);
        when(state.hasCollectedTreasure()).thenReturn(false);
        when(state.gameState()).thenReturn(EPlayerGameState.MustAct);

        playerView.update(state);

        String output = outputStreamCaptor.toString();
        assertThat(output, containsString(testHeader + ":"));
        assertThat(output, containsString("John Doe jdoe1"));
        assertThat(output, containsString("😐"));
        assertThat(output, containsString("🤔"));
    }

    @Test
    void Update_WonWithTreasure_CoversRemainingEmojiBranches() {
        var playerInformation = new PlayerInformation("Jane", "Smith", "jsmith2");
        var state = mock(PlayerState.class);

        when(state.playerInformation()).thenReturn(playerInformation);
        when(state.hasCollectedTreasure()).thenReturn(true);
        when(state.gameState()).thenReturn(EPlayerGameState.Won);

        playerView.update(state);

        String output = outputStreamCaptor.toString();
        assertThat(output, containsString(" Jane Smith jsmith2"));
        assertThat(output, containsString("🤑"));
        assertThat(output, containsString("🥳"));
    }

    @Test
    void Update_RemainingGameStateEmojis_CoversWaitAndLost() {
        var playerInformation = new PlayerInformation("A", "B", "C");
        PlayerState state = mock(PlayerState.class);
        when(state.playerInformation()).thenReturn(playerInformation);
        when(state.hasCollectedTreasure()).thenReturn(false);

        when(state.gameState()).thenReturn(EPlayerGameState.MustWait);
        playerView.update(state);
        assertThat(outputStreamCaptor.toString(), containsString("⏳"));

        outputStreamCaptor.reset();
        when(state.gameState()).thenReturn(EPlayerGameState.Lost);
        playerView.update(state);
        assertThat(outputStreamCaptor.toString(), containsString("😭"));
    }

    @Test
    void Update_NullPlayerInformationInState_ThrowsIllegalArgumentException() {
        var state = mock(PlayerState.class);
        when(state.playerInformation()).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> playerView.update(state));
    }
}