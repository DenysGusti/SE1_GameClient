package client.modelviewcontroller.controller;

import client.ai.FullMapGraph;
import client.ai.FullMapService;
import client.data.ETerrain;
import client.data.PlayerInformation;
import client.data.UniquePlayerIdentifier;
import client.data.XYPair;
import client.data.fromclient.EMove;
import client.data.fromclient.HalfMap;
import client.data.fromserver.GameState;
import client.halfmaplogic.generation.HalfMapGenerator;
import client.halfmaplogic.validation.HalfMapValidator;
import client.halfmaplogic.validation.exception.HalfMapGenerationException;
import client.modelviewcontroller.model.MapModel;
import client.modelviewcontroller.model.PlayerModel;
import client.network.NetworkService;
import client.modelviewcontroller.controller.accumulator.FullMapAccumulator;
import client.halfmaplogic.validation.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;

public class GameController {
    private static final Logger logger = LoggerFactory.getLogger(GameController.class);
    private static final long POLL_DELAY_MS = 400;

    private final PlayerModel playerModel;
    private final MapModel mapModel;

    private final NetworkService networkService;
    private final HalfMapGenerator halfMapGenerator;
    private final HalfMapValidator halfMapValidator;
    private final FullMapAccumulator fullMapAccumulator;

    private UniquePlayerIdentifier myPlayerIdentifier = null;
    private String lastGameStateID = null;

    public GameController(PlayerModel playerModel, MapModel mapModel,
                          NetworkService networkService, HalfMapGenerator halfMapGenerator,
                          HalfMapValidator halfMapValidator, FullMapAccumulator fullMapAccumulator) {
        this.playerModel = Objects.requireNonNull(playerModel, "playerModel must not be null");
        this.mapModel = Objects.requireNonNull(mapModel, "mapModel must not be null");
        this.networkService = Objects.requireNonNull(networkService, "networkService must not be null");
        this.halfMapGenerator = Objects.requireNonNull(halfMapGenerator, "halfMapGenerator must not be null");
        this.halfMapValidator = Objects.requireNonNull(halfMapValidator, "halfMapValidator must not be null");
        this.fullMapAccumulator = Objects.requireNonNull(fullMapAccumulator, "fullMapAccumulator must not be null");
    }

    public void runGame(PlayerInformation playerInformation) {
        myPlayerIdentifier = registerPlayer(playerInformation).block();
        logger.info("Player registration complete.");

        GameState currentState = pollForNewState().filter(GameState::myPlayerMustAct).next().blockOptional().orElseThrow();
        updateModels(currentState);
        logger.debug("First active game state received: {}", currentState);

        HalfMap halfMap = generateHalfMap();
        sendHalfMap(halfMap).block();
        logger.info("Half-map sent successfully.");

        while (true) {
            currentState = pollForNewState().filter(GameState::myPlayerMustNotWait).next().blockOptional().orElseThrow();
            updateModels(currentState);

            if (currentState.myPlayerWonOrLost()) {
                logger.info("Game has ended.");
                playerModel.updateGameEnd(currentState.myPlayerGameState());
                break;
            }

            var fullMapGraph = new FullMapGraph(currentState.fullMap());
            var fullMapService = new FullMapService();

            GameState finalCurrentState = currentState;

            XYPair goal = currentState.myPlayer().hasCollectedTreasure() ?
                    currentState.fullMap().nodes().entrySet().stream()
                            .filter(e -> !fullMapService.isOnMySide(finalCurrentState.fullMap(), e.getKey()))
                            .filter(e -> e.getValue().terrain() == ETerrain.Grass && !e.getValue().isRevealed())
                            .map(Map.Entry::getKey)
                            .findFirst().orElseThrow() :
                    finalCurrentState.fullMap().nodes().entrySet().stream()
                            .filter(e -> fullMapService.isOnMySide(finalCurrentState.fullMap(), e.getKey()))
                            .filter(e -> e.getValue().terrain() == ETerrain.Grass && !e.getValue().isRevealed())
                            .map(Map.Entry::getKey)
                            .findFirst().orElseThrow();

            XYPair next = fullMapGraph.getAllPaths(currentState.fullMap().myPlayerPosition(), goal).getFirst().get(1);

            EMove move = switch (new XYPair(
                    next.x() - currentState.fullMap().myPlayerPosition().x(),
                    next.y() - currentState.fullMap().myPlayerPosition().y()
            )) {
                case XYPair(int dx, int dy) when dx == 0 && dy == 1 -> EMove.Down;
                case XYPair(int dx, int dy) when dx == 0 && dy == -1 -> EMove.Up;
                case XYPair(int dx, int dy) when dx == 1 && dy == 0 -> EMove.Right;
                case XYPair(int dx, int dy) when dx == -1 && dy == 0 -> EMove.Left;
                default -> throw new IllegalStateException("Next path node is not an adjacent neighbor: " + next);
            };

            if (currentState.myPlayerMustAct()) {
                logger.info("My turn! Deciding move...");
                sendMove(move).block();
            }
        }

        logger.info("Game Over. Client shutting down.");
    }

    private void updateModels(GameState gameState) {
        playerModel.updateMyPlayerState(gameState.myPlayer());
        gameState.getOptionalEnemyPlayer().ifPresent(playerModel::updateEnemyPlayerState);
        mapModel.updateFullMap(gameState.fullMap());
    }

    private Mono<UniquePlayerIdentifier> registerPlayer(PlayerInformation playerInformation) {
        Objects.requireNonNull(playerInformation, "playerInformation must not be null");
        logger.info("Registering player...");
        return networkService.registerPlayer(playerInformation);
    }

    private Flux<GameState> pollForNewState() {
        Objects.requireNonNull(myPlayerIdentifier, "myPlayerIdentifier must not be null");

        return Flux.interval(Duration.ofMillis(POLL_DELAY_MS))
                .doOnNext(subscription -> logger.debug("Polling for game state..."))
                .flatMap(tick -> networkService.receiveGameState(myPlayerIdentifier))
                .filter(state -> !state.gameStateID().equals(lastGameStateID))
                // Command
                .doOnNext(gameState -> {
                    lastGameStateID = gameState.gameStateID();
                    fullMapAccumulator.accumulateFullMap(gameState.fullMap());
                })
                // Query
                .map(gameState -> gameState.withFullMap(fullMapAccumulator.getFullMap()));
    }

    private HalfMap generateHalfMap() {
        for (int attempt = 0; attempt < 100; ++attempt) {
            HalfMap halfMap = halfMapGenerator.generateHalfMap();
            Notification notification = halfMapValidator.validate(halfMap);

            if (!notification.hasErrors()) {
                logger.info("Generated a valid map in {} attempts.", attempt);
                mapModel.updateHalfMap(halfMap);
                return halfMap;
            }
            mapModel.updateHalfMapValidationErrors(notification.getErrors());
        }

        logger.error("Failed to generate a valid map after 100 attempts!");
        throw new HalfMapGenerationException("Map generation failed. Check rules.");
    }

    private Mono<Void> sendHalfMap(HalfMap halfMap) {
        Objects.requireNonNull(myPlayerIdentifier, "myPlayerIdentifier must not be null");
        Objects.requireNonNull(halfMap, "halfMap must not be null");
        return networkService.sendHalfMap(myPlayerIdentifier, halfMap);
    }

    private Mono<Void> sendMove(EMove move) {
        Objects.requireNonNull(myPlayerIdentifier, "myPlayerIdentifier must not be null");
        Objects.requireNonNull(move, "move must not be null");
        return networkService.sendMove(myPlayerIdentifier, move);
    }
}