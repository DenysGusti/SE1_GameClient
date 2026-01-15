package client.main;

import client.ai.AIPlayer;
import client.ai.KnowledgeBase;
import client.ai.graph.FullMapGraph;
import client.ai.path.ExpectedArrivalPathFactory;
import client.ai.utilities.FullMapSplitter;
import client.ai.graph.FullMapGraphFactory;
import client.data.fromserver.FullMap;
import client.halfmaplogic.generation.HalfMapGenerator;
import client.halfmaplogic.validation.HalfMapValidator;
import client.halfmaplogic.validation.rule.*;
import client.javafx.*;
import client.modelviewcontroller.controller.GameController;
import client.modelviewcontroller.model.*;
import client.modelviewcontroller.view.*;
import client.network.*;
import client.network.accumulator.*;
import client.network.fromserver.*;
import client.network.fromclient.FromClientConverter;
import client.data.UniqueGameIdentifier;

import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.shape.TriangleMesh;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class GameClientFactory {
    private static final Logger logger = LoggerFactory.getLogger(GameClientFactory.class);

    private final FullMapGraphFactory fullMapGraphFactory;

    public GameClientFactory(FullMapGraphFactory fullMapGraphFactory) {
        if (fullMapGraphFactory == null)
            throw new IllegalArgumentException("fullMapGraphFactory is null");

        this.fullMapGraphFactory = fullMapGraphFactory;
    }

    public NetworkService createNetworkService(String serverBaseURL, UniqueGameIdentifier uniqueGameIdentifier) {
        if (serverBaseURL == null)
            throw new IllegalArgumentException("serverBaseURL is null");
        if (uniqueGameIdentifier == null)
            throw new IllegalArgumentException("uniqueGameIdentifier is null");

        var gameWebClient = WebClient.builder()
                .baseUrl(serverBaseURL + "/games/" + uniqueGameIdentifier.uniqueGameID())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_VALUE)
                .build();

        var fromClientConverter = new FromClientConverter();
        var fullMapConverter = new FullMapConverter();
        var fromServerConverter = new FromServerConverter(fullMapConverter);
        return new NetworkService(gameWebClient, fromClientConverter, fromServerConverter);
    }

    public FullMapAccumulator createFullMapAccumulator() {
        var fullMapRevealer = new FullMapRevealer();
        return new FullMapAccumulator(fullMapRevealer);
    }

    public GameSession createGameSession(NetworkService networkService, FullMapAccumulator fullMapAccumulator) {
        if (networkService == null)
            throw new IllegalArgumentException("networkService is null");
        if (fullMapAccumulator == null)
            throw new IllegalArgumentException("fullMapAccumulator is null");

        return new GameSession(networkService, fullMapAccumulator);
    }

    public HalfMapGenerator createHalfMapGenerator() {
        long seed = new SplittableRandom().nextLong();
        logger.info("Creating HalfMapGenerator with seed {}", seed);
        var splittableRandom = new SplittableRandom(seed);
        return new HalfMapGenerator(splittableRandom);
    }

    public HalfMapValidator createHalfMapValidator() {
        Set<HalfMapValidationRule> rules = Set.of(
                new TerrainRule(), new BorderRule(), new FortRule(), new ConnectivityRule()
        );
        return new HalfMapValidator(rules);
    }

    public AIPlayer createAIPlayer(FullMap fullMap) {
        if (fullMap == null)
            throw new IllegalArgumentException("fullMap is null");

        FullMapGraph fullMapGraph = fullMapGraphFactory.createGraph(fullMap);
        var fullMapSplitter = new FullMapSplitter(fullMap.getOptionalMyFortPosition().orElseThrow());
        var expectedArrivalPathFactory = new ExpectedArrivalPathFactory();
        var knowledgeBase = new KnowledgeBase(fullMapGraph);
        return new AIPlayer(fullMapSplitter, expectedArrivalPathFactory, knowledgeBase);
    }

    public FullMapViewJavaFX createFullMapViewJavaFX(Group worldRoot, Camera camera, Map<String, Image> textures, Image[] waterTextures, Map<String, TriangleMesh> meshes) {
        if (worldRoot == null)
            throw new IllegalArgumentException("worldRoot is null");
        if (camera == null)
            throw new IllegalArgumentException("camera is null");
        if (textures == null)
            throw new IllegalArgumentException("textures is null");
        if (waterTextures == null)
            throw new IllegalArgumentException("waterTextures is null");
        if (meshes == null)
            throw new IllegalArgumentException("meshes is null");

        var cameraMovementDetector = new CameraMovementDetector(camera);
        var assets = new Assets(textures, waterTextures, meshes);
        var terrainManager = new TerrainManager(worldRoot, assets);
        var animationManager = new AnimationManager();
        EntityManager entityManager = new EntityManager(worldRoot, assets, animationManager);
        return new FullMapViewJavaFX(cameraMovementDetector, assets, terrainManager, entityManager);
    }

    public GameController createGameController(GameSession gameSession, HalfMapGenerator halfMapGenerator,
                                               HalfMapValidator halfMapValidator, FullMapViewJavaFX fullMapViewJavaFX) {
        if (gameSession == null)
            throw new IllegalArgumentException("gameSession is null");
        if (halfMapGenerator == null)
            throw new IllegalArgumentException("halfMapGenerator is null");
        if (halfMapValidator == null)
            throw new IllegalArgumentException("halfMapValidator is null");

        var playerModel = new PlayerModel();
        var myPlayerView = new PlayerView("My Player");
        var enemyPlayerView = new PlayerView("Enemy Player");
        var endGameView = new EndGameView();
        playerModel.subscribeOnMyPlayerStateUpdated(myPlayerView);
        playerModel.subscribeOnEnemyPlayerStateUpdated(enemyPlayerView);
        playerModel.subscribeOnGameEnded(endGameView);

        var mapModel = new MapModel();
        var halfMapView = new HalfMapView();
        var fullMapViewFullMapViewCLI = new FullMapViewCLI();
        var halfMapValidationErrorView = new HalfMapValidationErrorView();
        mapModel.subscribeOnHalfMapGenerated(halfMapView);
        mapModel.subscribeOnFullMapUpdated(fullMapViewFullMapViewCLI);
        mapModel.subscribeOnHalfMapValidationErrors(halfMapValidationErrorView);

        if (fullMapViewJavaFX != null)
            mapModel.subscribeOnFullMapUpdated(fullMapViewJavaFX);

        return new GameController(playerModel, mapModel, gameSession, halfMapGenerator, halfMapValidator, this);
    }
}