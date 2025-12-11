package client.data.fromserver;

import client.data.PlayerInformation;

public record PlayerState(PlayerInformation playerInformation, boolean hasCollectedTreasure,
                          EPlayerGameState gameState) {
    public PlayerState {
        if (playerInformation == null)
            throw new IllegalArgumentException("playerInformation is null");
        if (gameState == null)
            throw new IllegalArgumentException("gameState is null");
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
