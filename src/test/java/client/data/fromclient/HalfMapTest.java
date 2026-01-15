package client.data.fromclient;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import client.data.ETerrain;
import client.data.XYPair;

public class HalfMapTest {
    private static Map<XYPair, ETerrain> nodes;
    private static Set<XYPair> potentialForts;
    private static HalfMap realisticHalfMap;

    @BeforeAll
    public static void setUp() {
        nodes = new HashMap<>();
        for (int y = 0; y < 5; ++y)
            for (int x = 0; x < 10; ++x) {
                XYPair current = new XYPair(x, y);
                ETerrain terrain = switch (current) {
                    case XYPair p when p.y() == 0 && (p.x() <= 1 || p.x() >= 8) -> ETerrain.Water;
                    case XYPair p when p.y() == 0 -> ETerrain.Mountain;

                    case XYPair p when p.y() == 1 && p.x() == 8 -> ETerrain.Water;
                    case XYPair p when p.y() == 1 && p.x() == 9 -> ETerrain.Mountain;

                    case XYPair p when p.y() == 2 && (p.x() == 1 || p.x() == 4 || p.x() == 6 || p.x() == 9) ->
                            ETerrain.Mountain;

                    case XYPair p when p.y() == 3 && p.x() == 9 -> ETerrain.Mountain;

                    case XYPair p when p.y() == 4 && (p.x() <= 1 || p.x() == 9) -> ETerrain.Water;
                    case XYPair p when p.y() == 4 && p.x() <= 7 -> ETerrain.Mountain;

                    default -> ETerrain.Grass;
                };
                nodes.put(current, terrain);
            }
        potentialForts = new HashSet<>(Set.of(new XYPair(0, 2)));
        realisticHalfMap = new HalfMap(nodes, potentialForts);
    }

    @Test
    public void ValidArguments_ConstructorCalled_CollectionsAreDefensivelyCopied() {
        assertThat(realisticHalfMap.nodes(), is(not(sameInstance(nodes))));
        assertThat(realisticHalfMap.potentialForts(), is(not(sameInstance(potentialForts))));
        assertThat(realisticHalfMap.nodes(), is(nodes));
    }

    @ParameterizedTest
    @MethodSource("provideTerrainCheckScenarios")
    public void RealisticHalfMap_CheckTerrainType_ReturnsCorrectBoolean(XYPair coordinate, ETerrain expectedTerrain) {
        assertThat(realisticHalfMap.isWater(coordinate), is(expectedTerrain == ETerrain.Water));
        assertThat(realisticHalfMap.isMountain(coordinate), is(expectedTerrain == ETerrain.Mountain));
        assertThat(realisticHalfMap.isGrass(coordinate), is(expectedTerrain == ETerrain.Grass));
        assertThat(realisticHalfMap.getTerrain(coordinate), is(expectedTerrain));
    }

    private static Stream<Arguments> provideTerrainCheckScenarios() {
        return Stream.of(
                Arguments.of(new XYPair(8, 1), ETerrain.Water),
                Arguments.of(new XYPair(1, 2), ETerrain.Mountain),
                Arguments.of(new XYPair(8, 4), ETerrain.Grass)
        );
    }

    @Test
    public void NullNodes_ConstructorCalled_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new HalfMap(null, potentialForts));
    }

    @Test
    public void NullForts_ConstructorCalled_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new HalfMap(nodes, null));
    }

    @Test
    public void ValidHalfMap_isWaterWithNull_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> realisticHalfMap.isWater(null));
    }

    @Test
    public void ValidHalfMap_isMountainWithNull_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> realisticHalfMap.isMountain(null));
    }

    @Test
    public void ValidHalfMap_isGrassWithNull_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> realisticHalfMap.isGrass(null));
    }

    @Test
    public void ValidHalfMap_getTerrainWithNull_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> realisticHalfMap.getTerrain(null));
    }
}