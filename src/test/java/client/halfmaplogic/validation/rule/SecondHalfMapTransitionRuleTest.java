package client.halfmaplogic.validation.rule;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import client.data.ETerrain;
import client.data.XYPair;
import client.data.fromclient.HalfMap;
import client.data.fromserver.FullMap;
import client.data.fromserver.FullMapNode;
import client.halfmaplogic.validation.exception.HalfMapGenerationException;

public class SecondHalfMapTransitionRuleTest {
    private FullMap fullMapMock;
    private Map<XYPair, FullMapNode> fullMapNodes;
    private Map<XYPair, ETerrain> halfMapNodes;
    private static final XYPair topLeft = new XYPair(0, 0);
    private static final Set<XYPair> fortPositions = Set.of(new XYPair(2, 2));

    @BeforeEach
    public void setUp() {
        fullMapMock = mock(FullMap.class);
        fullMapNodes = new HashMap<>();
        halfMapNodes = new HashMap<>();

        for (int y = 0; y < 5; ++y)
            for (int x = 0; x < 10; ++x) {
                var coordinate = new XYPair(x, y);
                var fullMapNode = mock(FullMapNode.class);
                fullMapNodes.put(coordinate, fullMapNode);
                halfMapNodes.put(coordinate, ETerrain.Grass);
            }

        when(fullMapMock.nodes()).thenReturn(fullMapNodes);
        when(fullMapMock.getOptionalTopLeftCoordinate()).thenReturn(Optional.of(topLeft));
        when(fullMapMock.getTerrain(any(XYPair.class))).thenReturn(ETerrain.Grass);
    }

    @Test
    public void ValidTransition_ValidateCalled_ReturnsNoErrors() {
        var rule = new SecondHalfMapTransitionRule(fullMapMock);
        var halfMap = new HalfMap(halfMapNodes, fortPositions);

        var errors = rule.validate(halfMap);
        assertThat(errors, empty());
    }

    @Test
    public void OwnTopBorderWater_ValidateCalled_ReturnsTopViolation() {
        for (int x = 0; x < 10; ++x)
            halfMapNodes.put(new XYPair(x, 0), ETerrain.Water);

        var rule = new SecondHalfMapTransitionRule(fullMapMock);
        var halfMap = new HalfMap(halfMapNodes, fortPositions);

        var errors = rule.validate(halfMap);
        assertThat(errors.stream().anyMatch(e -> e.getMessage().contains("Top border")), is(true));
    }

    @Test
    public void OwnBottomBorderWater_ValidateCalled_ReturnsBottomViolation() {
        for (int x = 0; x < 10; ++x)
            halfMapNodes.put(new XYPair(x, 4), ETerrain.Water);

        var rule = new SecondHalfMapTransitionRule(fullMapMock);
        var halfMap = new HalfMap(halfMapNodes, fortPositions);

        var errors = rule.validate(halfMap);
        assertThat(errors.stream().anyMatch(e -> e.getMessage().contains("Bottom border")), is(true));
    }

    @Test
    public void OwnLeftBorderWater_ValidateCalled_ReturnsLeftViolation() {
        for (int y = 0; y < 5; ++y)
            halfMapNodes.put(new XYPair(0, y), ETerrain.Water);

        var rule = new SecondHalfMapTransitionRule(fullMapMock);
        var halfMap = new HalfMap(halfMapNodes, fortPositions);

        var errors = rule.validate(halfMap);
        assertThat(errors.stream().anyMatch(e -> e.getMessage().contains("Left border")), is(true));
    }

    @Test
    public void OwnRightBorderWater_ValidateCalled_ReturnsRightViolation() {
        for (int y = 0; y < 5; ++y)
            halfMapNodes.put(new XYPair(9, y), ETerrain.Water);

        var rule = new SecondHalfMapTransitionRule(fullMapMock);
        var halfMap = new HalfMap(halfMapNodes, fortPositions);

        var errors = rule.validate(halfMap);
        assertThat(errors.stream().anyMatch(e -> e.getMessage().contains("Right border")), is(true));
    }

    @Test
    public void OpponentWater_ValidateCalled_ReturnsViolation() {
        for (int x = 0; x < 10; ++x)
            when(fullMapMock.getTerrain(new XYPair(x, 4))).thenReturn(ETerrain.Water);

        var rule = new SecondHalfMapTransitionRule(fullMapMock);
        var halfMap = new HalfMap(halfMapNodes, fortPositions);

        var errors = rule.validate(halfMap);
        assertThat(errors.stream().anyMatch(e -> e.getMessage().contains("Top border")), is(true));
    }

    @Test
    public void NullFullMap_ConstructorCalled_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new SecondHalfMapTransitionRule(null));
    }

    @Test
    public void InvalidFullMapSize_ConstructorCalled_ThrowsHalfMapGenerationException() {
        fullMapNodes.remove(new XYPair(0, 0));
        assertThrows(HalfMapGenerationException.class, () -> new SecondHalfMapTransitionRule(fullMapMock));
    }

    @Test
    public void NullHalfMap_ValidateCalled_ThrowsIllegalArgumentException() {
        var rule = new SecondHalfMapTransitionRule(fullMapMock);
        assertThrows(IllegalArgumentException.class, () -> rule.validate(null));
    }
}