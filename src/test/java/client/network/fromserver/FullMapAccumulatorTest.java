package client.network.fromserver;

import client.data.fromserver.FullMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertAll;
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
    @DisplayName("First map accumulation should call reveal and set map")
    void NewAccumulator_AccumulateFirstMap_SetsMapAndCallsReveal() {
        when(revealerMock.revealCoordinatesFromMyPlayer(newTurnMapMock)).thenReturn(revealedMapMock);

        accumulator.accumulateFullMap(newTurnMapMock, false);

        assertAll(
                () -> verify(revealerMock, times(1)).revealCoordinatesFromMyPlayer(newTurnMapMock),
                () -> verify(revealerMock, never()).revealMyTreasureFromMyPlayer(any()),
                () -> verify(revealerMock, never()).combineRevealedNodes(any(), any()),
                () -> assertEquals(revealedMapMock, accumulator.getFullMap())
        );
    }

    @Test
    @DisplayName("Treasure collection should trigger treasure reveal")
    void MapWithTreasureCollected_AccumulateMap_CallsTreasureReveal() {
        when(revealerMock.revealCoordinatesFromMyPlayer(newTurnMapMock)).thenReturn(revealedMapMock);
        when(revealerMock.revealMyTreasureFromMyPlayer(revealedMapMock)).thenReturn(treasureRevealedMapMock);

        accumulator.accumulateFullMap(newTurnMapMock, true); // hasCollectedTreasure = true

        assertAll(
                () -> verify(revealerMock, times(1)).revealCoordinatesFromMyPlayer(newTurnMapMock),
                () -> verify(revealerMock, times(1)).revealMyTreasureFromMyPlayer(revealedMapMock),
                () -> assertEquals(treasureRevealedMapMock, accumulator.getFullMap())
        );
    }

    @Test
    @DisplayName("Treasure reveal should only happen once")
    void TreasureAlreadyRevealed_AccumulateMapWithTreasure_TreasureRevealNotCalledAgain() {
        when(revealerMock.revealCoordinatesFromMyPlayer(any())).thenReturn(revealedMapMock);
        when(revealerMock.revealMyTreasureFromMyPlayer(any())).thenReturn(treasureRevealedMapMock);

        accumulator.accumulateFullMap(newTurnMapMock, true); // First turn
        accumulator.accumulateFullMap(newTurnMapMock, true); // Second turn

        verify(revealerMock, times(1)).revealMyTreasureFromMyPlayer(any());
    }

    @Test
    @DisplayName("Subsequent accumulations should call combine")
    void ExistingMap_AccumulateSecondMap_CallsCombineNodes() {
        when(revealerMock.revealCoordinatesFromMyPlayer(newTurnMapMock)).thenReturn(revealedMapMock);

        accumulator.accumulateFullMap(newTurnMapMock, false); // First call
        FullMap firstMap = accumulator.getFullMap();

        when(revealerMock.combineRevealedNodes(firstMap, revealedMapMock)).thenReturn(combinedMapMock);
        accumulator.accumulateFullMap(newTurnMapMock, false); // Second call

        assertAll(
                () -> verify(revealerMock, times(1)).combineRevealedNodes(firstMap, revealedMapMock),
                () -> assertEquals(combinedMapMock, accumulator.getFullMap())
        );
    }
}