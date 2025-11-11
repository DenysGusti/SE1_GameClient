package client.network.fromclient;

import client.data.ETerrain;
import client.data.PlayerInformation;
import client.data.XYPair;
import client.data.fromclient.EMove;
import client.data.fromclient.HalfMap;
import messagesbase.messagesfromclient.PlayerHalfMap;
import messagesbase.messagesfromclient.PlayerMove;
import messagesbase.messagesfromclient.PlayerRegistration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;

class FromClientConverterTest {

    private FromClientConverter converter;
    private final String testPlayerID = "test-player-id";

    @BeforeEach
    void setUp() {
        converter = new FromClientConverter();
    }

    @Test
    @DisplayName("Converts client PlayerInformation to server PlayerRegistration")
    void PlayerInfo_ConvertPlayerInformation_ReturnsCorrectPlayerRegistration() {
        var info = new PlayerInformation("Test", "User", "testuser");
        PlayerRegistration registration = converter.convertPlayerInformation(info);

        assertAll(
                () -> assertThat(registration.getStudentFirstName(), is("Test")),
                () -> assertThat(registration.getStudentLastName(), is("User")),
                () -> assertThat(registration.getStudentUAccount(), is("testuser"))
        );
    }

    @Test
    @DisplayName("Converts client HalfMap to server PlayerHalfMap")
    void ClientHalfMap_ConvertHalfMap_ReturnsCorrectPlayerHalfMap() {
        var fortCoordinate = new XYPair(1, 1);
        var grassCoordinate = new XYPair(0, 0);

        Map<XYPair, ETerrain> nodes = Map.of(grassCoordinate, ETerrain.Grass, fortCoordinate, ETerrain.Grass);
        Set<XYPair> potentialForts = Set.of(fortCoordinate);
        HalfMap halfMap = new HalfMap(nodes, potentialForts);

        PlayerHalfMap result = converter.convertHalfMap(testPlayerID, halfMap);

        assertAll(
                () -> assertThat(result.getUniquePlayerID(), is(testPlayerID)),
                () -> assertThat(result.getMapNodes(), hasSize(2)),
                () -> assertThat(result.getMapNodes(), hasItem(
                        allOf(
                                hasProperty("x", is(fortCoordinate.x())),
                                hasProperty("y", is(fortCoordinate.y())),
                                hasProperty("fortPresent", is(true))
                        )
                ))
        );
    }

    @Test
    @DisplayName("Converts client EMove to server EMove")
    void ClientMove_ConvertMove_ReturnsCorrectPlayerMove() {
        PlayerMove move = converter.convertMove(testPlayerID, EMove.Up);

        assertAll(
                () -> assertThat(move.getUniquePlayerID(), is(testPlayerID)),
                () -> assertThat(move.getMove(), is(messagesbase.messagesfromclient.EMove.Up))
        );
    }
}