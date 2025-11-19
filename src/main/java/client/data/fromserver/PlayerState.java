package client.data.fromserver;

import client.data.PlayerInformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public record PlayerState(PlayerInformation playerInformation, boolean hasCollectedTreasure,
                          EPlayerGameState gameState) {
    private static final Logger logger = LoggerFactory.getLogger(PlayerState.class);

    public PlayerState {
        Objects.requireNonNull(playerInformation, "playerInformation must not be null");
        Objects.requireNonNull(gameState, "gameState must not be null");
    }

    public boolean mustWait() {
        return gameState == EPlayerGameState.MustWait;
    }

    public boolean mustAct() {
        return gameState == EPlayerGameState.MustAct;
    }

    public boolean won() {
        return gameState == EPlayerGameState.Won;
    }

    public boolean lost() {
        return gameState == EPlayerGameState.Lost;
    }
}
