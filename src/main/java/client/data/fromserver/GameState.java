package client.data.fromserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;

public record GameState(String ID, FullMap fullMap, PlayerState myPlayer, PlayerState enemyPlayer) {
    private static final Logger logger = LoggerFactory.getLogger(GameState.class);

    public GameState {
        Objects.requireNonNull(ID, "ID must not be null");
        Objects.requireNonNull(fullMap, "fullMap must not be null");
    }

    public GameState withAccumulatedMap(FullMap accumulatedMap) {
        Objects.requireNonNull(accumulatedMap, "accumulatedMap must not be null");
        return new GameState(ID, accumulatedMap, myPlayer, enemyPlayer);
    }

    public Optional<PlayerState> getOptionalMyPlayer() {
        return Optional.ofNullable(myPlayer);
    }

    public Optional<PlayerState> getOptionalEnemyPlayer() {
        return Optional.ofNullable(enemyPlayer);
    }
}
