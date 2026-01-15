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

public class ConnectivityRuleTest {
    private ConnectivityRule connectivityRule;
    private Map<XYPair, ETerrain> nodes;
    private final Set<XYPair> fortPositions = Set.of(new XYPair(0, 0));

    @BeforeEach
    public void setUp() {
        connectivityRule = new ConnectivityRule();
        nodes = new HashMap<>();

        for (int y = 0; y < 5; ++y)
            for (int x = 0; x < 10; ++x)
                nodes.put(new XYPair(x, y), ETerrain.Grass);
    }

    @Test
    public void FullyConnectedMap_ValidateCalled_ReturnsNoErrors() {
        var halfMap = new HalfMap(nodes, fortPositions);
        var errors = connectivityRule.validate(halfMap);

        assertThat(errors, empty());
    }

    @Test
    public void MapWithIsland_ValidateCalled_ReturnsConnectivityError() {
        for (int x = 0; x < 10; x++)
            nodes.put(new XYPair(x, 2), ETerrain.Water);

        var halfMap = new HalfMap(nodes, fortPositions);
        var errors = connectivityRule.validate(halfMap);

        assertThat(errors, hasSize(1));
        assertThat(errors.getFirst().getMessage().contains("Map has islands"), is(true));
    }

    @Test
    public void AllWaterMap_ValidateCalled_ReturnsNoTraversableNodesError() {
        nodes.replaceAll((k, v) -> ETerrain.Water);
        var halfMap = new HalfMap(nodes, fortPositions);
        var errors = connectivityRule.validate(halfMap);

        assertThat(errors, hasSize(1));
        assertThat(errors.getFirst().getMessage(), is("Half map has no traversable nodes at all."));
    }

    @Test
    public void ComplexPath_ValidateCalled_ReturnsNoErrors() {
        nodes.replaceAll((k, v) -> ETerrain.Water);

        nodes.put(new XYPair(0, 0), ETerrain.Grass);
        nodes.put(new XYPair(0, 1), ETerrain.Grass);
        nodes.put(new XYPair(1, 1), ETerrain.Grass);
        nodes.put(new XYPair(1, 0), ETerrain.Grass);

        var halfMap = new HalfMap(nodes, fortPositions);
        var errors = connectivityRule.validate(halfMap);

        assertThat(errors, empty());
    }

    @Test
    public void NullHalfMap_ValidateCalled_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> connectivityRule.validate(null));
    }
}