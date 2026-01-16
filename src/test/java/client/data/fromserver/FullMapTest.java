package client.data.fromserver;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.*;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import client.data.ETerrain;
import client.data.XYPair;

public class FullMapTest {
    private static Map<XYPair, FullMapNode> nodes;
    private static XYPair topLeftCoordinate;
    private static XYPair bottomRightCoordinate;
    private static XYPair myPlayerPosition;
    private static XYPair enemyPlayerPosition;
    private static XYPair myFortPosition;
    private static XYPair enemyFortPosition;
    private static XYPair myTreasurePosition;
    private static FullMap realisticFullMap;

    @BeforeAll
    public static void setUp() {
        topLeftCoordinate = new XYPair(0, 0);
        bottomRightCoordinate = new XYPair(19, 4);
        myPlayerPosition = new XYPair(2, 2);
        enemyPlayerPosition = new XYPair(17, 2);
        myFortPosition = new XYPair(1, 2);
        enemyFortPosition = new XYPair(18, 2);
        myTreasurePosition = new XYPair(3, 2);

        nodes = new HashMap<>();
        for (int y = 0; y < 5; ++y)
            for (int x = 0; x < 20; ++x) {
                XYPair current = new XYPair(x, y);
                ETerrain terrain = switch (current) {
                    case XYPair p when p.y() == 0 && (p.x() <= 1 || p.x() >= 8 && p.x() <= 11 || p.x() >= 18) ->
                            ETerrain.Water;
                    case XYPair p when p.y() == 0 -> ETerrain.Mountain;

                    case XYPair p when p.y() == 1 && (p.x() == 0 || p.x() == 19) -> ETerrain.Mountain;

                    case XYPair p when p.y() == 2 && (p.x() == 0 || p.x() == 2 || p.x() == 5 || p.x() == 8 ||
                            p.x() == 11 || p.x() == 14 || p.x() == 17 || p.x() == 19) -> ETerrain.Mountain;

                    case XYPair p when p.y() == 3 && (p.x() == 0 || p.x() == 19) -> ETerrain.Mountain;

                    case XYPair p when p.y() == 4 && (p.x() <= 1 || p.x() >= 8 && p.x() <= 11 || p.x() >= 18) ->
                            ETerrain.Water;
                    case XYPair p when p.y() == 4 -> ETerrain.Mountain;
                    default -> ETerrain.Grass;
                };
                nodes.put(current, new FullMapNode(terrain, current.equals(myPlayerPosition)));
            }

        realisticFullMap = new FullMap(nodes, topLeftCoordinate, bottomRightCoordinate, myPlayerPosition,
                enemyPlayerPosition, myFortPosition, enemyFortPosition, myTreasurePosition, false);
    }

    @Test
    public void MapWith20x5Boundaries_SizeCalled_ReturnsCorrectDimensions() {
        XYPair size = realisticFullMap.size();
        assertThat(size, is(new XYPair(20, 5)));
    }

    @Test
    public void EmptyMapStaticFactory_isEmptyCalled_ReturnsTrue() {
        FullMap empty = FullMap.emptyFullMap();
        assertThat(empty.isEmpty(), is(true));
        assertThat(empty.size(), is(new XYPair(0, 0)));
    }

    @Test
    public void ValidMap_withMyTreasurePositionCalled_ReturnsNewInstanceWithPosition() {
        var newTreasurePosition = new XYPair(5, 5);
        FullMap updatedFullMap = realisticFullMap.withMyTreasurePosition(newTreasurePosition);

        assertThat(updatedFullMap.myTreasurePosition(), is(newTreasurePosition));
        assertThat(updatedFullMap.getOptionalMyTreasurePosition(), is(Optional.of(newTreasurePosition)));
        assertThat(updatedFullMap, not(sameInstance(realisticFullMap)));
    }

    @Test
    public void ValidMap_withNodesCalled_ReturnsNewInstanceWithDefensiveCopy() {
        Map<XYPair, FullMapNode> newNodes = new HashMap<>();
        newNodes.put(new XYPair(0, 0), new FullMapNode(ETerrain.Grass, true));
        FullMap updatedFullMap = realisticFullMap.withNodes(newNodes);

        assertThat(updatedFullMap.nodes(), is(newNodes));
        assertThat(updatedFullMap.nodes(), not(sameInstance(newNodes)));
    }

