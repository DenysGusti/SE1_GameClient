package client.data.fromserver;

import client.data.ETerrain;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FullMapNodeTest {

    @Test
    @DisplayName("Helper methods return correct boolean for each terrain type")
    void VariousTerrainNodes_CheckTerrainType_ReturnsCorrectBoolean() {
        var grass = new FullMapNode(ETerrain.Grass, false);
        var mountain = new FullMapNode(ETerrain.Mountain, false);
        var water = new FullMapNode(ETerrain.Water, false);

        assertAll(
                () -> assertTrue(grass.isGrass()),
                () -> assertFalse(grass.isMountain()),
                () -> assertFalse(grass.isWater())
        );

        assertAll(
                () -> assertFalse(mountain.isGrass()),
                () -> assertTrue(mountain.isMountain()),
                () -> assertFalse(mountain.isWater())
        );

        assertAll(
                () -> assertFalse(water.isGrass()),
                () -> assertFalse(water.isMountain()),
                () -> assertTrue(water.isWater())
        );
    }

    @Test
    @DisplayName("withIsRevealed should create a new node and not mutate the original")
    void UnrevealedNode_WithIsRevealedTrue_ReturnsNewRevealedNodeAndKeepsOriginalImmutable() {
        FullMapNode node = new FullMapNode(ETerrain.Grass, false);
        FullMapNode revealedNode = node.withIsRevealed(true);

        assertAll(
                () -> assertNotSame(node, revealedNode, "A new object should be created"),
                () -> assertFalse(node.isRevealed(), "Original node should not change"),
                () -> assertTrue(revealedNode.isRevealed(), "New node should be revealed")
        );
    }
}