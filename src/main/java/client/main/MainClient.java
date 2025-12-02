package client.main;

import client.ai.AIPlayer;
import client.ai.graph.FullMapGraphFactory;
import client.ai.FullMapSplitter;
import client.ai.tsp.*;
import client.data.PlayerInformation;
import client.data.UniqueGameIdentifier;
import client.halfmaplogic.generation.HalfMapGenerator;
import client.main.exception.CommandLineArgumentsException;
import client.modelviewcontroller.controller.GameController;
import client.network.GameSession;
import client.network.accumulator.FullMapAccumulator;
import client.modelviewcontroller.model.*;
import client.modelviewcontroller.view.*;
import client.network.NetworkService;
import client.network.fromclient.FromClientConverter;
import client.network.fromserver.FromServerConverter;
import client.network.fromserver.FullMapConverter;
import client.network.accumulator.FullMapRevealer;
import client.halfmaplogic.validation.HalfMapValidator;
import client.halfmaplogic.validation.rule.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class MainClient {
    private static final Logger logger = LoggerFactory.getLogger(MainClient.class);
    private static final String CONFIG_FILE_NAME = "config.properties";

    public static void validateArguments(String[] args) throws CommandLineArgumentsException {
        if (args == null)
            throw new IllegalArgumentException("args must not be null");

        if (args.length != 2 && args.length != 3)
            throw new CommandLineArgumentsException("Wrong number of arguments!");
        if (!args[0].equals("TR"))
            throw new CommandLineArgumentsException("Invalid game visualization mode!");
    }

    private static Properties loadProperties() throws IOException {
        var properties = new Properties();
        try (InputStream inputStream = MainClient.class.getClassLoader().getResourceAsStream(CONFIG_FILE_NAME)) {
            if (inputStream == null)
                throw new FileNotFoundException("Property file '" + CONFIG_FILE_NAME + "' not found in the classpath");

            properties.load(inputStream);
        }
        return properties;
    }

    private static PlayerInformation loadPlayerInformation(Properties properties) {
        String firstName = properties.getProperty("player.firstName");
        String lastName = properties.getProperty("player.lastName");
        String uAccount = properties.getProperty("player.uAccount");
        return new PlayerInformation(firstName, lastName, uAccount);
    }

    private static NetworkService createNetworkService(String serverBaseURL, UniqueGameIdentifier uniqueGameIdentifier) {
        if (serverBaseURL == null)
            throw new IllegalArgumentException("serverBaseURL must not be null");
        if (uniqueGameIdentifier == null)
            throw new IllegalArgumentException("uniqueGameIdentifier must not be null");

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

    private static FullMapAccumulator createFullMapAccumulator() {
        var fullMapRevealer = new FullMapRevealer();
        return new FullMapAccumulator(fullMapRevealer);
    }

    private static GameSession createGameSession(NetworkService networkService, FullMapAccumulator fullMapAccumulator) {
        if (networkService == null)
            throw new IllegalArgumentException("networkService must not be null");
        if (fullMapAccumulator == null)
            throw new IllegalArgumentException("fullMapAccumulator must not be null");

        return new GameSession(networkService, fullMapAccumulator);
    }

    private static HalfMapGenerator createHalfMapGenerator() {
        long seed = new Random().nextLong();
        logger.info("Creating HalfMapGenerator with seed {}", seed);
        var random = new Random(seed);
        return new HalfMapGenerator(random);
    }

    private static HalfMapValidator createHalfMapValidator() {
        Collection<IHalfMapValidationRule> rules = List.of(
                new TerrainRule(), new BorderRule(), new FortRule(), new ConnectivityRule()
        );
        return new HalfMapValidator(rules);
    }

    private static AIPlayer createAIPlayer() {
        var heldKarp = new HeldKarpTraversalStrategy();

        var fullMapService = new FullMapSplitter();
        var twoOptHelper = new TwoOptHelper();

        var nearestNeighbour = new NearestNeighbourTraversalStrategy();
        var twoOpt = new TwoOptTraversalStrategy(nearestNeighbour, twoOptHelper);
        var simulatedAnnealing = new SimulatedAnnealingTraversalStrategy(twoOpt, new Random(), twoOptHelper);

        var nodeTraversalStrategy = new GeneralNodeTraversalStrategy(heldKarp, simulatedAnnealing);

        return new AIPlayer(fullMapService, nodeTraversalStrategy);
    }

    public static void main(String[] args) {
        try {
            validateArguments(args);
        } catch (CommandLineArgumentsException e) {
            logger.error(e.toString());
            return;
        }

        Properties properties;
        try {
            properties = loadProperties();
        } catch (IOException e) {
            logger.error("Error reading properties file.", e);
            return;
        }

        String serverBaseURL = args[1];
        UniqueGameIdentifier uniqueGameIdentifier;
        if (args.length > 2)
            uniqueGameIdentifier = new UniqueGameIdentifier(args[2]);
        else {
            String debugModeString = Objects.requireNonNull(properties.getProperty("game.debugMode"));
            String dummyCompetitionString = Objects.requireNonNull(properties.getProperty("game.dummyCompetition"));

            boolean debugMode = Boolean.parseBoolean(debugModeString);
            boolean dummyCompetition = Boolean.parseBoolean(dummyCompetitionString);

            uniqueGameIdentifier = NetworkService.createNewGame(serverBaseURL, debugMode, dummyCompetition).block();
        }

        NetworkService networkService = createNetworkService(serverBaseURL, uniqueGameIdentifier);
        FullMapAccumulator fullMapAccumulator = createFullMapAccumulator();
        GameSession gameSession = createGameSession(networkService, fullMapAccumulator);

        HalfMapGenerator halfMapGenerator = createHalfMapGenerator();
        HalfMapValidator halfMapValidator = createHalfMapValidator();

        var playerModel = new PlayerModel();
        var playerView = new PlayerView();
        playerModel.subscribeOnMyPlayerStateUpdated(playerView::renderMyPlayerState);
        playerModel.subscribeOnEnemyPlayerStateUpdated(playerView::renderEnemyPlayerState);

        var mapModel = new MapModel();
        var mapView = new MapView();
        mapModel.subscribeOnFullMapUpdated(mapView::renderFullMap);

        var aiPlayer = createAIPlayer();
        var fullMapGraphFactory = new FullMapGraphFactory();

        var gameController = new GameController(playerModel, mapModel, gameSession, halfMapGenerator, halfMapValidator,
                aiPlayer, fullMapGraphFactory);

        PlayerInformation playerInformation = loadPlayerInformation(properties);
        gameController.runGame(playerInformation);
    }
}
