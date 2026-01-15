package client.halfmaplogic.validation.rule;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import client.data.ETerrain;
import client.data.XYPair;
import client.data.fromclient.HalfMap;

public class TerrainRuleTest {
    private TerrainRule terrainRule;
    private Map<XYPair, ETerrain> nodes;
    private final Set<XYPair> fortPosition = Set.of(new XYPair(0, 0));

    @BeforeEach
    public void setUp() {
        terrainRule = new TerrainRule();
        nodes = new HashMap<>();
    }

    private void fillMap(int grass, int mountains) {
        int count = 0;
        for (int y = 0; y < 5; y++)
            for (int x = 0; x < 10; x++) {
                var coordinate = new XYPair(x, y);

                ETerrain terrain = ETerrain.Water;
                if (count < grass)
                    terrain = ETerrain.Grass;
                else if (count < grass + mountains)
                    terrain = ETerrain.Mountain;

                nodes.put(coordinate, terrain);
                ++count;
            }
    }

    @Test
    public void ValidComposition_ValidateCalled_ReturnsNoErrors() {
        fillMap(24, 5);
        var halfMap = new HalfMap(nodes, fortPosition);
        var errors = terrainRule.validate(halfMap);

        assertThat(errors, empty());
    }

    @Test
    public void TooFewGrass_ValidateCalled_ReturnsTerrainError() {
        fillMap(23, 7);
        var halfMap = new HalfMap(nodes, fortPosition);
        var errors = terrainRule.validate(halfMap);

        assertThat(errors, hasSize(1));
        assertThat(errors.getFirst().getMessage().contains("Not enough grass"), is(true));
    }

    @Test
    public void TooFewMountains_ValidateCalled_ReturnsTerrainError() {
        fillMap(30, 4);
        var halfMap = new HalfMap(nodes, fortPosition);
        var errors = terrainRule.validate(halfMap);

        assertThat(errors, hasSize(1));
        assertThat(errors.getFirst().getMessage().contains("Not enough mountains"), is(true));
    }

    @Test
    public void TooFewWater_ValidateCalled_ReturnsTerrainError() {
        fillMap(40, 5);
        var halfMap = new HalfMap(nodes, fortPosition);
        var errors = terrainRule.validate(halfMap);

        assertThat(errors, hasSize(1));
        assertThat(errors.getFirst().getMessage().contains("Not enough water"), is(true));
    }

    @Test
    public void WrongNodeCount_ValidateCalled_ReturnsSizeError() {
        nodes.put(new XYPair(0, 0), ETerrain.Grass);
        var halfMap = new HalfMap(nodes, fortPosition);
        var errors = terrainRule.validate(halfMap);

        boolean sizeErrorFound = errors.stream()
                .anyMatch(e -> e.getMessage().contains("must have exactly 50 nodes"));

        assertThat(sizeErrorFound, is(true));
    }

    @Test
    public void NullHalfMap_ValidateCalled_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> terrainRule.validate(null));
    }
}