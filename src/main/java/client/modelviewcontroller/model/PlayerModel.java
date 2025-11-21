package client.modelviewcontroller.model;

import client.data.fromserver.EPlayerGameState;
import client.data.fromserver.PlayerState;
import client.modelviewcontroller.observer.Publisher;
import client.modelviewcontroller.observer.Subscriber;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerModel {
    private static final Logger logger = LoggerFactory.getLogger(PlayerModel.class);

    private final Publisher<PlayerState> onMyPlayerStateUpdated = new Publisher<>();
    private final Publisher<PlayerState> onEnemyPlayerStateUpdated = new Publisher<>();
    private final Publisher<EPlayerGameState> onGameEnded = new Publisher<>();

    public void subscribeOnMyPlayerStateUpdated(Subscriber<PlayerState> view) {
        if (view == null)
            throw new IllegalArgumentException("view must not be null");

        onMyPlayerStateUpdated.subscribe(view);
    }

    public void updateMyPlayerState(PlayerState playerState) {
        if (playerState == null)
            throw new IllegalArgumentException("playerState must not be null");

        onMyPlayerStateUpdated.notify(playerState);
    }

    public void subscribeOnEnemyPlayerStateUpdated(Subscriber<PlayerState> view) {
        if (view == null)
            throw new IllegalArgumentException("view must not be null");

        onEnemyPlayerStateUpdated.subscribe(view);
    }


    public void updateEnemyPlayerState(PlayerState playerState) {
        if (playerState == null)
            throw new IllegalArgumentException("playerState must not be null");

        onEnemyPlayerStateUpdated.notify(playerState);
    }

    public void subscribeOnGameEnded(Subscriber<EPlayerGameState> view) {
        if (view == null)
            throw new IllegalArgumentException("view must not be null");

        onGameEnded.subscribe(view);
    }

    public void updateGameEnd(EPlayerGameState playerGameState) {
        if (playerGameState == null)
            throw new IllegalArgumentException("playerGameState must not be null");

        onGameEnded.notify(playerGameState);
    }
}
