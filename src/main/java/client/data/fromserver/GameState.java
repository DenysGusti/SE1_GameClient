package client.data.fromserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;

public record GameState(String gameStateID, FullMap fullMap, PlayerState myPlayer, PlayerState enemyPlayer) {
    private static final Logger logger = LoggerFactory.getLogger(GameState.class);

    public GameState {
        Objects.requireNonNull(gameStateID, "uniqueGameID must not be null");
        Objects.requireNonNull(fullMap, "fullMap must not be null");
        Objects.requireNonNull(myPlayer, "myPlayer must not be null");
    }

    public GameState withFullMap(FullMap fullMap) {
        Objects.requireNonNull(fullMap, "fullMap must not be null");
        return new GameState(gameStateID, fullMap, myPlayer, enemyPlayer);
    }

    public Optional<PlayerState> getOptionalEnemyPlayer() {
        return Optional.ofNullable(enemyPlayer);
    }

    public boolean myPlayerMustWait() {
        return myPlayer.mustWait();
    }

    public boolean myPlayerMustAct() {
        return myPlayer.mustAct();
    }

    public boolean myPlayerWon() {
        return myPlayer.won();
    }

    public boolean myPlayerLost() {
        return myPlayer.lost();
    }

    public boolean myPlayerHasCollectedTreasure() {
        return myPlayer.hasCollectedTreasure();
    }
}
