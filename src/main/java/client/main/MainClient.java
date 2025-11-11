package client.main;

import client.data.ETerrain;
import client.data.PlayerInformation;
import client.data.XYPair;
import client.data.fromclient.HalfMap;
import client.data.fromserver.GameState;
import client.main.exception.CommandLineArgumentsException;
import client.network.fromserver.FullMapAccumulator;
import client.network.NetworkService;

import client.network.fromclient.FromClientConverter;
import client.network.fromserver.FromServerConverter;
import client.network.fromserver.FullMapConverter;
import client.network.fromserver.FullMapRevealer;
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

    private static NetworkService createNetworkService(String serverBaseURL, String gameID) {
        Objects.requireNonNull(serverBaseURL, "serverBaseURL must not be null");
        Objects.requireNonNull(gameID, "gameID must not be null");

        var gameWebClient = WebClient.builder()
                .baseUrl(serverBaseURL + "/games/" + gameID)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_VALUE)
                .build();
        var fromClientConverter = new FromClientConverter();
        var fullMapConverter = new FullMapConverter();
        var fromServerConverter = new FromServerConverter(fullMapConverter);
        var fullMapRevealer = new FullMapRevealer();
        var fullMapAccumulator = new FullMapAccumulator(fullMapRevealer);
        return new NetworkService(gameWebClient, fromClientConverter, fromServerConverter, fullMapAccumulator);
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
        String gameID;
        if (args.length > 2)
            gameID = args[2];
        else
            gameID = NetworkService.createNewGame(serverBaseURL, true, true);
        NetworkService networkService = createNetworkService(serverBaseURL, gameID);

        networkService.registerPlayer(playerInformation).block();
        logger.info("Player registration complete.");

        GameState gameState;

        while (true) {
            gameState = networkService.receiveGameState().blockOptional().orElseThrow();

            if (gameState.myPlayerMustWait())
                logger.debug("Waiting for my turn...");
            else
                break;
        }

        logger.debug(gameState.toString());

        var nodes = new HashMap<XYPair, ETerrain>();
        nodes.put(new XYPair(0, 0), ETerrain.Grass);
        nodes.put(new XYPair(1, 0), ETerrain.Grass);
        nodes.put(new XYPair(2, 0), ETerrain.Grass);
        nodes.put(new XYPair(3, 0), ETerrain.Grass);
        nodes.put(new XYPair(4, 0), ETerrain.Grass);
        nodes.put(new XYPair(5, 0), ETerrain.Grass);
        nodes.put(new XYPair(6, 0), ETerrain.Grass);
        nodes.put(new XYPair(7, 0), ETerrain.Grass);
        nodes.put(new XYPair(8, 0), ETerrain.Grass);
        nodes.put(new XYPair(9, 0), ETerrain.Grass);

        nodes.put(new XYPair(0, 1), ETerrain.Grass);
        nodes.put(new XYPair(1, 1), ETerrain.Water);
        nodes.put(new XYPair(2, 1), ETerrain.Water);
        nodes.put(new XYPair(3, 1), ETerrain.Water);
        nodes.put(new XYPair(4, 1), ETerrain.Water);
        nodes.put(new XYPair(5, 1), ETerrain.Water);
        nodes.put(new XYPair(6, 1), ETerrain.Water);
        nodes.put(new XYPair(7, 1), ETerrain.Water);
        nodes.put(new XYPair(8, 1), ETerrain.Water);
        nodes.put(new XYPair(9, 1), ETerrain.Grass);

        nodes.put(new XYPair(0, 2), ETerrain.Grass);
        nodes.put(new XYPair(1, 2), ETerrain.Mountain);
        nodes.put(new XYPair(2, 2), ETerrain.Mountain);
        nodes.put(new XYPair(3, 2), ETerrain.Mountain);
        nodes.put(new XYPair(4, 2), ETerrain.Mountain);
        nodes.put(new XYPair(5, 2), ETerrain.Mountain);
        nodes.put(new XYPair(6, 2), ETerrain.Mountain);
        nodes.put(new XYPair(7, 2), ETerrain.Mountain);
        nodes.put(new XYPair(8, 2), ETerrain.Mountain);
        nodes.put(new XYPair(9, 2), ETerrain.Grass);

        nodes.put(new XYPair(0, 3), ETerrain.Grass);
        nodes.put(new XYPair(1, 3), ETerrain.Grass);
        nodes.put(new XYPair(2, 3), ETerrain.Grass);
        nodes.put(new XYPair(3, 3), ETerrain.Grass);
        nodes.put(new XYPair(4, 3), ETerrain.Grass);
        nodes.put(new XYPair(5, 3), ETerrain.Grass);
        nodes.put(new XYPair(6, 3), ETerrain.Grass);
        nodes.put(new XYPair(7, 3), ETerrain.Grass);
        nodes.put(new XYPair(8, 3), ETerrain.Grass);
        nodes.put(new XYPair(9, 3), ETerrain.Grass);

        nodes.put(new XYPair(0, 4), ETerrain.Grass);
        nodes.put(new XYPair(1, 4), ETerrain.Grass);
        nodes.put(new XYPair(2, 4), ETerrain.Grass);
        nodes.put(new XYPair(3, 4), ETerrain.Grass);
        nodes.put(new XYPair(4, 4), ETerrain.Grass);
        nodes.put(new XYPair(5, 4), ETerrain.Grass);
        nodes.put(new XYPair(6, 4), ETerrain.Grass);
        nodes.put(new XYPair(7, 4), ETerrain.Grass);
        nodes.put(new XYPair(8, 4), ETerrain.Grass);
        nodes.put(new XYPair(9, 4), ETerrain.Grass);

        var potentialForts = Set.of(new XYPair(0, 0));

        var halfMap = new HalfMap(nodes, potentialForts);
        networkService.sendHalfMap(halfMap).block();
    }
}
