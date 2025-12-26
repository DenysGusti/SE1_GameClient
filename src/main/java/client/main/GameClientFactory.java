package client.main;

import client.ai.AIPlayer;
import client.ai.path.ExpectedArrivalPathFactory;
import client.ai.utilities.FullMapUtilities;
import client.ai.graph.FullMapGraphFactory;
import client.halfmaplogic.generation.HalfMapGenerator;
import client.halfmaplogic.validation.HalfMapValidator;
import client.halfmaplogic.validation.rule.*;
import client.modelviewcontroller.controller.GameController;
import client.modelviewcontroller.model.*;
import client.modelviewcontroller.view.*;
import client.network.*;
import client.network.accumulator.*;
import client.network.fromserver.*;
import client.network.fromclient.FromClientConverter;
import client.data.PlayerInformation;
import client.data.UniqueGameIdentifier;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class GameClientFactory {
    private static final Logger logger = LoggerFactory.getLogger(GameClientFactory.class);

    public PlayerInformation createPlayerInformation(Properties properties) {
        if (properties == null)
            throw new IllegalArgumentException("properties is null");

        String firstName = properties.getProperty("player.firstName");
        String lastName = properties.getProperty("player.lastName");
        String uAccount = properties.getProperty("player.uAccount");
        return new PlayerInformation(firstName, lastName, uAccount);
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
//        long seed = -8929787966741954084L;
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

    public AIPlayer createAIPlayer() {
        var fullMapSplitter = new FullMapUtilities();
        var expectedArrivalPathFactory = new ExpectedArrivalPathFactory();
        return new AIPlayer(fullMapSplitter, expectedArrivalPathFactory);
    }

    public GameController createGameController(GameSession gameSession,
                                               HalfMapGenerator halfMapGenerator,
                                               HalfMapValidator halfMapValidator,
                                               AIPlayer aiPlayer) {
        if (gameSession == null)
            throw new IllegalArgumentException("gameSession is null");
        if (halfMapGenerator == null)
            throw new IllegalArgumentException("halfMapGenerator is null");
        if (halfMapValidator == null)
            throw new IllegalArgumentException("halfMapValidator is null");
        if (aiPlayer == null)
            throw new IllegalArgumentException("aiPlayer is null");

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

        var fullMapGraphFactory = new FullMapGraphFactory();

        return new GameController(playerModel, mapModel, gameSession, halfMapGenerator, halfMapValidator,
                aiPlayer, fullMapGraphFactory);
    }
}