package client.modelviewcontroller.model;

import client.data.fromserver.EPlayerGameState;
import client.data.fromserver.PlayerState;
import client.observer.Subscriber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class PlayerModelTest {
    private PlayerModel playerModel;
    private Subscriber<PlayerState> myPlayerSubscriber;
    private Subscriber<PlayerState> enemyPlayerSubscriber;
    private Subscriber<EPlayerGameState> gameEndSubscriber;

    @BeforeEach
    public void setUp() {
        playerModel = new PlayerModel();
        myPlayerSubscriber = mock(Subscriber.class);
        enemyPlayerSubscriber = mock(Subscriber.class);
        gameEndSubscriber = mock(Subscriber.class);
    }

    @Test
    public void ValidPlayerState_UpdateMyPlayerStateCalled_SubscriberIsNotified() {
        var state = mock(PlayerState.class);
        playerModel.subscribeOnMyPlayerStateUpdated(myPlayerSubscriber);
        playerModel.updateMyPlayerState(state);

        verify(myPlayerSubscriber).update(state);
    }

    @Test
    public void ValidPlayerState_UpdateEnemyPlayerStateCalled_SubscriberIsNotified() {
        var state = mock(PlayerState.class);
        playerModel.subscribeOnEnemyPlayerStateUpdated(enemyPlayerSubscriber);
        playerModel.updateEnemyPlayerState(state);

        verify(enemyPlayerSubscriber).update(state);
    }

    @Test
    public void ValidGameState_UpdateGameEndCalled_SubscriberIsNotified() {
        var endState = EPlayerGameState.Won;
        playerModel.subscribeOnGameEnded(gameEndSubscriber);
        playerModel.updateGameEnd(endState);

        verify(gameEndSubscriber).update(endState);
    }

    @Test
    public void NullPlayerState_UpdateMyPlayerStateCalled_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> playerModel.updateMyPlayerState(null));
    }

    @Test
    public void NullSubscriber_SubscribeOnMyPlayerStateUpdatedCalled_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> playerModel.subscribeOnMyPlayerStateUpdated(null));
    }

    @Test
    public void NullPlayerState_UpdateEnemyPlayerStateCalled_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> playerModel.updateEnemyPlayerState(null));
    }

    @Test
    public void NullSubscriber_SubscribeOnEnemyPlayerStateUpdatedCalled_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> playerModel.subscribeOnEnemyPlayerStateUpdated(null));
    }

    @Test
    public void NullGameState_UpdateGameEndCalled_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> playerModel.updateGameEnd(null));
    }

    @Test
    public void NullSubscriber_SubscribeOnGameEndedCalled_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> playerModel.subscribeOnGameEnded(null));
    }
}