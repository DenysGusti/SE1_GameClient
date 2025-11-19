package client.modelviewcontroller.model;

import client.data.fromserver.EPlayerGameState;
import client.data.fromserver.PlayerState;
import client.modelviewcontroller.observer.Publisher;
import client.modelviewcontroller.observer.Subscriber;

public class PlayerModel {
    private final Publisher<PlayerState> onMyPlayerStateUpdated = new Publisher<>();
    private final Publisher<PlayerState> onEnemyPlayerStateUpdated = new Publisher<>();
    private final Publisher<EPlayerGameState> onGameEnded = new Publisher<>();

    public void subscribeOnMyPlayerStateUpdated(Subscriber<PlayerState> view) {
        onMyPlayerStateUpdated.subscribe(view);
    }

    public void updateMyPlayerState(PlayerState playerState) {
        onMyPlayerStateUpdated.notify(playerState);
    }

    public void subscribeOnEnemyPlayerStateUpdated(Subscriber<PlayerState> view) {
        onEnemyPlayerStateUpdated.subscribe(view);
    }


    public void updateEnemyPlayerState(PlayerState playerState) {
        onEnemyPlayerStateUpdated.notify(playerState);
    }

    public void subscribeOnGameEnded(Subscriber<EPlayerGameState> view) {
        onGameEnded.subscribe(view);
    }

    public void updateGameEnd(EPlayerGameState playerGameState) {
        onGameEnded.notify(playerGameState);
    }
}
