package client.data.fromserver;

import client.data.PlayerInformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public record PlayerState(PlayerInformation playerInformation, boolean hasCollectedTreasure, EPlayerGameState state) {
    private static final Logger logger = LoggerFactory.getLogger(PlayerState.class);

    public PlayerState {
        Objects.requireNonNull(playerInformation, "playerInformation must not be null");
        Objects.requireNonNull(state, "state must not be null");
    }

    public boolean mustWait() {
        return state == EPlayerGameState.MustWait;
    }

    public boolean mustAct() {
        return state == EPlayerGameState.MustAct;
    }

    public boolean won() {
        return state == EPlayerGameState.Won;
    }

    public boolean lost() {
        return state == EPlayerGameState.Lost;
    }
}
