package client.data.fromserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public record GameState(String ID, PlayerState myPlayer, PlayerState enemyPlayer, FullMap fullMap) {
    private static final Logger logger = LoggerFactory.getLogger(GameState.class);

    public GameState {
        Objects.requireNonNull(ID, "ID must not be null");
        Objects.requireNonNull(myPlayer, "myPlayer must not be null");
        Objects.requireNonNull(enemyPlayer, "enemyPlayer must not be null");
        Objects.requireNonNull(fullMap, "fullMap must not be null");
    }
}
