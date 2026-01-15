package client.network;

import client.data.PlayerInformation;
import client.data.UniquePlayerIdentifier;
import client.data.fromclient.EMove;
import client.data.fromclient.HalfMap;
import client.data.fromserver.GameState;
import client.network.accumulator.FullMapAccumulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;

public class GameSession {
    private static final Logger logger = LoggerFactory.getLogger(GameSession.class);
    private static final long POLL_DELAY_MS = 400;

    private final NetworkService networkService;
    private final FullMapAccumulator fullMapAccumulator;

    private UniquePlayerIdentifier playerIdentifier = null;
    private String lastGameStateID = null;

    public GameSession(NetworkService networkService, FullMapAccumulator fullMapAccumulator) {
        if (networkService == null)
            throw new IllegalArgumentException("networkService is null");
        if (fullMapAccumulator == null)
            throw new IllegalArgumentException("fullMapAccumulator is null");

        this.networkService = networkService;
        this.fullMapAccumulator = fullMapAccumulator;
    }

    public Mono<Void> registerPlayer(PlayerInformation playerInformation) {
        if (playerInformation == null)
            return Mono.error(new IllegalArgumentException("playerInformation is null"));

        return networkService.registerPlayer(playerInformation)
                .doOnNext(playerIdentifier -> {
                    this.playerIdentifier = playerIdentifier;
                    logger.info("Session established for uniquePlayerID: {}", playerIdentifier.uniquePlayerID());
                })
                .then();
    }

    public Flux<GameState> pollForNewGameState() {
        if (playerIdentifier == null)
            return Flux.error(new IllegalStateException("playerIdentifier is null"));

        return Flux.interval(Duration.ofMillis(POLL_DELAY_MS))
                .doOnNext(tick -> logger.trace("Polling for game state..."))
                .flatMap(tick -> networkService.receiveGameState(playerIdentifier))
                .filter(this::isNewGameState)
                .doOnNext(this::updateInternalState)
                .map(this::injectAccumulatedMap);
    }

    public Mono<Void> sendHalfMap(HalfMap halfMap) {
        if (playerIdentifier == null)
            return Mono.error(new IllegalStateException("playerIdentifier is null"));

        return networkService.sendHalfMap(playerIdentifier, halfMap);
    }

    public Mono<Void> sendMove(EMove move) {
        if (playerIdentifier == null)
            return Mono.error(new IllegalStateException("playerIdentifier is null"));

        return networkService.sendMove(playerIdentifier, move);
    }

    private boolean isNewGameState(GameState gameState) {
        // null-safe comparison, if lastGameStateID is null
        return !Objects.equals(lastGameStateID, gameState.gameStateID());
    }

    // Command
    private void updateInternalState(GameState gameState) {
        lastGameStateID = gameState.gameStateID();
        logger.debug("Received new GameState ID: {}", lastGameStateID);

        fullMapAccumulator.accumulateFullMap(gameState.fullMap());
    }

    // Query
    private GameState injectAccumulatedMap(GameState gameState) {
        return gameState.withFullMap(fullMapAccumulator.getFullMap());
    }
}