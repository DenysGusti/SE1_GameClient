package client.main;

import client.ai.AIPlayer;
import client.data.PlayerInformation;
import client.data.UniqueGameIdentifier;
import client.halfmaplogic.generation.HalfMapGenerator;
import client.halfmaplogic.validation.HalfMapValidator;
import client.main.exception.CommandLineArgumentsException;
import client.modelviewcontroller.controller.GameController;
import client.main.obj.ObjTriangleMeshFactory;
import client.modelviewcontroller.view.FullMapViewJavaFX;
import client.network.GameSession;
import client.network.NetworkService;
import client.network.accumulator.FullMapAccumulator;

import javafx.scene.shape.TriangleMesh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.scene.image.Image;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class MainClient {
    private static final Logger logger = LoggerFactory.getLogger(MainClient.class);

    private static final String PROPERTIES_PATH = "/config.properties";

    private static final String BLOCK_TEXTURES_FOLDER = "/assets/textures/block";
    private static final String ENTITY_TEXTURES_FOLDER = "/assets/textures/entity";
    private static final String MESHES_FOLDER = "/assets/meshes";

    private static final int BLOCK_TEXTURE_WIDTH = 1024;
    private static final int BLOCK_TEXTURE_HEIGHT = 768;
    private static final Map<String, String> BLOCK_TEXTURES_PATHS = Map.of(
            "grass", BLOCK_TEXTURES_FOLDER + "/grass.png",
            "stone", BLOCK_TEXTURES_FOLDER + "/stone.png",
            "snow", BLOCK_TEXTURES_FOLDER + "/snow.png"
    );
    private static final String WATER_TEXTURES_FOLDER_PATH = BLOCK_TEXTURES_FOLDER + "/water";

    private static final int ENTITY_TEXTURE_WIDTH = 1024;
    private static final int ENTITY_TEXTURE_HEIGHT = 512;
    private static final Map<String, String> ENTITY_TEXTURES_PATHS = Map.of(
            "gold_rabbit", ENTITY_TEXTURES_FOLDER + "/rabbit/gold.png",
            "salt_rabbit", ENTITY_TEXTURES_FOLDER + "/rabbit/salt.png"
    );

    private static final Map<String, String> MESHES_PATHS = Map.of(
            "block", MESHES_FOLDER + "/block.obj",
            "block_14-16", MESHES_FOLDER + "/block_14-16.obj",
            "block_2-16", MESHES_FOLDER + "/block_2-16.obj",
            "rabbit", MESHES_FOLDER + "/rabbit.obj"
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
        var objTriangleMeshFactory = new ObjTriangleMeshFactory();
        var assetLoader = new AssetLoader(objTriangleMeshFactory);

        Map<String, Image> textures;
        Image[] waterTextures;
        Map<String, TriangleMesh> meshes;
        try {
            configurationManager.loadProperties(PROPERTIES_PATH);
            textures = assetLoader.loadTextures(BLOCK_TEXTURES_PATHS, BLOCK_TEXTURE_WIDTH, BLOCK_TEXTURE_HEIGHT,
                    ENTITY_TEXTURES_PATHS, ENTITY_TEXTURE_WIDTH, ENTITY_TEXTURE_HEIGHT);
            waterTextures = assetLoader.loadTextureArray(WATER_TEXTURES_FOLDER_PATH, BLOCK_TEXTURE_WIDTH, BLOCK_TEXTURE_HEIGHT);
            meshes = assetLoader.loadMeshes(MESHES_PATHS);
        } catch (IOException e) {
            logger.error("Error reading file.", e);
            return;
        }

        var gameClientFactory = new GameClientFactory();

        String serverBaseURL = args[1];
        UniqueGameIdentifier uniqueGameIdentifier;
        if (args.length > 2)
            uniqueGameIdentifier = new UniqueGameIdentifier(args[2]);
        else {
            boolean debugMode = configurationManager.getBoolean("game.debugMode");
            boolean dummyCompetition = configurationManager.getBoolean("game.dummyCompetition");

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

        String firstName = configurationManager.getString("player.firstName");
        String lastName = configurationManager.getString("player.lastName");
        String uAccount = configurationManager.getString("player.uAccount");
        PlayerInformation playerInformation = new PlayerInformation(firstName, lastName, uAccount);

        gameController.runGame(playerInformation);
    }
}