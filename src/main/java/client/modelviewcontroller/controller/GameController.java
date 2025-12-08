package client.modelviewcontroller.controller;

import client.ai.*;
import client.ai.graph.*;
import client.data.PlayerInformation;
import client.data.fromclient.EMove;
import client.data.fromclient.HalfMap;
import client.data.fromserver.GameState;
import client.halfmaplogic.generation.HalfMapGenerator;
import client.halfmaplogic.validation.HalfMapValidator;
import client.halfmaplogic.validation.exception.HalfMapGenerationException;
import client.modelviewcontroller.model.MapModel;
import client.modelviewcontroller.model.PlayerModel;
import client.network.GameSession;
import client.halfmaplogic.validation.Notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameController {
    private static final Logger logger = LoggerFactory.getLogger(GameController.class);

    private static final int HALF_MAP_GENERATION_ATTEMPTS = 100_000;

    private final PlayerModel playerModel;
    private final MapModel mapModel;
    private final GameSession gameSession;
    private final HalfMapGenerator halfMapGenerator;
    private final HalfMapValidator halfMapValidator;
    private final AIPlayer aiPlayer;
    private final FullMapGraphFactory fullMapGraphFactory;

    public GameController(PlayerModel playerModel, MapModel mapModel, GameSession gameSession,
                          HalfMapGenerator halfMapGenerator, HalfMapValidator halfMapValidator, AIPlayer aiPlayer,
                          FullMapGraphFactory fullMapGraphFactory) {
        if (playerModel == null)
            throw new IllegalArgumentException("playerModel is null");
        if (mapModel == null)
            throw new IllegalArgumentException("mapModel is null");
        if (gameSession == null)
            throw new IllegalArgumentException("gameSession is null");
        if (halfMapGenerator == null)
            throw new IllegalArgumentException("halfMapGenerator is null");
        if (halfMapValidator == null)
            throw new IllegalArgumentException("halfMapValidator is null");
        if (aiPlayer == null)
            throw new IllegalArgumentException("aiPlayer is null");
        if (fullMapGraphFactory == null)
            throw new IllegalArgumentException("fullMapGraphFactory is null");

        this.playerModel = playerModel;
        this.mapModel = mapModel;
        this.gameSession = gameSession;
        this.halfMapGenerator = halfMapGenerator;
        this.halfMapValidator = halfMapValidator;
        this.aiPlayer = aiPlayer;
        this.fullMapGraphFactory = fullMapGraphFactory;
    }

    public void runGame(PlayerInformation playerInformation) {
        if (playerInformation == null)
            throw new IllegalArgumentException("playerInformation is null");

        gameSession.registerPlayer(playerInformation).block();
        logger.info("Player registration complete.");

        gameSession.pollForNewGameState()
                .doOnNext(this::updateModels)
                .filter(GameState::myPlayerMustAct)
                .next().blockOptional().orElseThrow();

        HalfMap halfMap = generateHalfMap();
        gameSession.sendHalfMap(halfMap).block();
        logger.info("Half-map sent successfully.");

        while (true) {
            GameState currentState = gameSession.pollForNewGameState()
                    .doOnNext(this::updateModels)
                    .filter(GameState::myPlayerMustNotWait)
                    .next().blockOptional().orElseThrow();

            if (currentState.myPlayerWonOrLost()) {
                logger.info("Game has ended.");
                playerModel.updateGameEnd(currentState.myPlayerGameState());
                break;
            }

            if (!currentState.myPlayerMustAct())
                continue;

            if (!aiPlayer.isFullMapGraphInitialized()) {
                FullMapGraph fullMapGraph = fullMapGraphFactory.createGraph(currentState.fullMap());
                aiPlayer.setFullMapGraph(fullMapGraph);
            }

            logger.info("My turn! Deciding move...");
            aiPlayer.updateKnowledgeBase(currentState.fullMap());

            EMove move = aiPlayer.getNextMove();
            gameSession.sendMove(move).block();
        }

        logger.info("Game Over. Client shutting down.");
    }

    private void updateModels(GameState gameState) {
        if (gameState == null)
            throw new IllegalArgumentException("gameState is null");

        playerModel.updateMyPlayerState(gameState.myPlayer());
        gameState.getOptionalEnemyPlayer().ifPresent(playerModel::updateEnemyPlayerState);
        mapModel.updateFullMap(gameState.fullMap());
    }

    private HalfMap generateHalfMap() {
        long startTime = System.nanoTime();
        logger.debug("Generating half-map...");

        for (int attempt = 0; attempt < HALF_MAP_GENERATION_ATTEMPTS; ++attempt) {
            HalfMap halfMap = halfMapGenerator.generateHalfMap();
            Notification notification = halfMapValidator.validate(halfMap);

            if (!notification.hasErrors()) {
                logger.debug("Generated a valid map in {} attempts.", attempt);
                mapModel.updateHalfMap(halfMap);

                long endTime = System.nanoTime();
                double duration = (endTime - startTime) / 1_000_000_000.;
                logger.debug("Half-map generation completed in {}s.", duration);

                return halfMap;
            }
            mapModel.updateHalfMapValidationErrors(notification.getErrors());
        }

        logger.error("Failed to generate a valid map after {} attempts!", HALF_MAP_GENERATION_ATTEMPTS);
        throw new HalfMapGenerationException("Map generation failed. Check rules.");
    }
}