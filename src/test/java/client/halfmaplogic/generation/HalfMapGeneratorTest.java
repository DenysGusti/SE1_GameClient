package client.halfmaplogic.generation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import java.util.random.RandomGenerator;

import org.junit.jupiter.api.Test;

import client.data.ETerrain;
import client.data.XYPair;
import client.data.fromclient.HalfMap;

public class HalfMapGeneratorTest {
    @Test
    void Constructor_NullRandomGenerator_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new HalfMapGenerator(null));
    }

    @Test
    void GenerateHalfMap_ValidRandomness_ReturnsValidHalfMap() {
        RandomGenerator seededRandom = new java.util.Random(42);

        var generator = new HalfMapGenerator(seededRandom);
        HalfMap result = generator.generateHalfMap();

        assertThat(result, notNullValue());
        assertThat(result.nodes().size(), is(50));
        assertThat(result.potentialForts().size(), is(1));

        long grassCount = countTerrain(result.nodes(), ETerrain.Grass);
        long mountainCount = countTerrain(result.nodes(), ETerrain.Mountain);
        long waterCount = countTerrain(result.nodes(), ETerrain.Water);

        assertThat(grassCount, greaterThanOrEqualTo(24L));
        assertThat(waterCount, greaterThanOrEqualTo(7L));
        assertThat(mountainCount, greaterThanOrEqualTo(5L));
    }

    private static long countTerrain(Map<XYPair, ETerrain> nodes, ETerrain terrain) {
        return nodes.values().stream().filter(t -> t == terrain).count();
    }
}