package client.network.fromserver;

import client.data.XYPair;
import client.data.fromserver.FullMap;
import messagesbase.messagesfromclient.ETerrain;
import messagesbase.messagesfromserver.EPlayerPositionState;
import messagesbase.messagesfromserver.ETreasureState;
import messagesbase.messagesfromserver.EFortState;
import messagesbase.messagesfromserver.FullMapNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;

class FullMapConverterTest {

    private FullMapConverter converter;

    @BeforeEach
    void setUp() {
        converter = new FullMapConverter();
    }

    @Test
    @DisplayName("Should return an empty FullMap when server map is empty")
    void convertFullMap_emptyServerMap() {
        var serverMap = new messagesbase.messagesfromserver.FullMap();
        FullMap clientMap = converter.convertFullMap(serverMap);

        assertThat(clientMap.isEmpty(), is(true));
        assertThat(clientMap.getOptionalTopLeftCoordinate().isEmpty(), is(true));
    }

    @Test
    @DisplayName("Should correctly calculate coordinates for a single half-map")
    void convertFullMap_halfMap_calculatesCoords() {
        // Simulates a map placed in the bottom-right quadrant
        var node1 = new FullMapNode(ETerrain.Grass, EPlayerPositionState.NoPlayerPresent,
                ETreasureState.NoOrUnknownTreasureState, EFortState.NoOrUnknownFortState, 10, 5);
        var node2 = new FullMapNode(ETerrain.Grass, EPlayerPositionState.NoPlayerPresent,
                ETreasureState.NoOrUnknownTreasureState, EFortState.NoOrUnknownFortState, 19, 9);

        var serverMap = new messagesbase.messagesfromserver.FullMap(List.of(node1, node2));
        FullMap clientMap = converter.convertFullMap(serverMap);

        // Check that the bounding box is correct
        assertAll(
                () -> assertThat(clientMap.topLeftCoordinate(), is(new XYPair(10, 5))),
                () -> assertThat(clientMap.bottomRightCoordinate(), is(new XYPair(19, 9)))
        );
    }

    @Test
    @DisplayName("Should correctly convert all map object types")
    void convertFullMap_allNodeTypes() {
        var myPlayerNode = new FullMapNode(ETerrain.Grass, EPlayerPositionState.MyPlayerPosition,
                ETreasureState.NoOrUnknownTreasureState, EFortState.NoOrUnknownFortState, 0, 0);
        var enemyPlayerNode = new FullMapNode(ETerrain.Grass, EPlayerPositionState.EnemyPlayerPosition,
                ETreasureState.NoOrUnknownTreasureState, EFortState.NoOrUnknownFortState, 1, 0);
        var myFortNode = new FullMapNode(ETerrain.Grass, EPlayerPositionState.NoPlayerPresent,
                ETreasureState.NoOrUnknownTreasureState, EFortState.MyFortPresent, 2, 0);
        var enemyFortNode = new FullMapNode(ETerrain.Grass, EPlayerPositionState.NoPlayerPresent,
                ETreasureState.NoOrUnknownTreasureState, EFortState.EnemyFortPresent, 3, 0);
        var myTreasureNode = new FullMapNode(ETerrain.Grass, EPlayerPositionState.NoPlayerPresent,
                ETreasureState.MyTreasureIsPresent, EFortState.NoOrUnknownFortState, 4, 0);

        var serverMap = new messagesbase.messagesfromserver.FullMap(
                List.of(myPlayerNode, enemyPlayerNode, myFortNode, enemyFortNode, myTreasureNode));
        FullMap clientMap = converter.convertFullMap(serverMap);

        assertAll(
                () -> assertThat(clientMap.myPlayerPosition(), is(new XYPair(0, 0))),
                () -> assertThat(clientMap.enemyPlayerPosition(), is(new XYPair(1, 0))),
                () -> assertThat(clientMap.myFortPosition(), is(new XYPair(2, 0))),
                () -> assertThat(clientMap.enemyFortPosition(), is(new XYPair(3, 0))),
                () -> assertThat(clientMap.myTreasurePosition(), is(new XYPair(4, 0)))
        );
    }
}