package client.network.accumulator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import client.data.XYPair;
import client.data.fromserver.FullMap;

public class FullMapAccumulatorTest {
    private FullMapAccumulator accumulator;
    private FullMap mapMock;

    @BeforeEach
    public void setUp() {
        var revealerMock = mock(FullMapRevealer.class);
        accumulator = new FullMapAccumulator(revealerMock);
        mapMock = mock(FullMap.class);

        when(revealerMock.revealCoordinatesFromMyPlayer(any())).thenAnswer(inv -> inv.getArgument(0));
        when(revealerMock.combineRevealedMyTreasure(any(), any())).thenAnswer(inv -> inv.getArgument(1));
        when(revealerMock.combineRevealedNodes(any(), any())).thenAnswer(inv -> inv.getArgument(1));
    }

    @Test
    public void NewAccumulator_GetFullMap_ReturnsNull() {
        assertThat(accumulator.getFullMap(), nullValue());
    }

    @Test
    public void FirstTurn_AccumulateFullMap_InitializesMap() {
        when(mapMock.isMyTreasureCollected()).thenReturn(false);
        accumulator.accumulateFullMap(mapMock);
        assertThat(accumulator.getFullMap(), is(mapMock));
    }

    @Test
    public void FirstTurnImpossibleCase_AccumulateFullMap_SetsTreasureToPlayerPos() {
        var playerPosition = new XYPair(1, 1);
        when(mapMock.isMyTreasureCollected()).thenReturn(true);
        when(mapMock.myPlayerPosition()).thenReturn(playerPosition);
        when(mapMock.withMyTreasurePosition(playerPosition)).thenReturn(mapMock);

        accumulator.accumulateFullMap(mapMock);
        assertThat(accumulator.getFullMap(), is(mapMock));
    }

    @Test
    public void TreasureTransition_AccumulateFullMap_SetsTreasurePositionOnCollection() {
        var firstMap = mock(FullMap.class);
        var secondMap = mock(FullMap.class);
        var playerPosition = new XYPair(5, 5);

        when(firstMap.isMyTreasureCollected()).thenReturn(false);
        when(secondMap.isMyTreasureCollected()).thenReturn(true);
        when(secondMap.myPlayerPosition()).thenReturn(playerPosition);
        when(secondMap.withMyTreasurePosition(playerPosition)).thenReturn(secondMap);

        accumulator.accumulateFullMap(firstMap);
        accumulator.accumulateFullMap(secondMap);

        assertThat(accumulator.getFullMap(), is(secondMap));
    }

    @Test
    public void TreasureAlreadyCollected_AccumulateFullMap_StaysCollected() {
        var firstMap = mock(FullMap.class);
        var secondMap = mock(FullMap.class);
        var position = new XYPair(0,0);

        when(firstMap.isMyTreasureCollected()).thenReturn(true);
        when(firstMap.myPlayerPosition()).thenReturn(position);
        when(firstMap.withMyTreasurePosition(any())).thenReturn(firstMap);
        when(secondMap.isMyTreasureCollected()).thenReturn(true);

        accumulator.accumulateFullMap(firstMap);
        accumulator.accumulateFullMap(secondMap);

        assertThat(accumulator.getFullMap(), is(secondMap));
    }

    @Test
    public void TreasureNotYetCollected_AccumulateFullMap_RemainsUncollected() {
        var firstMap = mock(FullMap.class);
        var secondMap = mock(FullMap.class);

        when(firstMap.isMyTreasureCollected()).thenReturn(false);
        when(secondMap.isMyTreasureCollected()).thenReturn(false);

        accumulator.accumulateFullMap(firstMap);
        accumulator.accumulateFullMap(secondMap);

        assertThat(accumulator.getFullMap(), is(secondMap));
    }

    @Test
    public void NullMap_AccumulateFullMap_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> accumulator.accumulateFullMap(null));
    }
}