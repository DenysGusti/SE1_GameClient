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
    @DisplayName("Constructor should throw NullPointerException for null arguments")
    void constructor_nullArgs_shouldThrowNPE() {
        assertThrows(NullPointerException.class, () -> new PlayerState(null, false, EPlayerGameState.MustAct));
        assertThrows(NullPointerException.class, () -> new PlayerState(testPlayerInfo, false, null));
    }

    @Test
    @DisplayName("Method logic should match enum state")
    void stateMethods() {
        var actState = new PlayerState(testPlayerInfo, false, EPlayerGameState.MustAct);
        assertTrue(actState.mustAct());
        assertFalse(actState.mustWait());

        var waitState = new PlayerState(testPlayerInfo, false, EPlayerGameState.MustWait);
        assertTrue(waitState.mustWait());
        assertFalse(waitState.mustAct());

        var wonState = new PlayerState(testPlayerInfo, false, EPlayerGameState.Won);
        assertTrue(wonState.won());
        assertFalse(wonState.lost());

        var lostState = new PlayerState(testPlayerInfo, false, EPlayerGameState.Lost);
        assertTrue(lostState.lost());
        assertFalse(lostState.won());
    }
}