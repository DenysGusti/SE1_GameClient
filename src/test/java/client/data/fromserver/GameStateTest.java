package client.data.fromserver;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class GameStateTest {

    @Mock
    private FullMap fullMapMock;
    @Mock
    private PlayerState myPlayerMock;

    private GameState gameState;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        gameState = new GameState("game-id", fullMapMock, myPlayerMock, null);
    }

    @Test
    @DisplayName("Constructor should throw NullPointerException for null arguments")
    void constructor_nullArgs_shouldThrowNPE() {
        assertThrows(NullPointerException.class, () -> new GameState(null, fullMapMock, myPlayerMock, null));
        assertThrows(NullPointerException.class, () -> new GameState("id", null, myPlayerMock, null));
        assertThrows(NullPointerException.class, () -> new GameState("id", fullMapMock, null, null));
    }

    @Test
    @DisplayName("myPlayerMustAct should delegate to myPlayer")
    void myPlayerMustAct() {
        when(myPlayerMock.mustAct()).thenReturn(true);
        assertTrue(gameState.myPlayerMustAct());
    }

    @Test
    @DisplayName("myPlayerMustWait should delegate to myPlayer")
    void myPlayerMustWait() {
        when(myPlayerMock.mustWait()).thenReturn(true);
        assertTrue(gameState.myPlayerMustWait());
    }
}