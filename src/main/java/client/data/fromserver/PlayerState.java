package client.data.fromserver;

import client.data.PlayerInformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record PlayerState(PlayerInformation playerInformation, boolean hasCollectedTreasure,
                          EPlayerGameState gameState) {
    private static final Logger logger = LoggerFactory.getLogger(PlayerState.class);

    public PlayerState {
        if (playerInformation == null)
            throw new IllegalArgumentException("playerInformation must not be null");
        if (gameState == null)
            throw new IllegalArgumentException("gameState must not be null");
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
