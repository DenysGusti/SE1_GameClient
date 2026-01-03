package client.main;

import client.ai.AIPlayer;
import client.data.PlayerInformation;
import client.data.UniqueGameIdentifier;
import client.halfmaplogic.generation.HalfMapGenerator;
import client.halfmaplogic.validation.HalfMapValidator;
import client.main.exception.CommandLineArgumentsException;
import client.modelviewcontroller.controller.GameController;
import client.modelviewcontroller.obj.ObjTriangleMeshFactory;
import client.modelviewcontroller.view.FullMapViewJavaFX;
import client.network.GameSession;
import client.network.NetworkService;
import client.network.accumulator.FullMapAccumulator;

import javafx.scene.shape.TriangleMesh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.scene.image.Image;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

public class MainClient {
    private static final Logger logger = LoggerFactory.getLogger(MainClient.class);

    private static final String CONFIG_FILE_PATH = "/config.properties";
    private static final String[] BLOCK_TEXTURE_NAMES = new String[]{"grass", "stone", "snow"};
    private static final int BLOCK_TEXTURE_WIDTH = 1024;
    private static final int BLOCK_TEXTURE_HEIGHT = 768;
    private static final String[] ENTITY_TEXTURE_NAMES = new String[]{"gold_rabbit", "salt_rabbit"};
    private static final int ENTITY_TEXTURE_WIDTH = 1024;
    private static final int ENTITY_TEXTURE_HEIGHT = 512;
    private static final ObjTriangleMeshFactory objTriangleMeshFactory = new ObjTriangleMeshFactory();
    private static final String[] MESH_NAMES = new String[]{"block", "block_7-8", "block_1-8", "rabbit"};

    public static void validateArguments(String[] args) throws CommandLineArgumentsException {
        if (args == null)
            throw new IllegalArgumentException("args is null");
        if (args.length != 2 && args.length != 3)
            throw new CommandLineArgumentsException("Wrong number of arguments!");
        if (!args[0].equals("TR") && !args[0].equals("GUI"))
            throw new CommandLineArgumentsException("Invalid game visualization mode!");
    }

    private static Properties loadProperties() throws IOException {
        var properties = new Properties();
        try (InputStream inputStream = MainClient.class.getResourceAsStream(CONFIG_FILE_PATH)) {
            if (inputStream == null)
                throw new FileNotFoundException("Property file '" + CONFIG_FILE_PATH + "' not found in the classpath");

            properties.load(inputStream);
        }
        return properties;
    }

    private static Image loadTexture(String textureFile, int width, int height) throws IOException {
        if (textureFile == null)
            throw new IllegalArgumentException("textureFile is null");

        Image texture;
        try (InputStream inputStream = MainClient.class.getResourceAsStream(textureFile)) {
            if (inputStream == null)
                throw new FileNotFoundException("Texture file '" + textureFile + "' not found in the classpath");

            texture = new Image(inputStream, width, height, true, false);
        }
        return texture;
    }

    private static TriangleMesh loadTriangleMesh(String objFile) throws IOException {
        if (objFile == null)
            throw new IllegalArgumentException("objFile is null");

        TriangleMesh triangleMesh;
        try (InputStream inputStream = MainClient.class.getResourceAsStream(objFile)) {
            if (inputStream == null)
                throw new FileNotFoundException("OBJ file '" + objFile + "' not found in the classpath");

            var objContent = new String(inputStream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            triangleMesh = objTriangleMeshFactory.createTriangleMesh(objContent);
        }
        return triangleMesh;
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

        Map<String, Image> textures = new HashMap<>();
        var waterTextures = new Image[64];
        try {
            for (String textureName : BLOCK_TEXTURE_NAMES) {
                String textureFile = String.format("/assets/%s.png", textureName);
                Image textureImage = loadTexture(textureFile, BLOCK_TEXTURE_WIDTH, BLOCK_TEXTURE_HEIGHT);
                textures.put(textureName, textureImage);
            }
            for (int i = 0; i < waterTextures.length; ++i) {
                String textureFile = String.format("/assets/water/water_%d.png", i);
                Image textureImage = loadTexture(textureFile, BLOCK_TEXTURE_WIDTH, BLOCK_TEXTURE_HEIGHT);
                waterTextures[i] = textureImage;
            }
            for (String textureName : ENTITY_TEXTURE_NAMES) {
                String textureFile = String.format("/assets/%s.png", textureName);
                Image textureImage = loadTexture(textureFile, ENTITY_TEXTURE_WIDTH, ENTITY_TEXTURE_HEIGHT);
                textures.put(textureName, textureImage);
            }
        } catch (IOException e) {
            logger.error("Error reading texture file.", e);
            return;
        }

        Map<String, TriangleMesh> meshes = new HashMap<>();
        try {
            for (String meshName : MESH_NAMES) {
                String objMeshFile = String.format("/assets/%s.obj", meshName);
                TriangleMesh triangleMesh = loadTriangleMesh(objMeshFile);
                meshes.put(meshName, triangleMesh);
            }
        } catch (IOException e) {
            logger.error("Error reading obj file.", e);
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

        FullMapViewJavaFX fullMapViewJavaFX = null;
        if (args[0].equals("GUI"))
            fullMapViewJavaFX = gameClientFactory.createFullMapViewJavaFX(textures, waterTextures, meshes);

        GameController gameController = gameClientFactory.createGameController(
                gameSession, halfMapGenerator, halfMapValidator, aiPlayer, fullMapViewJavaFX
        );

        PlayerInformation playerInformation = gameClientFactory.createPlayerInformation(properties);
        gameController.runGame(playerInformation);
    }
}