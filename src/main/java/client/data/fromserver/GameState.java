package client.data.fromserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public record GameState(String gameStateID, FullMap fullMap, PlayerState myPlayer, PlayerState enemyPlayer) {
    private static final Logger logger = LoggerFactory.getLogger(GameState.class);

    public GameState {
        if (gameStateID == null)
            throw new IllegalArgumentException("gameStateID is null");
        if (fullMap == null)
            throw new IllegalArgumentException("fullMap is null");
        if (myPlayer == null)
            throw new IllegalArgumentException("myPlayer is null");
    }

    public GameState withFullMap(FullMap fullMap) {
        if (fullMap == null)
            throw new IllegalArgumentException("fullMap is null");

        return new GameState(gameStateID, fullMap, myPlayer, enemyPlayer);
    }

    public Optional<PlayerState> getOptionalEnemyPlayer() {
        return Optional.ofNullable(enemyPlayer);
    }

    public boolean myPlayerMustNotWait() {
        return !myPlayer.mustWait();
    }

    public boolean myPlayerMustAct() {
        return myPlayer.mustAct();
    }

    public boolean myPlayerWonOrLost() {
        return myPlayer.won() || myPlayer.lost();
    }

    public EPlayerGameState myPlayerGameState() {
        return myPlayer.gameState();
    }

    public boolean myPlayerHasCollectedTreasure() {
        return myPlayer.hasCollectedTreasure();
    }
}
