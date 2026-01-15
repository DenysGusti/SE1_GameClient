package client.data.fromserver;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import client.data.ETerrain;

public class FullMapNodeTest {
    @ParameterizedTest
    @MethodSource("provideTerrainScenarios")
    public void NodeWithSpecificTerrain_CheckTerrainMethods_ReturnsCorrectBooleans(ETerrain terrain,
                                                                                   boolean isGrass, boolean isMountain, boolean isWater) {
        var node = new FullMapNode(terrain, false);

        assertThat(node.isGrass(), is(isGrass));
        assertThat(node.isMountain(), is(isMountain));
        assertThat(node.isWater(), is(isWater));
        assertThat(node.terrain(), is(terrain));
    }

    private static Stream<Arguments> provideTerrainScenarios() {
        return Stream.of(
                Arguments.of(ETerrain.Grass, true, false, false),
                Arguments.of(ETerrain.Mountain, false, true, false),
                Arguments.of(ETerrain.Water, false, false, true)
        );
    }

    @Test
    public void NodeWithRevealedStatus_withIsRevealedCalled_ReturnsNewNodeWithUpdatedStatus() {
        var initialNode = new FullMapNode(ETerrain.Grass, false);
        FullMapNode updatedNode = initialNode.withIsRevealed(true);

        assertThat(updatedNode.isRevealed(), is(true));
        assertThat(updatedNode.terrain(), is(initialNode.terrain()));
        assertThat(updatedNode, is(not(sameInstance(initialNode))));
    }

    @Test
    public void NullTerrain_ConstructorCalled_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new FullMapNode(null, false));
    }
}