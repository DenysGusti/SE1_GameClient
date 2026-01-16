package client.main;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.awt.*;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.NoSuchElementException;

import client.ai.graph.FullMapGraph;
import client.data.ETerrain;
import client.data.XYPair;
import client.data.fromserver.FullMap;
import client.data.fromserver.FullMapNode;
import client.halfmaplogic.generation.HalfMapGenerator;
import client.halfmaplogic.validation.HalfMapValidator;
import client.modelviewcontroller.view.FullMapViewJavaFX;
import client.network.GameSession;
import client.network.NetworkService;
import client.network.accumulator.FullMapAccumulator;
import client.data.UniqueGameIdentifier;
import client.ai.graph.FullMapGraphFactory;

import javafx.collections.FXCollections;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.shape.TriangleMesh;
import org.mockito.ArgumentMatchers;

public class GameClientFactoryTest {
    private GameClientFactory factory;
    private FullMapGraphFactory graphFactoryMock;

    @BeforeEach
    void setUp() {
        graphFactoryMock = mock(FullMapGraphFactory.class);
        factory = new GameClientFactory(graphFactoryMock);
    }

    @Test
    void Constructor_NullFullMapGraphFactory_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new GameClientFactory(null));
    }

    @Test
    void CreateNetworkService_ValidInputs_ReturnsInstance() {
        var gameId = new UniqueGameIdentifier("game1");
        assertThat(factory.createNetworkService("http://test", gameId), notNullValue());
    }

    @Test
    void CreateNetworkService_NullURL_ThrowsIllegalArgumentException() {
        var gameId = new UniqueGameIdentifier("game1");
        assertThrows(IllegalArgumentException.class, () -> factory.createNetworkService(null, gameId));
    }

    @Test
    void CreateNetworkService_NullGameId_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> factory.createNetworkService("http://test", null));
    }

    @Test
    void CreateAIPlayer_ValidMap_ReturnsAIPlayerInstance() {
        var mapMock = mock(FullMap.class);
        var positionMock = mock(XYPair.class);
        var graphMock = mock(FullMapGraph.class);

        when(mapMock.getOptionalMyFortPosition()).thenReturn(Optional.of(positionMock));
        when(graphFactoryMock.createGraph(mapMock)).thenReturn(graphMock);

        var aiPlayer = factory.createAIPlayer(mapMock);

        assertThat(aiPlayer, notNullValue());
        verify(graphFactoryMock).createGraph(mapMock);
    }

    @Test
    void CreateAIPlayer_MapMissingFort_ThrowsNoSuchElementException() {
        var mapMock = mock(FullMap.class);
        when(mapMock.getOptionalMyFortPosition()).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> factory.createAIPlayer(mapMock));
    }

    @Test
    void CreateAIPlayer_NullMap_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> factory.createAIPlayer(null));
    }

    @Test
    void CreateFullMapViewJavaFX_ValidInputs_ThrowsIllegalStateExceptionDueToToolkit() {
        var groupMock = mock(Group.class);
        when(groupMock.getChildren()).thenReturn(FXCollections.observableArrayList());

        var camera = mock(Camera.class);
        Map<String, Image> textures = Collections.emptyMap();
        var water = new Image[0];
        Map<String, TriangleMesh> meshes = Collections.emptyMap();

        assertThrows(IllegalStateException.class, () ->
                factory.createFullMapViewJavaFX(groupMock, camera, textures, water, meshes));
    }

    @Test
    void CreateFullMapViewJavaFX_NullWorldRoot_ThrowsIllegalArgumentException() {
        var camera = mock(Camera.class);
        assertThrows(IllegalArgumentException.class, () ->
                factory.createFullMapViewJavaFX(null, camera, Collections.emptyMap(), new Image[0], Collections.emptyMap()));
    }

    @Test
    void CreateFullMapViewJavaFX_NullCamera_ThrowsIllegalArgumentException() {
        var group = mock(Group.class);
        assertThrows(IllegalArgumentException.class, () ->
                factory.createFullMapViewJavaFX(group, null, Collections.emptyMap(), new Image[0], Collections.emptyMap()));
    }

    @Test
    void CreateFullMapViewJavaFX_NullTextures_ThrowsIllegalArgumentException() {
        var group = mock(Group.class);
        var camera = mock(Camera.class);
        assertThrows(IllegalArgumentException.class, () ->
                factory.createFullMapViewJavaFX(group, camera, null, new Image[0], Collections.emptyMap()));
    }

    @Test
    void CreateFullMapViewJavaFX_NullWaterTextures_ThrowsIllegalArgumentException() {
        var group = mock(Group.class);
        var camera = mock(Camera.class);
        assertThrows(IllegalArgumentException.class, () ->
                factory.createFullMapViewJavaFX(group, camera, Collections.emptyMap(), null, Collections.emptyMap()));
    }

    @Test
    void CreateFullMapViewJavaFX_NullMeshes_ThrowsIllegalArgumentException() {
        var group = mock(Group.class);
        var camera = mock(Camera.class);
        assertThrows(IllegalArgumentException.class, () ->
                factory.createFullMapViewJavaFX(group, camera, Collections.emptyMap(), new Image[0], null));
    }

    @Test
    void CreateGameController_ValidInputs_ReturnsInstance() {
        var session = mock(GameSession.class);
        var gen = mock(HalfMapGenerator.class);
        var val = mock(HalfMapValidator.class);
        var view = mock(FullMapViewJavaFX.class);

        assertThat(factory.createGameController(session, gen, val, view), notNullValue());
        assertThat(factory.createGameController(session, gen, val, null), notNullValue());
    }

    @Test
    void CreateGameController_NullSession_ThrowsIllegalArgumentException() {
        var gen = mock(HalfMapGenerator.class);
        var val = mock(HalfMapValidator.class);
        assertThrows(IllegalArgumentException.class, () ->
                factory.createGameController(null, gen, val, null));
    }

    @Test
    void CreateGameController_NullGenerator_ThrowsIllegalArgumentException() {
        var session = mock(GameSession.class);
        var val = mock(HalfMapValidator.class);
        assertThrows(IllegalArgumentException.class, () ->
                factory.createGameController(session, null, val, null));
    }

    @Test
    void CreateGameController_NullValidator_ThrowsIllegalArgumentException() {
        var session = mock(GameSession.class);
        var gen = mock(HalfMapGenerator.class);
        assertThrows(IllegalArgumentException.class, () ->
                factory.createGameController(session, gen, null, null));
    }

    @Test
    void CreateSecondHalfMapTransitionRule_ValidInput_ReturnsInstance() {
        var mapMock = mock(FullMap.class);
        var topLeft = new XYPair(0, 0);

        Map<XYPair, FullMapNode> fakeNodes = mock(Map.class);
        when(fakeNodes.size()).thenReturn(50);
        when(mapMock.nodes()).thenReturn(fakeNodes);

        when(mapMock.getOptionalTopLeftCoordinate()).thenReturn(Optional.of(topLeft));
        when(mapMock.getTerrain(ArgumentMatchers.any(XYPair.class))).thenReturn(ETerrain.Grass);

        assertThat(factory.createSecondHalfMapTransitionRule(mapMock), notNullValue());
    }

    @Test
    void CreateSecondHalfMapTransitionRule_NullMap_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> factory.createSecondHalfMapTransitionRule(null));
    }

    @Test
    void CreateFullMapAccumulator_ReturnsNonNullInstance() {
        assertThat(factory.createFullMapAccumulator(), instanceOf(FullMapAccumulator.class));
    }

    @Test
    void CreateGameSession_ValidInputs_ReturnsInstance() {
        var net = mock(NetworkService.class);
        var acc = mock(FullMapAccumulator.class);
        assertThat(factory.createGameSession(net, acc), instanceOf(GameSession.class));
    }

    @Test
    void CreateGameSession_NullNetworkService_ThrowsIllegalArgumentException() {
        var acc = mock(FullMapAccumulator.class);
        assertThrows(IllegalArgumentException.class, () -> factory.createGameSession(null, acc));
    }

    @Test
    void CreateGameSession_NullFullMapAccumulator_ThrowsIllegalArgumentException() {
        var net = mock(NetworkService.class);
        assertThrows(IllegalArgumentException.class, () -> factory.createGameSession(net, null));
    }

    @Test
    void CreateHalfMapGenerator_ReturnsNonNullInstance() {
        assertThat(factory.createHalfMapGenerator(), instanceOf(HalfMapGenerator.class));
    }

    @Test
    void CreateHalfMapValidator_ReturnsNonNullInstance() {
        assertThat(factory.createHalfMapValidator(), instanceOf(HalfMapValidator.class));
    }
}