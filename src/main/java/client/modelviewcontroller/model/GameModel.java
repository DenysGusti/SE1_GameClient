package client.modelviewcontroller.model;

import client.data.fromclient.HalfMap;
import client.data.fromserver.EPlayerGameState;
import client.data.fromserver.FullMap;
import client.data.fromserver.PlayerState;
import client.modelviewcontroller.observer.Publisher;
import client.modelviewcontroller.observer.Subscriber;
import client.halfmaplogic.validation.exception.HalfMapGenerationException;

import java.util.Collection;

public class GameModel {
    private final Publisher<FullMap> onFullMapUpdated = new Publisher<>();
    private final Publisher<Collection<HalfMapGenerationException>> onHalfMapValidationErrors = new Publisher<>();
    private final Publisher<HalfMap> onHalfMapGenerated = new Publisher<>();

    private final Publisher<PlayerState> onMyPlayerStateUpdated = new Publisher<>();
    private final Publisher<PlayerState> onEnemyPlayerStateUpdated = new Publisher<>();

    private final Publisher<EPlayerGameState> onGameEnded = new Publisher<>();

    public void subscribeOnFullMapUpdated(Subscriber<FullMap> view) {
        onFullMapUpdated.subscribe(view);
    }

    public void updateFullMap(FullMap fullMap) {
        onFullMapUpdated.notify(fullMap);
    }

    public void subscribeOnHalfMapValidationErrors(Subscriber<Collection<HalfMapGenerationException>> view) {
        onHalfMapValidationErrors.subscribe(view);
    }

    public void updateHalfMapValidationErrors(Collection<HalfMapGenerationException> errors) {
        onHalfMapValidationErrors.notify(errors);
    }

    public void subscribeOnHalfMapGenerated(Subscriber<HalfMap> view) {
        onHalfMapGenerated.subscribe(view);
    }

    public void updateHalfMap(HalfMap halfMap) {
        onHalfMapGenerated.notify(halfMap);
    }

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