    @ParameterizedTest
    @MethodSource("provideTerrainCheckScenarios")
    public void RealisticFullMap_CheckTerrainAndReveal_ReturnsCorrectBoolean(XYPair coordinate, ETerrain expectedTerrain, boolean expectedRevealed) {
        assertThat(realisticFullMap.getTerrain(coordinate), is(expectedTerrain));
        assertThat(realisticFullMap.isWater(coordinate), is(expectedTerrain == ETerrain.Water));
        assertThat(realisticFullMap.isMountain(coordinate), is(expectedTerrain == ETerrain.Mountain));
        assertThat(realisticFullMap.isGrass(coordinate), is(expectedTerrain == ETerrain.Grass));
        assertThat(realisticFullMap.isRevealed(coordinate), is(expectedRevealed));
    }

    private static Stream<Arguments> provideTerrainCheckScenarios() {
        return Stream.of(
                Arguments.of(new XYPair(0, 0), ETerrain.Water, false),
                Arguments.of(new XYPair(2, 2), ETerrain.Mountain, true),
                Arguments.of(new XYPair(2, 2), ETerrain.Mountain, true),
                Arguments.of(new XYPair(5, 1), ETerrain.Grass, false)
        );
    }

    @ParameterizedTest
    @MethodSource("provideOptionalGetterScenarios")
    public void ValidFullMap_OptionalGetters_ReturnCorrectOptional(Optional<XYPair> actual, Optional<XYPair> expected) {
        assertThat(actual, is(expected));
    }

    private static Stream<Arguments> provideOptionalGetterScenarios() {
        return Stream.of(
                Arguments.of(realisticFullMap.getOptionalMyPlayerPosition(), Optional.of(myPlayerPosition)),
                Arguments.of(realisticFullMap.getOptionalEnemyPlayerPosition(), Optional.of(enemyPlayerPosition)),
                Arguments.of(realisticFullMap.getOptionalMyFortPosition(), Optional.of(myFortPosition)),
                Arguments.of(realisticFullMap.getOptionalEnemyFortPosition(), Optional.of(enemyFortPosition)),
                Arguments.of(realisticFullMap.getOptionalMyTreasurePosition(), Optional.of(myTreasurePosition)),
                Arguments.of(realisticFullMap.getOptionalTopLeftCoordinate(), Optional.of(topLeftCoordinate)),
                Arguments.of(realisticFullMap.getOptionalBottomRightCoordinate(), Optional.of(bottomRightCoordinate))
        );
    }

    @Test
    public void NullNodes_ConstructorCalled_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new FullMap(null, topLeftCoordinate, bottomRightCoordinate, null, null, null, null, null, false));
    }

    @Test
    public void MissingBottomRight_SizeCalled_ThrowsIllegalStateException() {
        var incompleteFullMap = new FullMap(nodes, topLeftCoordinate, null, null, null, null, null, null, false);
        assertThrows(IllegalStateException.class, incompleteFullMap::size);
    }

    @Test
    public void MissingTopLeft_SizeCalled_ThrowsIllegalStateException() {
        var incompleteFullMap = new FullMap(nodes, null, bottomRightCoordinate, null, null, null, null, null, false);
        assertThrows(IllegalStateException.class, incompleteFullMap::size);
    }

    @Test
    public void ValidFullMap_withNodesNull_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> realisticFullMap.withNodes(null));
    }

    @Test
    public void ValidFullMap_isRevealedWithNull_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> realisticFullMap.isRevealed(null));
    }

    @Test
    public void ValidFullMap_isWaterWithNull_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> realisticFullMap.isWater(null));
    }

    @Test
    public void ValidFullMap_isMountainWithNull_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> realisticFullMap.isMountain(null));
    }

    @Test
    public void ValidFullMap_isGrassWithNull_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> realisticFullMap.isGrass(null));
    }

    @Test
    public void ValidFullMap_getTerrainWithNull_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> realisticFullMap.getTerrain(null));
    }
}