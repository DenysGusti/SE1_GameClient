package client.main;

import client.data.PlayerInformation;
import client.data.UniqueGameIdentifier;
import client.halfmaplogic.generation.HalfMapGenerator;
import client.main.exception.CommandLineArgumentsException;
import client.modelviewcontroller.controller.GameController;
import client.modelviewcontroller.model.GameModel;
import client.modelviewcontroller.controller.accumulator.FullMapAccumulator;
import client.network.NetworkService;

import client.network.fromclient.FromClientConverter;
import client.network.fromserver.FromServerConverter;
import client.network.fromserver.FullMapConverter;
import client.modelviewcontroller.controller.accumulator.FullMapRevealer;
import client.halfmaplogic.validation.HalfMapValidator;
import client.halfmaplogic.validation.rule.IHalfMapValidationRule;
import client.halfmaplogic.validation.rule.BorderRule;
import client.halfmaplogic.validation.rule.ConnectivityRule;
import client.halfmaplogic.validation.rule.FortRule;
import client.halfmaplogic.validation.rule.TerrainRule;
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
    private static final long POLL_DELAY_MS = 400; // 400ms delay

    public static void validateArguments(String[] args) throws CommandLineArgumentsException {
        Objects.requireNonNull(args, "args must not be null");

        if (args.length != 2 && args.length != 3)
            throw new CommandLineArgumentsException("Wrong number of arguments!");
        if (!args[0].equals("TR"))
            throw new CommandLineArgumentsException("Invalid game visualization mode!");
    }

    private static PlayerInformation loadPlayerInformation() throws IOException {
        var properties = new Properties();
        try (InputStream inputStream = MainClient.class.getClassLoader().getResourceAsStream(CONFIG_FILE_NAME)) {
            properties.load(inputStream);
        }
        String firstName = properties.getProperty("player.firstname");
        String lastName = properties.getProperty("player.lastname");
        String uaccount = properties.getProperty("player.uaccount");
        return new PlayerInformation(firstName, lastName, uaccount);
    }

    private static NetworkService createNetworkService(String serverBaseURL, UniqueGameIdentifier uniqueGameIdentifier) {
        Objects.requireNonNull(serverBaseURL, "serverBaseURL must not be null");
        Objects.requireNonNull(uniqueGameIdentifier, "uniqueGameIdentifier must not be null");

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

    public static void main(String[] args) {
        try {
            validateArguments(args);
        } catch (CommandLineArgumentsException e) {
            logger.error(e.toString());
            return;
        }

        PlayerInformation playerInformation;
        try {
            playerInformation = loadPlayerInformation();
        } catch (FileNotFoundException e) {
            logger.error("Properties file not found.", e);
            return;
        } catch (IOException e) {
            logger.error("Error reading properties file.", e);
            return;
        }

        String serverBaseURL = args[1];
        UniqueGameIdentifier uniqueGameIdentifier;
        if (args.length > 2)
            uniqueGameIdentifier = new UniqueGameIdentifier(args[2]);
        else
            uniqueGameIdentifier = NetworkService.createNewGame(serverBaseURL, true, true).block();

        NetworkService networkService = createNetworkService(serverBaseURL, uniqueGameIdentifier);
        HalfMapGenerator halfMapGenerator = createHalfMapGenerator();
        HalfMapValidator halfMapValidator = createHalfMapValidator();
        FullMapAccumulator fullMapAccumulator = createFullMapAccumulator();

        var gameModel = new GameModel();
        var gameController =
                new GameController(gameModel, networkService, halfMapGenerator, halfMapValidator, fullMapAccumulator);

        gameController.runGame(playerInformation);
    }
}
