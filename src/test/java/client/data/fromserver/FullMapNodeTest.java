package client.data.fromserver;

import client.data.ETerrain;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FullMapNodeTest {

    @Test
    @DisplayName("Terrain type check should be correct")
    void terrainTypeChecks() {
        assertTrue(new FullMapNode(ETerrain.Grass, false).isGrass());
        assertTrue(new FullMapNode(ETerrain.Mountain, false).isMountain());
        assertTrue(new FullMapNode(ETerrain.Water, false).isWater());
        assertFalse(new FullMapNode(ETerrain.Grass, false).isMountain());
    }

    @Test
    @DisplayName("withIsRevealed should create new node with updated flag")
    void withIsRevealed() {
        FullMapNode node = new FullMapNode(ETerrain.Grass, false);
        FullMapNode revealedNode = node.withIsRevealed(true);

        assertNotSame(node, revealedNode);
        assertFalse(node.isRevealed());
        assertTrue(revealedNode.isRevealed());
    }
}