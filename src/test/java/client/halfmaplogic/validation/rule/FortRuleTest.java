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

public class FortRuleTest {
    private FortRule fortRule;
    private Map<XYPair, ETerrain> nodes;

    @BeforeEach
    public void setUp() {
        fortRule = new FortRule();
        nodes = new HashMap<>();

        for (int y = 0; y < 5; ++y)
            for (int x = 0; x < 10; ++x)
                nodes.put(new XYPair(x, y), ETerrain.Grass);
    }

    @Test
    public void ValidFort_ValidateCalled_ReturnsNoErrors() {
        var halfMap = new HalfMap(nodes, Set.of(new XYPair(0, 0)));
        var errors = fortRule.validate(halfMap);

        assertThat(errors, empty());
    }

    @Test
    public void NoFort_ValidateCalled_ReturnsQuantityError() {
        var halfMap = new HalfMap(nodes, Set.of());
        var errors = fortRule.validate(halfMap);

        assertThat(errors, hasSize(1));
        assertThat(errors.getFirst().getMessage().contains("Wrong number of forts"), is(true));
    }

    @Test
    public void MultipleForts_ValidateCalled_ReturnsQuantityError() {
        var halfMap = new HalfMap(nodes, Set.of(new XYPair(0, 0), new XYPair(1, 1)));
        var errors = fortRule.validate(halfMap);

        assertThat(errors, hasSize(1));
        assertThat(errors.getFirst().getMessage().contains("Wrong number of forts"), is(true));
    }

    @Test
    public void FortOnMountain_ValidateCalled_ReturnsTerrainError() {
        var fortPosition = new XYPair(5, 2);
        nodes.put(fortPosition, ETerrain.Mountain);

        var halfMap = new HalfMap(nodes, Set.of(fortPosition));
        var errors = fortRule.validate(halfMap);

        assertThat(errors, hasSize(1));
        assertThat(errors.getFirst().getMessage().contains("non-Grass tile"), is(true));
    }

    @Test
    public void FortOnWater_ValidateCalled_ReturnsTerrainError() {
        var fortPosition = new XYPair(2, 2);
        nodes.put(fortPosition, ETerrain.Water);

        var halfMap = new HalfMap(nodes, Set.of(fortPosition));
        var errors = fortRule.validate(halfMap);

        assertThat(errors, hasSize(1));
        assertThat(errors.getFirst().getMessage().contains("non-Grass tile"), is(true));
    }

    @Test
    public void NullHalfMap_ValidateCalled_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> fortRule.validate(null));
    }
}