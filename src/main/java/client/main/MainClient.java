package client.main;

import client.ai.AIPlayer;
import client.data.PlayerInformation;
import client.data.UniqueGameIdentifier;
import client.halfmaplogic.generation.HalfMapGenerator;
import client.halfmaplogic.validation.HalfMapValidator;
import client.main.exception.CommandLineArgumentsException;
import client.modelviewcontroller.assets.AssetLoader;
import client.modelviewcontroller.controller.GameController;
import client.modelviewcontroller.assets.ObjTriangleMeshFactory;
import client.modelviewcontroller.javafx.JavaFXApplication;
import client.modelviewcontroller.view.FullMapViewJavaFX;
import client.network.GameSession;
import client.network.NetworkService;
import client.network.accumulator.FullMapAccumulator;

import javafx.application.Application;
import javafx.scene.shape.TriangleMesh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.scene.image.Image;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainClient {
    private static final Logger logger = LoggerFactory.getLogger(MainClient.class);

    private static final String PROPERTIES_PATH = "/config.properties";

    private static final String BLOCK_TEXTURES_FOLDER = "/assets/textures/block/";
    private static final String ENTITY_TEXTURES_FOLDER = "/assets/textures/entity/";
    private static final String ITEM_TEXTURES_FOLDER = "/assets/textures/item/";
    private static final String MESHES_FOLDER = "/assets/meshes/";

    private static final int BLOCK_TEXTURE_WIDTH = 1024;
    private static final int BLOCK_TEXTURE_HEIGHT = 768;
    private static final Map<String, String> BLOCK_TEXTURES_PATHS = Map.of(
            "grass", BLOCK_TEXTURES_FOLDER + "grass.png",
            "stone", BLOCK_TEXTURES_FOLDER + "stone.png",
            "snow", BLOCK_TEXTURES_FOLDER + "snow.png",
            "dirt_path", BLOCK_TEXTURES_FOLDER + "dirt_path.png",
            "ice", BLOCK_TEXTURES_FOLDER + "ice.png",
            "red_wool", BLOCK_TEXTURES_FOLDER + "red_wool.png",
            "cyan_wool", BLOCK_TEXTURES_FOLDER + "cyan_wool.png"
    );
    private static final String WATER_TEXTURES_FOLDER = BLOCK_TEXTURES_FOLDER + "water/";

    private static final int ENTITY_TEXTURE_WIDTH = 1024;
    private static final int ENTITY_TEXTURE_HEIGHT = 512;
    private static final Map<String, String> ENTITY_TEXTURES_PATHS = Map.of(
            "gold_rabbit", ENTITY_TEXTURES_FOLDER + "rabbit/gold.png",
            "salt_rabbit", ENTITY_TEXTURES_FOLDER + "rabbit/salt.png"
    );
    private static final int ITEM_TEXTURE_WIDTH = 256;
    private static final int ITEM_TEXTURE_HEIGHT = 256;
    private static final Map<String, String> ITEM_TEXTURES_PATHS = Map.of(
            "emerald", ITEM_TEXTURES_FOLDER + "emerald.png"
    );

    private static final Map<String, String> MESHES_PATHS = Map.of(
            "block_02-16", MESHES_FOLDER + "block_02-16.obj",
            "block_14-16", MESHES_FOLDER + "block_14-16.obj",
            "block_15-16", MESHES_FOLDER + "block_15-16.obj",
            "block_16-16", MESHES_FOLDER + "block_16-16.obj",
            "rabbit", MESHES_FOLDER + "rabbit.obj",
            "emerald", MESHES_FOLDER + "emerald.obj"
    );

    public static void validateArguments(String[] args) throws CommandLineArgumentsException {
        if (args == null)
            throw new IllegalArgumentException("args is null");
        if (args.length != 2 && args.length != 3)
            throw new CommandLineArgumentsException("Wrong number of arguments!");
        if (!args[0].equals("TR") && !args[0].equals("GUI"))
            throw new CommandLineArgumentsException("Invalid game visualization mode!");
    }

    public static void main(String[] args) {
        try {
            validateArguments(args);
        } catch (CommandLineArgumentsException e) {
            logger.error(e.toString());
            return;
        }

        var configurationManager = new ConfigurationManager();
        try {
            configurationManager.loadProperties(PROPERTIES_PATH);
        } catch (IOException e) {
            logger.error("Error reading properties file.", e);
            return;
        }

        String serverBaseURL = args[1];
        UniqueGameIdentifier uniqueGameIdentifier;
        if (args.length > 2)
            uniqueGameIdentifier = new UniqueGameIdentifier(args[2]);
        else {
            boolean debugMode = configurationManager.getBoolean("game.debugMode");
            boolean dummyCompetition = configurationManager.getBoolean("game.dummyCompetition");

            uniqueGameIdentifier = NetworkService.createNewGame(serverBaseURL, debugMode, dummyCompetition).block();
        }

        var gameClientFactory = new GameClientFactory();

        NetworkService networkService = gameClientFactory.createNetworkService(serverBaseURL, uniqueGameIdentifier);
        FullMapAccumulator fullMapAccumulator = gameClientFactory.createFullMapAccumulator();
        GameSession gameSession = gameClientFactory.createGameSession(networkService, fullMapAccumulator);

        HalfMapGenerator halfMapGenerator = gameClientFactory.createHalfMapGenerator();
        HalfMapValidator halfMapValidator = gameClientFactory.createHalfMapValidator();

        FullMapViewJavaFX fullMapViewJavaFX = null;
        if (args[0].equals("GUI")) {
            var objTriangleMeshFactory = new ObjTriangleMeshFactory();
            var assetLoader = new AssetLoader(objTriangleMeshFactory);

            Map<String, Image> textures = new HashMap<>();
            Image[] waterTextures;
            Map<String, TriangleMesh> meshes;

            try {
                Map<String, Image> blockTextures = assetLoader.loadTextures(BLOCK_TEXTURES_PATHS, BLOCK_TEXTURE_WIDTH, BLOCK_TEXTURE_HEIGHT);
                Map<String, Image> entityTextures = assetLoader.loadTextures(ENTITY_TEXTURES_PATHS, ENTITY_TEXTURE_WIDTH, ENTITY_TEXTURE_HEIGHT);
                Map<String, Image> itemTextures = assetLoader.loadTextures(ITEM_TEXTURES_PATHS, ITEM_TEXTURE_WIDTH, ITEM_TEXTURE_HEIGHT);
                textures.putAll(blockTextures);
                textures.putAll(entityTextures);
                textures.putAll(itemTextures);
                waterTextures = assetLoader.loadTextureArray(WATER_TEXTURES_FOLDER, BLOCK_TEXTURE_WIDTH, BLOCK_TEXTURE_HEIGHT);
                meshes = assetLoader.loadMeshes(MESHES_PATHS);
            } catch (IOException e) {
                logger.error("Error reading textures or meshes file.", e);
                return;
            }

            new Thread(() -> Application.launch(JavaFXApplication.class)).start();
            fullMapViewJavaFX = gameClientFactory.createFullMapViewJavaFX(JavaFXApplication.getWorldGroup(),
                    JavaFXApplication.getCamera(), textures, waterTextures, meshes);
        }

        GameController gameController =
                gameClientFactory.createGameController(gameSession, halfMapGenerator, halfMapValidator, fullMapViewJavaFX);

        String firstName = configurationManager.getString("player.firstName");
        String lastName = configurationManager.getString("player.lastName");
        String uAccount = configurationManager.getString("player.uAccount");
        PlayerInformation playerInformation = new PlayerInformation(firstName, lastName, uAccount);

        gameController.runGame(playerInformation);
    }
}