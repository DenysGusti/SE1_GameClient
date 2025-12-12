package client.main;

import client.ai.AIPlayer;
import client.ai.FullMapSplitter;
import client.ai.graph.FullMapGraphFactory;
import client.ai.mountain.*;
import client.ai.tsp.*;
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
        var fullMapService = new FullMapSplitter();

        var heldKarp = new HeldKarpTraversalStrategy();
        var nearestNeighbor = new NearestNeighborTraversalStrategy();
        var twoOpt = new TwoOptTraversalStrategy(nearestNeighbor);

        long seedRW0 = new SplittableRandom().nextLong();
        logger.info("Creating Random Walk 0 with seed {}", seedRW0);
        var splittableRandomRW0 = new SplittableRandom(seedRW0);
        var randomWalk0 = new RandomWalkTraversalStrategy(splittableRandomRW0);

        long seedRW1 = new SplittableRandom().nextLong();
        logger.info("Creating Random Walk 1 with seed {}", seedRW1);
        var splittableRandomRW1 = new SplittableRandom(seedRW1);
        var randomWalk1 = new RandomWalkTraversalStrategy(splittableRandomRW1);

        long seedM = new SplittableRandom().nextLong();
        logger.info("Creating Metropolis with seed {}", seedM);
        var splittableRandomM = new SplittableRandom(seedM);
        var metropolis = new MetropolisTraversalStrategy(randomWalk0, splittableRandomM);

        long seedSA = new SplittableRandom().nextLong();
        logger.info("Creating Simulated Annealing with seed {}", seedSA);
        var splittableRandomSA = new SplittableRandom(seedSA);
        var simulatedAnnealing = new SimulatedAnnealingTraversalStrategy(randomWalk1, splittableRandomSA);

        var nodeTraversalStrategy = new GeneralNodeTraversalStrategy(heldKarp, metropolis, simulatedAnnealing);
        var pathOptimizer = new PathOptimizer();
        var mountainSelector = new GreedyMountainSelector(nodeTraversalStrategy, pathOptimizer);

        return new AIPlayer(fullMapService, mountainSelector);
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
        var playerView = new PlayerView();
        playerModel.subscribeOnMyPlayerStateUpdated(playerView::renderMyPlayerState);
        playerModel.subscribeOnEnemyPlayerStateUpdated(playerView::renderEnemyPlayerState);

        var mapModel = new MapModel();
        var mapView = new MapView();
        mapModel.subscribeOnFullMapUpdated(mapView::renderFullMap);

        var fullMapGraphFactory = new FullMapGraphFactory();

        return new GameController(playerModel, mapModel, gameSession, halfMapGenerator, halfMapValidator,
                aiPlayer, fullMapGraphFactory);
    }
}