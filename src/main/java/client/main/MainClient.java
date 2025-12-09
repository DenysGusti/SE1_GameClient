package client.main;

import client.ai.AIPlayer;
import client.data.PlayerInformation;
import client.data.UniqueGameIdentifier;
import client.halfmaplogic.generation.HalfMapGenerator;
import client.halfmaplogic.validation.HalfMapValidator;
import client.main.exception.CommandLineArgumentsException;
import client.modelviewcontroller.controller.GameController;
import client.network.GameSession;
import client.network.NetworkService;
import client.network.accumulator.FullMapAccumulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

public class MainClient {
    private static final Logger logger = LoggerFactory.getLogger(MainClient.class);
    private static final String CONFIG_FILE_NAME = "config.properties";

    public static void validateArguments(String[] args) throws CommandLineArgumentsException {
        if (args == null)
            throw new IllegalArgumentException("args is null");
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

        var gameClientFactory = new GameClientFactory();

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

        NetworkService networkService = gameClientFactory.createNetworkService(serverBaseURL, uniqueGameIdentifier);
        FullMapAccumulator fullMapAccumulator = gameClientFactory.createFullMapAccumulator();
        GameSession gameSession = gameClientFactory.createGameSession(networkService, fullMapAccumulator);

        HalfMapGenerator halfMapGenerator = gameClientFactory.createHalfMapGenerator();
        HalfMapValidator halfMapValidator = gameClientFactory.createHalfMapValidator();
        AIPlayer aiPlayer = gameClientFactory.createAIPlayer();

        GameController gameController = gameClientFactory.createGameController(
                gameSession, halfMapGenerator, halfMapValidator, aiPlayer
        );

        PlayerInformation playerInformation = gameClientFactory.createPlayerInformation(properties);
        gameController.runGame(playerInformation);
    }
}