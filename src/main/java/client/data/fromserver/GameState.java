package client.data.fromserver;

import java.util.Objects;

public record GameState(String ID, PlayerState myPlayer, PlayerState enemyPlayer, FullMap fullMap) {
    public GameState {
        Objects.requireNonNull(ID, "ID must not be null");
        Objects.requireNonNull(myPlayer, "myPlayer must not be null");
        Objects.requireNonNull(enemyPlayer, "enemyPlayer must not be null");
        Objects.requireNonNull(fullMap, "fullMap must not be null");
    }
}
