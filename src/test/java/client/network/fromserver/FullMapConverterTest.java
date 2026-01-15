package client.network.fromserver;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import client.data.XYPair;
import messagesbase.messagesfromserver.EFortState;
import messagesbase.messagesfromserver.EPlayerPositionState;
import messagesbase.messagesfromserver.ETreasureState;
import messagesbase.messagesfromclient.ETerrain;
import messagesbase.messagesfromserver.FullMapNode;

public class FullMapConverterTest {
    private FullMapConverter converter;

    @BeforeEach
    public void setUp() {
        converter = new FullMapConverter();
    }

    @Test
    public void EmptyMap_ConvertFullMap_ReturnsEmptyFullMap() {
        messagesbase.messagesfromserver.FullMap serverMap = mock(messagesbase.messagesfromserver.FullMap.class);
        when(serverMap.isEmpty()).thenReturn(true);

        var result = converter.convertFullMap(serverMap, false);

        assertThat(result.nodes().isEmpty(), is(true));
    }

    @Test
    public void SingleNodeWithAllStates_ConvertFullMap_MapsPropertiesCorrectly() {
        var node = mock(FullMapNode.class);
        var coordinate = new XYPair(2, 3);

        when(node.getX()).thenReturn(coordinate.x());
        when(node.getY()).thenReturn(coordinate.y());
        when(node.getTerrain()).thenReturn(ETerrain.Grass);
        when(node.getPlayerPositionState()).thenReturn(EPlayerPositionState.BothPlayerPosition);
        when(node.getFortState()).thenReturn(EFortState.MyFortPresent);
        when(node.getTreasureState()).thenReturn(ETreasureState.MyTreasureIsPresent);

        messagesbase.messagesfromserver.FullMap serverMap = mock(messagesbase.messagesfromserver.FullMap.class);
        when(serverMap.isEmpty()).thenReturn(false);
        when(serverMap.iterator()).thenReturn(List.of(node).iterator());

        var result = converter.convertFullMap(serverMap, true);

        assertThat(result.getOptionalTopLeftCoordinate().orElseThrow(), is(coordinate));

        assertThat(result.myPlayerPosition(), is(coordinate));
        assertThat(result.enemyPlayerPosition(), is(coordinate));

        assertThat(result.myFortPosition(), is(coordinate));
        assertThat(result.myTreasurePosition(), is(coordinate));
        assertThat(result.isMyTreasureCollected(), is(true));
    }

    @Test
    public void EnemyFortAndPlayer_ConvertFullMap_MapsEnemyStatesCorrectly() {
        var node = mock(FullMapNode.class);
        var coordinate = new XYPair(2, 3);

        when(node.getX()).thenReturn(coordinate.x());
        when(node.getY()).thenReturn(coordinate.y());
        when(node.getTerrain()).thenReturn(ETerrain.Mountain);
        when(node.getPlayerPositionState()).thenReturn(EPlayerPositionState.EnemyPlayerPosition);
        when(node.getFortState()).thenReturn(EFortState.EnemyFortPresent);
        when(node.getTreasureState()).thenReturn(ETreasureState.NoOrUnknownTreasureState);

        messagesbase.messagesfromserver.FullMap serverMap = mock(messagesbase.messagesfromserver.FullMap.class);
        when(serverMap.isEmpty()).thenReturn(false);
        when(serverMap.iterator()).thenReturn(List.of(node).iterator());

        var result = converter.convertFullMap(serverMap, false);

        assertThat(result.enemyPlayerPosition(), is(coordinate));
        assertThat(result.enemyFortPosition(), is(coordinate));
        assertThat(result.myPlayerPosition(), nullValue());
    }

    @Test
    public void MultipleNodes_ConvertFullMap_CalculatesCorrectBoundaries() {
        var coordinate1 = new XYPair(0, 0);
        var coordinate2 = new XYPair(10, 5);

        FullMapNode node1 = createNode(coordinate1);
        FullMapNode node2 = createNode(coordinate2);

        messagesbase.messagesfromserver.FullMap serverMap = mock(messagesbase.messagesfromserver.FullMap.class);
        when(serverMap.isEmpty()).thenReturn(false);
        when(serverMap.iterator()).thenReturn(List.of(node1, node2).iterator());

        var result = converter.convertFullMap(serverMap, false);

        assertThat(result.getOptionalTopLeftCoordinate().orElseThrow(), is(coordinate1));
        assertThat(result.getOptionalBottomRightCoordinate().orElseThrow(), is(coordinate2));
    }

    @Test
    public void NullMap_ConvertFullMap_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> converter.convertFullMap(null, false));
    }

    private FullMapNode createNode(XYPair coordinate) {
        var node = mock(FullMapNode.class);
        when(node.getX()).thenReturn(coordinate.x());
        when(node.getY()).thenReturn(coordinate.y());
        when(node.getTerrain()).thenReturn(ETerrain.Grass);
        when(node.getPlayerPositionState()).thenReturn(EPlayerPositionState.NoPlayerPresent);
        when(node.getFortState()).thenReturn(EFortState.NoOrUnknownFortState);
        when(node.getTreasureState()).thenReturn(ETreasureState.NoOrUnknownTreasureState);
        return node;
    }
}