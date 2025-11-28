package client.modelviewcontroller.controller;

import client.ai.*;
import client.ai.tsp.*;
import client.data.PlayerInformation;
import client.data.UniquePlayerIdentifier;
import client.data.XYPair;
import client.data.fromclient.EMove;
import client.data.fromclient.HalfMap;
import client.data.fromserver.FullMap;
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
        if (playerModel == null)
            throw new IllegalArgumentException("playerModel must not be null");
        if (mapModel == null)
            throw new IllegalArgumentException("mapModel must not be null");
        if (networkService == null)
            throw new IllegalArgumentException("networkService must not be null");
        if (halfMapGenerator == null)
            throw new IllegalArgumentException("halfMapGenerator must not be null");
        if (halfMapValidator == null)
            throw new IllegalArgumentException("halfMapValidator must not be null");
        if (fullMapAccumulator == null)
            throw new IllegalArgumentException("fullMapAccumulator must not be null");

        this.playerModel = playerModel;
        this.mapModel = mapModel;
        this.networkService = networkService;
        this.halfMapGenerator = halfMapGenerator;
        this.halfMapValidator = halfMapValidator;
        this.fullMapAccumulator = fullMapAccumulator;
    }

    public void runGame(PlayerInformation playerInformation) {
        if (playerInformation == null)
            throw new IllegalArgumentException("playerInformation must not be null");

        myPlayerIdentifier = registerPlayer(playerInformation).block();
        logger.info("Player registration complete.");

        GameState currentState = pollForNewState().filter(GameState::myPlayerMustAct).next().blockOptional().orElseThrow();
        updateModels(currentState);
        logger.trace("First active game state received: {}", currentState);

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

            var fullMapGraph = FullMapGraphFactory.createGraph(currentState.fullMap());
            NodeTraversalStrategy nearestNeighbourTraversalStrategy = new NearestNeighbourTraversalStrategy(fullMapGraph);
            NodeTraversalStrategy fallbackStrategy = new TwoOptTraversalStrategy(fullMapGraph, nearestNeighbourTraversalStrategy);
            NodeTraversalStrategy nodeTraversalStrategy = new HeldKarpTraversalStrategy(fullMapGraph, fallbackStrategy);
            var fullMapService = new FullMapService();

            FullMap fullMap = currentState.fullMap();

            XYPair goal;
            if (fullMap.isMyTreasureCollected()) {
                if (fullMap.getOptionalEnemyFortPosition().isPresent())
                    goal = fullMap.getOptionalEnemyFortPosition().get();
                else {
                    Set<XYPair> nodesToTraverse = fullMapService.getUnrevealedGrassNodesOnEnemySide(fullMap);
                    List<XYPair> bypassOrder = nodeTraversalStrategy
                            .orderNodes(fullMap.getOptionalMyPlayerPosition().orElseThrow(), nodesToTraverse);
                    List<List<XYPair>> paths = fullMapGraph.getAllPaths(bypassOrder);
                    goal = paths.getFirst().get(1);
                }
            } else {
                if (fullMap.getOptionalMyTreasurePosition().isPresent())
                    goal = fullMap.getOptionalMyTreasurePosition().get();
                else {
                    Set<XYPair> nodesToTraverse = fullMapService.getUnrevealedGrassNodesOnMySide(fullMap);
                    List<XYPair> bypassOrder = nodeTraversalStrategy
                            .orderNodes(fullMap.getOptionalMyPlayerPosition().orElseThrow(), nodesToTraverse);
                    List<List<XYPair>> paths = fullMapGraph.getAllPaths(bypassOrder);
                    goal = paths.getFirst().get(1);
                }
            }

            XYPair next = fullMapGraph.getAllPaths(currentState.fullMap().myPlayerPosition(), goal).getFirst().get(1);

            var delta = new XYPair(
                    next.x() - currentState.fullMap().myPlayerPosition().x(),
                    next.y() - currentState.fullMap().myPlayerPosition().y()
            );

            EMove move = switch (delta) {
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
        if (gameState == null)
            throw new IllegalArgumentException("gameState must not be null");

        playerModel.updateMyPlayerState(gameState.myPlayer());
        gameState.getOptionalEnemyPlayer().ifPresent(playerModel::updateEnemyPlayerState);
        mapModel.updateFullMap(gameState.fullMap());
    }

    private Mono<UniquePlayerIdentifier> registerPlayer(PlayerInformation playerInformation) {
        if (playerInformation == null)
            throw new IllegalArgumentException("playerInformation must not be null");

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
        if (halfMap == null)
            throw new IllegalArgumentException("halfMap must not be null");

        Objects.requireNonNull(myPlayerIdentifier, "myPlayerIdentifier must not be null");
        return networkService.sendHalfMap(myPlayerIdentifier, halfMap);
    }

    private Mono<Void> sendMove(EMove move) {
        if (move == null)
            throw new IllegalArgumentException("move must not be null");

        Objects.requireNonNull(myPlayerIdentifier, "myPlayerIdentifier must not be null");
        return networkService.sendMove(myPlayerIdentifier, move);
    }
}