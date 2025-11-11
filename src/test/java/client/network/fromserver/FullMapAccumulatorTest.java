package client.network.fromserver;

import client.data.fromserver.FullMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class FullMapAccumulatorTest {

    @Mock
    private FullMapRevealer revealerMock;

    @InjectMocks
    private FullMapAccumulator accumulator;

    @Mock
    private FullMap newTurnMapMock, revealedMapMock, treasureRevealedMapMock, combinedMapMock;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("First accumulation should call reveal and set map")
    void accumulateFullMap_firstCall() {
        when(revealerMock.revealCoordinatesFromMyPlayer(newTurnMapMock)).thenReturn(revealedMapMock);

        accumulator.accumulateFullMap(newTurnMapMock, false);

        verify(revealerMock, times(1)).revealCoordinatesFromMyPlayer(newTurnMapMock);
        verify(revealerMock, never()).revealMyTreasureFromMyPlayer(any());
        verify(revealerMock, never()).combineRevealedNodes(any(), any());
        assertEquals(revealedMapMock, accumulator.getFullMap());
    }

    @Test
    @DisplayName("Treasure collection should trigger treasure reveal")
    void accumulateFullMap_treasureCollected() {
        when(revealerMock.revealCoordinatesFromMyPlayer(newTurnMapMock)).thenReturn(revealedMapMock);
        when(revealerMock.revealMyTreasureFromMyPlayer(revealedMapMock)).thenReturn(treasureRevealedMapMock);

        accumulator.accumulateFullMap(newTurnMapMock, true); // hasCollectedTreasure = true

        verify(revealerMock, times(1)).revealCoordinatesFromMyPlayer(newTurnMapMock);
        verify(revealerMock, times(1)).revealMyTreasureFromMyPlayer(revealedMapMock);
        assertEquals(treasureRevealedMapMock, accumulator.getFullMap());
    }

    @Test
    @DisplayName("Treasure reveal should only happen once")
    void accumulateFullMap_treasureRevealOnlyOnce() {
        when(revealerMock.revealCoordinatesFromMyPlayer(any())).thenReturn(revealedMapMock);
        when(revealerMock.revealMyTreasureFromMyPlayer(any())).thenReturn(treasureRevealedMapMock);

        // First turn (collects treasure)
        accumulator.accumulateFullMap(newTurnMapMock, true);

        // Second turn (treasure already revealed)
        accumulator.accumulateFullMap(newTurnMapMock, true);

        // Verify revealMyTreasure was only called ONCE
        verify(revealerMock, times(1)).revealMyTreasureFromMyPlayer(any());
    }

    @Test
    @DisplayName("Subsequent accumulations should call combine")
    void accumulateFullMap_secondCall() {
        when(revealerMock.revealCoordinatesFromMyPlayer(newTurnMapMock)).thenReturn(revealedMapMock);

        // First call
        accumulator.accumulateFullMap(newTurnMapMock, false);
        FullMap firstMap = accumulator.getFullMap(); // this is revealedMapMock

        // Second call
        when(revealerMock.combineRevealedNodes(firstMap, revealedMapMock)).thenReturn(combinedMapMock);
        accumulator.accumulateFullMap(newTurnMapMock, false);

        verify(revealerMock, times(1)).combineRevealedNodes(firstMap, revealedMapMock);
        assertEquals(combinedMapMock, accumulator.getFullMap());
    }
}