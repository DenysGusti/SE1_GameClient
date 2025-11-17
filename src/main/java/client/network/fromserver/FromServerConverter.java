package client.network.fromserver;

import client.data.UniquePlayerIdentifier;
import client.data.PlayerInformation;

import client.data.fromserver.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;

public class FromServerConverter {
    private static final Logger logger = LoggerFactory.getLogger(FromServerConverter.class);
    private static final Map<messagesbase.messagesfromserver.EPlayerGameState, EPlayerGameState>
            playerGameStateConverter =
            Map.of(
                    messagesbase.messagesfromserver.EPlayerGameState.MustWait, EPlayerGameState.MustWait,
                    messagesbase.messagesfromserver.EPlayerGameState.MustAct, EPlayerGameState.MustAct,
                    messagesbase.messagesfromserver.EPlayerGameState.Won, EPlayerGameState.Won,
                    messagesbase.messagesfromserver.EPlayerGameState.Lost, EPlayerGameState.Lost
            );

    private final FullMapConverter fullMapConverter;

    public FromServerConverter(FullMapConverter fullMapConverter) {
        this.fullMapConverter = Objects.requireNonNull(fullMapConverter, "fullMapConverter must not be null");
    }

    public UniquePlayerIdentifier convertPlayerID(messagesbase.UniquePlayerIdentifier uniquePlayerIdentifier) {
        return new UniquePlayerIdentifier(uniquePlayerIdentifier.getUniquePlayerID());
    }

    public GameState convertGameState(UniquePlayerIdentifier uniquePlayerIdentifier, messagesbase.messagesfromserver.GameState gameState) {
        Objects.requireNonNull(uniquePlayerIdentifier, "uniquePlayerIdentifier must not be null");
        Objects.requireNonNull(gameState, "gameState must not be null");

        PlayerState myPlayerState = gameState.getPlayers().stream()
                .filter(player -> player.equals(messagesbase.UniquePlayerIdentifier.of(uniquePlayerIdentifier.uniquePlayerID())))
                .findFirst().map(this::convertPlayerState).orElseThrow();  // my player must always be present

        PlayerState enemyPlayerState = gameState.getPlayers().stream()
                .filter(player -> !player.equals(messagesbase.UniquePlayerIdentifier.of(uniquePlayerIdentifier.uniquePlayerID())))
                .findFirst().map(this::convertPlayerState).orElse(null);

        return new GameState(
                gameState.getGameStateId(),
                fullMapConverter.convertFullMap(gameState.getMap()),
                myPlayerState,
                enemyPlayerState
        );
    }

    private PlayerState convertPlayerState(messagesbase.messagesfromserver.PlayerState playerState) {
        Objects.requireNonNull(playerState, "playerState must not be null");

        var playerInformation = new PlayerInformation(
                playerState.getFirstName(),
                playerState.getLastName(),
                playerState.getUAccount()
        );

        return new PlayerState(
                playerInformation,
                playerState.hasCollectedTreasure(),
                playerGameStateConverter.get(playerState.getState())
        );
    }
}
