package client.data.fromserver;

import client.data.PlayerInformation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerStateTest {

    private static PlayerInformation testPlayerInfo;

    @BeforeAll
    static void setUp() {
        testPlayerInfo = new PlayerInformation("Test", "User", "testuser");
    }

    @Test
    @DisplayName("Negative Test: Constructor throws exception for null arguments")
    void NullInfoOrState_ConstructPlayerState_ThrowsNullPointerException() {
        assertAll(
                () -> assertThrows(NullPointerException.class, () -> new PlayerState(null, false, EPlayerGameState.MustAct)),
                () -> assertThrows(NullPointerException.class, () -> new PlayerState(testPlayerInfo, false, null))
        );
    }

    @Test
    @DisplayName("Helper methods return correct boolean based on state enum")
    void VariousPlayerStates_CheckStateMethods_ReturnCorrectBooleans() {
        var actState = new PlayerState(testPlayerInfo, false, EPlayerGameState.MustAct);
        var waitState = new PlayerState(testPlayerInfo, false, EPlayerGameState.MustWait);
        var wonState = new PlayerState(testPlayerInfo, false, EPlayerGameState.Won);
        var lostState = new PlayerState(testPlayerInfo, false, EPlayerGameState.Lost);

        assertAll(
                () -> assertTrue(actState.mustAct()),
                () -> assertFalse(actState.mustWait())
        );

        assertAll(
                () -> assertTrue(waitState.mustWait()),
                () -> assertFalse(waitState.mustAct())
        );

        assertAll(
                () -> assertTrue(wonState.won()),
                () -> assertFalse(wonState.lost())
        );

        assertAll(
                () -> assertTrue(lostState.lost()),
                () -> assertFalse(lostState.won())
        );
    }
}