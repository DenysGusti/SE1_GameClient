package client.network.fromserver;

import client.data.UniquePlayerIdentifier;
import client.data.PlayerInformation;

import client.data.fromserver.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

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
        if (fullMapConverter == null)
            throw new IllegalArgumentException("fullMapConverter is null");

        this.fullMapConverter = fullMapConverter;
    }

    public UniquePlayerIdentifier convertPlayerID(messagesbase.UniquePlayerIdentifier uniquePlayerIdentifier) {
        if (uniquePlayerIdentifier == null)
            throw new IllegalArgumentException("uniquePlayerIdentifier is null");

        return new UniquePlayerIdentifier(uniquePlayerIdentifier.getUniquePlayerID());
    }

    public GameState convertGameState(UniquePlayerIdentifier uniquePlayerIdentifier, messagesbase.messagesfromserver.GameState gameState) {
        if (uniquePlayerIdentifier == null)
            throw new IllegalArgumentException("uniquePlayerIdentifier is null");
        if (gameState == null)
            throw new IllegalArgumentException("gameState is null");

        PlayerState myPlayerState = gameState.getPlayers().stream()
                .filter(player -> player.equals(messagesbase.UniquePlayerIdentifier.of(uniquePlayerIdentifier.uniquePlayerID())))
                .findFirst().map(FromServerConverter::convertPlayerState).orElseThrow();  // my player must always be present

        PlayerState enemyPlayerState = gameState.getPlayers().stream()
                .filter(player -> !player.equals(messagesbase.UniquePlayerIdentifier.of(uniquePlayerIdentifier.uniquePlayerID())))
                .findFirst().map(FromServerConverter::convertPlayerState).orElse(null);

        return new GameState(
                gameState.getGameStateId(),
                fullMapConverter.convertFullMap(gameState.getMap(), myPlayerState.hasCollectedTreasure()),
                myPlayerState,
                enemyPlayerState
        );
    }

    private static PlayerState convertPlayerState(messagesbase.messagesfromserver.PlayerState playerState) {
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