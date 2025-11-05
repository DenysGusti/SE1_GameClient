package client.data.fromserver;

import client.data.PlayerInformation;

import java.util.Objects;

public record PlayerState(PlayerInformation playerInformation, boolean hasCollectedTreasure, EPlayerGameState state) {
    public PlayerState {
        Objects.requireNonNull(playerInformation, "playerInformation must not be null");
        Objects.requireNonNull(state, "state must not be null");
    }
}
