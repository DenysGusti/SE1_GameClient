package client.network.accumulator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import client.data.XYPair;
import client.data.fromserver.FullMap;
import client.data.fromserver.FullMapNode;

public class FullMapRevealerTest {
    private FullMapRevealer revealer;
    private FullMap fullMapMock;
    private static final XYPair playerPosition = new XYPair(5, 5);

    @BeforeEach
    public void setUp() {
        revealer = new FullMapRevealer();
        fullMapMock = mock(FullMap.class);
    }

    @Test
    public void PlayerOnGrass_RevealFromMyPlayer_OnlyPlayerPositionRevealed() {
        var node = mock(FullMapNode.class);
        var updatedNode = mock(FullMapNode.class);

        when(fullMapMock.getOptionalMyPlayerPosition()).thenReturn(Optional.of(playerPosition));
        when(fullMapMock.isMountain(playerPosition)).thenReturn(false);
        when(fullMapMock.nodes()).thenReturn(Map.of(playerPosition, node));
        when(node.withIsRevealed(true)).thenReturn(updatedNode);
        when(fullMapMock.withNodes(anyMap())).thenReturn(fullMapMock);

        var result = revealer.revealCoordinatesFromMyPlayer(fullMapMock);
        assertThat(result, is(fullMapMock));
    }

    @Test
    public void PlayerOnMountain_RevealFromMyPlayer_PlayerAndNeighborsRevealed() {
        var mapSize = new XYPair(10, 10);
        var nodes = new HashMap<XYPair, FullMapNode>();

        var expectedCoordinates = playerPosition.getAllNeighborsWithThis(mapSize);
        for (XYPair coordinate : expectedCoordinates) {
            var nodeMock = mock(FullMapNode.class);
            when(nodeMock.withIsRevealed(any(Boolean.class))).thenReturn(nodeMock);
            nodes.put(coordinate, nodeMock);
        }

        when(fullMapMock.getOptionalMyPlayerPosition()).thenReturn(Optional.of(playerPosition));
        when(fullMapMock.isMountain(playerPosition)).thenReturn(true);
        when(fullMapMock.size()).thenReturn(mapSize);
        when(fullMapMock.nodes()).thenReturn(nodes);
        when(fullMapMock.withNodes(anyMap())).thenReturn(fullMapMock);

        var result = revealer.revealCoordinatesFromMyPlayer(fullMapMock);
        assertThat(result, is(fullMapMock));
    }

    @Test
    public void ExistingTreasure_CombineRevealedMyTreasure_OldTreasureIsMaintained() {
        var oldMap = mock(FullMap.class);
        var newMap = mock(FullMap.class);
        var treasurePosition = new XYPair(1, 1);

        when(oldMap.getOptionalMyTreasurePosition()).thenReturn(Optional.of(treasurePosition));
        when(newMap.withMyTreasurePosition(treasurePosition)).thenReturn(newMap);

        var result = revealer.combineRevealedMyTreasure(oldMap, newMap);
        assertThat(result, is(newMap));
    }

    @Test
    public void RevealedNodesExist_CombineRevealedNodes_StateIsMerged() {
        var oldMap = mock(FullMap.class);
        var newMap = mock(FullMap.class);
        var position = new XYPair(0, 0);
        var oldNode = mock(FullMapNode.class);

        when(oldNode.isRevealed()).thenReturn(true);
        when(oldMap.nodes()).thenReturn(Map.of(position, oldNode));
        when(newMap.nodes()).thenReturn(Map.of(position, mock(FullMapNode.class)));
        when(newMap.isRevealed(position)).thenReturn(false);
        when(oldNode.withIsRevealed(true)).thenReturn(oldNode);
        when(newMap.withNodes(anyMap())).thenReturn(newMap);

        var result = revealer.combineRevealedNodes(oldMap, newMap);
        assertThat(result, is(newMap));
    }

    @Test
    public void UnrevealedNodes_CombineRevealedNodes_RemainsUnrevealed() {
        var oldMap = mock(FullMap.class);
        var newMap = mock(FullMap.class);
        var position = new XYPair(0, 0);
        var oldNode = mock(FullMapNode.class);

        when(oldNode.isRevealed()).thenReturn(false);
        when(newMap.isRevealed(position)).thenReturn(false);
        when(oldMap.nodes()).thenReturn(Map.of(position, oldNode));
        when(newMap.nodes()).thenReturn(Map.of(position, mock(FullMapNode.class)));
        when(oldNode.withIsRevealed(false)).thenReturn(oldNode);
        when(newMap.withNodes(anyMap())).thenReturn(newMap);

        var result = revealer.combineRevealedNodes(oldMap, newMap);
        assertThat(result, is(newMap));
    }

    @Test
    public void NullFullMap_RevealFromMyPlayer_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> revealer.revealCoordinatesFromMyPlayer(null));
    }

    @Test
    public void NullOldMap_CombineRevealedMyTreasure_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> revealer.combineRevealedMyTreasure(null, fullMapMock));
    }

    @Test
    public void NullNewMap_CombineRevealedMyTreasure_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> revealer.combineRevealedMyTreasure(fullMapMock, null));
    }

    @Test
    public void NullOldMap_CombineRevealedNodes_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> revealer.combineRevealedNodes(null, fullMapMock));
    }

    @Test
    public void NullNewMap_CombineRevealedNodes_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> revealer.combineRevealedNodes(fullMapMock, null));
    }
}