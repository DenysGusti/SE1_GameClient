package client.ai.utilities;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import client.data.ETerrain;
import client.data.XYPair;
import client.data.fromserver.FullMapNode;

public class FullMapSplitterTest {
    private final XYPair mapSize = new XYPair(20, 5);
    private final XYPair topLeftPosition = new XYPair(1, 1);
    private final XYPair bottomRightPosition = new XYPair(15, 3);

    @Test
    void Constructor_NullPosition_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new FullMapSplitter(null));
    }

    @Test
    void Constructor_FortOnTopLeft_InitializesCorrectSideState() {
        var splitter = new FullMapSplitter(topLeftPosition);
        Map<XYPair, FullMapNode> nodes = new HashMap<>();
        var coordinate = new XYPair(2, 2);
        nodes.put(coordinate, new FullMapNode(ETerrain.Grass, false));

        assertThat(splitter.getUnrevealedGrass(nodes, true), contains(coordinate));
    }

    @Test
    void Constructor_FortOnBottomRight_InitializesCorrectSideState() {
        var splitter = new FullMapSplitter(bottomRightPosition);
        Map<XYPair, FullMapNode> nodes = new HashMap<>();
        var coordinate = new XYPair(15, 3);
        nodes.put(coordinate, new FullMapNode(ETerrain.Grass, false));

        assertThat(splitter.getUnrevealedGrass(nodes, true), contains(coordinate));
    }

    @Test
    void Constructor_FortOnBottomLeft_InitializesCorrectSideState() {
        var splitter = new FullMapSplitter(new XYPair(5, 7));
        Map<XYPair, FullMapNode> nodes = new HashMap<>();
        var coordinate = new XYPair(5, 7);
        nodes.put(coordinate, new FullMapNode(ETerrain.Grass, false));

        assertThat(splitter.getUnrevealedGrass(nodes, true), contains(coordinate));
    }

    @Test
    void GetUnrevealedGrass_MixedNodes_ReturnsOnlyMatchingGrass() {
        var splitter = new FullMapSplitter(topLeftPosition);
        Map<XYPair, FullMapNode> nodes = new HashMap<>();

        var mySideGrass = new XYPair(2, 2);
        var enemySideGrass = new XYPair(12, 2);
        var revealedGrass = new XYPair(1, 1);
        var mySideMountain = new XYPair(3, 3);

        nodes.put(mySideGrass, new FullMapNode(ETerrain.Grass, false)); // Correct
        nodes.put(enemySideGrass, new FullMapNode(ETerrain.Grass, false)); // Wrong Side
        nodes.put(revealedGrass, new FullMapNode(ETerrain.Grass, true)); // Revealed
        nodes.put(mySideMountain, new FullMapNode(ETerrain.Mountain, false)); // Wrong Terrain

        List<XYPair> result = splitter.getUnrevealedGrass(nodes, true);

        assertThat(result, contains(mySideGrass));
        assertThat(result, hasSize(1));
    }

    @Test
    void GetMountains_MixedNodes_ReturnsOnlyMatchingMountains() {
        var splitter = new FullMapSplitter(topLeftPosition);
        Map<XYPair, FullMapNode> nodes = new HashMap<>();

        var enemySideMountain = new XYPair(15, 2);
        var mySideMountain = new XYPair(1, 1);

        nodes.put(enemySideMountain, new FullMapNode(ETerrain.Mountain, true));
        nodes.put(mySideMountain, new FullMapNode(ETerrain.Mountain, true));

        List<XYPair> result = splitter.getMountains(nodes, false);

        assertThat(result, contains(enemySideMountain));
        assertThat(result, hasSize(1));
    }

    @Test
    void GetNeighbors_ListProvided_ReturnsOnlyNodesAdjacentToTargetList() {
        var splitter = new FullMapSplitter(topLeftPosition);
        var target = new XYPair(5, 0);
        var validNeighbor = new XYPair(4, 0);
        var farNode = new XYPair(10, 0);

        List<XYPair> result = splitter.getNeighbors(mapSize, List.of(validNeighbor, farNode), List.of(target));

        assertThat(result, contains(validNeighbor));
        assertThat(result, hasSize(1));
    }

    @Test
    void GetUnrevealedGrass_CoordinateOnBottomLeft_IdentifiedAsOpponentSide() {
        var splitter = new FullMapSplitter(topLeftPosition);
        Map<XYPair, FullMapNode> nodes = new HashMap<>();
        var bottomLeftCoordinate = new XYPair(5, 7);
        nodes.put(bottomLeftCoordinate, new FullMapNode(ETerrain.Grass, false));

        List<XYPair> result = splitter.getUnrevealedGrass(nodes, false);
        assertThat(result, contains(bottomLeftCoordinate));
    }

    @Test
    void GetUnrevealedGrass_NullNodes_ThrowsIllegalArgumentException() {
        var splitter = new FullMapSplitter(topLeftPosition);
        assertThrows(IllegalArgumentException.class, () -> splitter.getUnrevealedGrass(null, true));
    }

    @Test
    void GetMountains_NullNodes_ThrowsIllegalArgumentException() {
        var splitter = new FullMapSplitter(topLeftPosition);
        assertThrows(IllegalArgumentException.class, () -> splitter.getMountains(null, true));
    }

    @Test
    void GetNeighbors_NullMapSize_ThrowsIllegalArgumentException() {
        var splitter = new FullMapSplitter(topLeftPosition);
        assertThrows(IllegalArgumentException.class, () -> splitter.getNeighbors(null, List.of(), List.of()));
    }

    @Test
    void GetNeighbors_NullPotentialNeighbors_ThrowsIllegalArgumentException() {
        var splitter = new FullMapSplitter(topLeftPosition);
        assertThrows(IllegalArgumentException.class, () -> splitter.getNeighbors(mapSize, null, List.of()));
    }

    @Test
    void GetNeighbors_NullNodeList_ThrowsIllegalArgumentException() {
        var splitter = new FullMapSplitter(topLeftPosition);
        assertThrows(IllegalArgumentException.class, () -> splitter.getNeighbors(mapSize, List.of(), null));
    }
}