package client.modelviewcontroller.controller;

import client.data.PlayerInformation;
import client.data.UniquePlayerIdentifier;
import client.data.fromclient.EMove;
import client.data.fromclient.HalfMap;
import client.data.fromserver.GameState;
import client.halfmaplogic.generation.HalfMapGenerator;
import client.halfmaplogic.validation.HalfMapValidator;
import client.halfmaplogic.validation.exception.HalfMapGenerationException;
import client.modelviewcontroller.model.GameModel;
import client.network.NetworkService;
import client.modelviewcontroller.controller.accumulator.FullMapAccumulator;
import client.halfmaplogic.validation.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;

public class GameController {
    private static final Logger logger = LoggerFactory.getLogger(GameController.class);
    private static final long POLL_DELAY_MS = 400;

    private final GameModel gameModel;
    private final NetworkService networkService;
    private final HalfMapGenerator halfMapGenerator;
    private final HalfMapValidator halfMapValidator;
    private final FullMapAccumulator fullMapAccumulator;

    private UniquePlayerIdentifier myPlayerIdentifier = null;
    private String lastGameStateID = null;

    public GameController(GameModel gameModel, NetworkService networkService, HalfMapGenerator halfMapGenerator,
                          HalfMapValidator halfMapValidator, FullMapAccumulator fullMapAccumulator) {
        this.gameModel = Objects.requireNonNull(gameModel, "gameModel must not be null");
        this.networkService = Objects.requireNonNull(networkService, "networkService must not be null");
        this.halfMapGenerator = Objects.requireNonNull(halfMapGenerator, "halfMapGenerator must not be null");
        this.halfMapValidator = Objects.requireNonNull(halfMapValidator, "halfMapValidator must not be null");
        this.fullMapAccumulator = Objects.requireNonNull(fullMapAccumulator, "fullMapAccumulator must not be null");
    }

    public void runGame(PlayerInformation playerInformation) {
        myPlayerIdentifier = registerPlayer(playerInformation).block();
        logger.info("Player registration complete.");

        GameState currentState = pollForNewState().filter(GameState::myPlayerMustAct).next().blockOptional().orElseThrow();
        logger.debug("First active state received: {}", currentState);

        HalfMap halfMap = generateHalfMap();
        sendHalfMap(halfMap).block();
        logger.info("Half-map sent successfully.");

        while (true) {
            currentState = pollForNewState().filter(GameState::myPlayerMustNotWait).next().blockOptional().orElseThrow();
            updateModels(currentState);

            if (currentState.myPlayerWonOrLost()) {
                logger.info("Game has ended.");
                gameModel.updateGameEnd(currentState.myPlayerGameState());
                break;
            }

            if (currentState.myPlayerMustAct()) {
                logger.info("My turn! Deciding move...");
                EMove move = EMove.Down;
                sendMove(move).block();
            }
        }

        logger.info("Game Over. Client shutting down.");
    }

    private void updateModels(GameState gameState) {
        gameModel.updateMyPlayerState(gameState.myPlayer());
        gameState.getOptionalEnemyPlayer().ifPresent(gameModel::updateEnemyPlayerState);
        gameModel.updateFullMap(gameState.fullMap());
    }

    private Mono<UniquePlayerIdentifier> registerPlayer(PlayerInformation playerInformation) {
        Objects.requireNonNull(playerInformation, "playerInformation must not be null");
        logger.info("Registering player...");
        return networkService.registerPlayer(playerInformation);
    }

    private Flux<GameState> pollForNewState() {
        Objects.requireNonNull(myPlayerIdentifier, "myPlayerIdentifier must not be null");

        return networkService.receiveGameState(myPlayerIdentifier)
                .doOnSubscribe(subscription -> logger.debug("Polling for game state..."))
                .repeatWhen(companion -> companion.delayElements(Duration.ofMillis(POLL_DELAY_MS)))
                .filter(state -> !state.gameStateID().equals(lastGameStateID))
                // Command
                .doOnNext(gameState -> {
                    lastGameStateID = gameState.gameStateID();
                    fullMapAccumulator.accumulateFullMap(gameState.fullMap(), gameState.myPlayerHasCollectedTreasure());
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
                gameModel.updateHalfMap(halfMap);
                return halfMap;
            }
            gameModel.updateHalfMapValidationErrors(notification.getErrors());
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