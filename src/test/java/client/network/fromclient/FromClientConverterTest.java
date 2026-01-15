package client.network.fromclient;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import client.data.ETerrain;
import client.data.PlayerInformation;
import client.data.UniquePlayerIdentifier;
import client.data.XYPair;
import client.data.fromclient.EMove;
import client.data.fromclient.HalfMap;

public class FromClientConverterTest {
    private FromClientConverter converter;
    private UniquePlayerIdentifier playerId;

    @BeforeEach
    public void setUp() {
        converter = new FromClientConverter();
        playerId = new UniquePlayerIdentifier("test-id");
    }

    @Test
    public void ValidPlayerInfo_ConvertPlayerInformation_ReturnsCorrectRegistration() {
        var playerInformation = new PlayerInformation("John", "Doe", "doe123");
        var result = converter.convertPlayerInformation(playerInformation);

        assertThat(result.getStudentFirstName(), is("John"));
        assertThat(result.getStudentLastName(), is("Doe"));
        assertThat(result.getStudentUAccount(), is("doe123"));
    }

    @ParameterizedTest
    @EnumSource(EMove.class)
    public void ValidMove_ConvertMove_ReturnsCorrectNetworkMove(EMove move) {
        var result = converter.convertMove(playerId, move);

        assertThat(result.getUniquePlayerID(), is("test-id"));
        assertThat(result.getMove().name(), is(move.name()));
    }

    @Test
    public void ValidHalfMapWithFort_ConvertHalfMap_NodesTranslatedCorrectly() {
        var fortPosition = new XYPair(1, 1);
        var grassPosition = new XYPair(0, 0);
        var nodes = Map.of(
                grassPosition, ETerrain.Grass,
                fortPosition, ETerrain.Grass
        );
        var halfMap = new HalfMap(nodes, Set.of(fortPosition));

        var result = converter.convertHalfMap(playerId, halfMap);

        assertThat(result.getUniquePlayerID(), is("test-id"));
        assertThat(result.getMapNodes(), hasSize(2));

        var fortNode = result.getMapNodes().stream()
                .filter(n -> n.getX() == 1 && n.getY() == 1)
                .findFirst().orElseThrow();

        assertThat(fortNode.isFortPresent(), is(true));
        assertThat(fortNode.getTerrain(), is(messagesbase.messagesfromclient.ETerrain.Grass));
    }

    @ParameterizedTest
    @EnumSource(ETerrain.class)
    public void VariousTerrain_ConvertHalfMap_TerrainMappedCorrectly(ETerrain terrain) {
        var position = new XYPair(0, 0);
        var halfMap = new HalfMap(Map.of(position, terrain), Set.of());

        var result = converter.convertHalfMap(playerId, halfMap);
        var node = result.getMapNodes().iterator().next();

        assertThat(node.getTerrain().name(), is(terrain.name()));
    }

    @Test
    public void NullPlayerInfo_ConvertPlayerInformation_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> converter.convertPlayerInformation(null));
    }

    @Test
    public void NullPlayerId_ConvertHalfMap_ThrowsIllegalArgumentException() {
        var halfMap = new HalfMap(Map.of(), Set.of());
        assertThrows(IllegalArgumentException.class, () -> converter.convertHalfMap(null, halfMap));
    }

    @Test
    public void NullHalfMap_ConvertHalfMap_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> converter.convertHalfMap(playerId, null));
    }

    @Test
    public void NullPlayerId_ConvertMove_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> converter.convertMove(null, EMove.Up));
    }

    @Test
    public void NullMove_ConvertMove_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> converter.convertMove(playerId, null));
    }
}