package client.network.fromserver;

import client.data.ETerrain;
import client.data.XYPair;
import client.data.fromserver.FullMap;
import client.data.fromserver.FullMapNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

class FullMapRevealerTest {

    private FullMapRevealer revealer;
    private Map<XYPair, FullMapNode> nodes;
    private final XYPair mapSize = new XYPair(10, 5);
    private final XYPair playerOnGrass = new XYPair(1, 1);
    private final XYPair playerOnMountain = new XYPair(3, 3);
    private FullMap baseMap;

    @BeforeEach
    void setUp() {
        revealer = new FullMapRevealer();
        nodes = new HashMap<>();
        for (int y = 0; y < mapSize.y(); y++) {
            for (int x = 0; x < mapSize.x(); x++) {
                nodes.put(new XYPair(x, y), new FullMapNode(ETerrain.Grass, false));
            }
        }
        nodes.put(playerOnMountain, new FullMapNode(ETerrain.Mountain, false));

        baseMap = new FullMap(nodes, new XYPair(0,0), new XYPair(9,4),
                null, null, null, null, null);
    }

    @Test
    @DisplayName("Should reveal only player's coordinate when on Grass")
    void revealCoordinates_playerOnGrass() {
        FullMap map = baseMap.withMyTreasurePosition(null) // Using 'with' to create a new instance with player
                .withNodes(nodes)
                .withMyPlayerPosition(playerOnGrass);

        FullMap newMap = revealer.revealCoordinatesFromMyPlayer(map);

        assertThat(newMap.nodes().get(playerOnGrass).isRevealed(), is(true));
        assertThat(newMap.nodes().values().stream().filter(FullMapNode::isRevealed).count(), is(1L));
    }

    @Test
    @DisplayName("Should reveal 9 coordinates (player + 8 neighbors) when on Mountain")
    void revealCoordinates_playerOnMountain() {
        FullMap map = baseMap.withMyTreasurePosition(null)
                .withNodes(nodes)
                .withMyPlayerPosition(playerOnMountain);

        FullMap newMap = revealer.revealCoordinatesFromMyPlayer(map);

        // Use the map's real size for neighbor calculation
        Set<XYPair> expectedRevealed = playerOnMountain.getAllNeighborsWithThis(map.size());

        assertThat(newMap.nodes().values().stream().filter(FullMapNode::isRevealed).count(), is(9L));
        for (XYPair coordinate : expectedRevealed) {
            assertThat(newMap.nodes().get(coordinate).isRevealed(), is(true));
        }
    }

    @Test
    @DisplayName("combineRevealedNodes should merge revealed nodes")
    void combineRevealedNodes() {
        var coordinate1 = new XYPair(1, 1);
        var coordinate2 = new XYPair(2, 2);

        Map<XYPair, FullMapNode> nodes1 = new HashMap<>(nodes);
        nodes1.replace(coordinate1, new FullMapNode(ETerrain.Grass, true));
        FullMap map1 = baseMap.withNodes(nodes1);

        Map<XYPair, FullMapNode> nodes2 = new HashMap<>(nodes);
        nodes2.replace(coordinate2, new FullMapNode(ETerrain.Grass, true));
        FullMap map2 = baseMap.withNodes(nodes2);

        FullMap combinedMap = revealer.combineRevealedNodes(map1, map2);

        assertTrue(combinedMap.nodes().get(coordinate1).isRevealed());
        assertTrue(combinedMap.nodes().get(coordinate2).isRevealed());
    }

    @Test
    @DisplayName("revealMyTreasure should set treasure to player position")
    void revealMyTreasure() {
        FullMap map = baseMap.withMyTreasurePosition(null)
                .withNodes(nodes)
                .withMyPlayerPosition(playerOnGrass);

        FullMap newMap = revealer.revealMyTreasureFromMyPlayer(map);

        assertEquals(playerOnGrass, newMap.myTreasurePosition());
    }
}