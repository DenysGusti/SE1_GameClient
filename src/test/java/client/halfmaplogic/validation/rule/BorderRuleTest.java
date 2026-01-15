package client.halfmaplogic.validation.rule;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import client.modelviewcontroller.view.HalfMapView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import client.data.ETerrain;
import client.data.XYPair;
import client.data.fromclient.HalfMap;

public class BorderRuleTest {
    private BorderRule borderRule;
    private Map<XYPair, ETerrain> nodes;

    @BeforeEach
    public void setUp() {
        borderRule = new BorderRule();
        nodes = new HashMap<>();

        for (int y = 0; y < 5; ++y)
            for (int x = 0; x < 10; ++x)
                nodes.put(new XYPair(x, y), ETerrain.Grass);
    }

    @Test
    public void ValidBorders_ValidateCalled_ReturnsNoErrors() {
        nodes.put(new XYPair(0, 0), ETerrain.Water);
        nodes.put(new XYPair(1, 0), ETerrain.Water);
        nodes.put(new XYPair(0, 1), ETerrain.Water);
        nodes.put(new XYPair(9, 0), ETerrain.Water);
        nodes.put(new XYPair(9, 1), ETerrain.Water);
        nodes.put(new XYPair(0, 4), ETerrain.Water);
        nodes.put(new XYPair(1, 4), ETerrain.Water);

        var halfMap = new HalfMap(nodes, Set.of(new XYPair(2, 2)));
        var errors = borderRule.validate(halfMap);
        assertThat(errors, empty());
    }

    @Test
    public void AllGrassMap_ValidateCalled_ReturnsErrorsForAllBorders() {
        var halfMap = new HalfMap(nodes, Set.of(new XYPair(2, 2)));
        var errors = borderRule.validate(halfMap);

        assertThat(errors, hasSize(4));
    }

    @Test
    public void AllWaterMap_ValidateCalled_ReturnsErrorsForAllBorders() {
        nodes.replaceAll((k, v) -> ETerrain.Water);
        var halfMap = new HalfMap(nodes, Set.of(new XYPair(2, 2)));
        var errors = borderRule.validate(halfMap);

        assertThat(errors, hasSize(4));
    }

    @Test
    public void InvalidTopBorderOnly_ValidateCalled_ReturnsSpecificError() {
        nodes.put(new XYPair(0, 4), ETerrain.Water);
        nodes.put(new XYPair(1, 4), ETerrain.Water);
        nodes.put(new XYPair(0, 1), ETerrain.Water);
        nodes.put(new XYPair(9, 1), ETerrain.Water);

        var halfMap = new HalfMap(nodes, Set.of(new XYPair(2, 2)));
        var errors = borderRule.validate(halfMap);

        assertThat(errors, hasSize(1));
        assertThat(errors.getFirst().getMessage(), is("Top border (y=0) violation."));
    }

    @Test
    public void NullHalfMap_ValidateCalled_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> borderRule.validate(null));
    }
}