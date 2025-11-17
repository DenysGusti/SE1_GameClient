package client.network.fromserver;

import client.data.UniquePlayerIdentifier;
import client.data.fromserver.EPlayerGameState;
import client.data.fromserver.FullMap;
import client.data.fromserver.GameState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.NoSuchElementException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class FromServerConverterTest {

    @Mock
    private FullMapConverter fullMapConverterMock;

    @Mock
    private FullMap fullMapMock;

    @InjectMocks
    private FromServerConverter fromServerConverter;

    private final UniquePlayerIdentifier myPlayer = new UniquePlayerIdentifier("my-player-id");
    private final UniquePlayerIdentifier enemyPlayer = new UniquePlayerIdentifier("enemy-player-id");
    private messagesbase.messagesfromserver.GameState serverGameState;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        var myPlayerState = new messagesbase.messagesfromserver.PlayerState(
                "My", "Player", "myuser",
                messagesbase.messagesfromserver.EPlayerGameState.MustAct,
                messagesbase.UniquePlayerIdentifier.of(myPlayer.uniquePlayerID()), false);

        var enemyPlayerState = new messagesbase.messagesfromserver.PlayerState(
                "Enemy", "Player", "enemyuser",
                messagesbase.messagesfromserver.EPlayerGameState.MustWait,
                messagesbase.UniquePlayerIdentifier.of(enemyPlayer.uniquePlayerID()), true);

        var serverFullMap = new messagesbase.messagesfromserver.FullMap();

        serverGameState = new messagesbase.messagesfromserver.GameState(
                serverFullMap, List.of(myPlayerState, enemyPlayerState), "game-id-123");

        when(fullMapConverterMock.convertFullMap(any(messagesbase.messagesfromserver.FullMap.class)))
                .thenReturn(fullMapMock);
    }

    @Test
    @DisplayName("Converts server GameState to client GameState")
    void ServerGameState_ConvertGameState_ReturnsCorrectClientGameState() {
        GameState clientState = fromServerConverter.convertGameState(myPlayer, serverGameState);

        assertAll(
                () -> assertThat(clientState.gameStateID(), is("game-id-123")),
                () -> assertThat(clientState.fullMap(), is(fullMapMock)),
                () -> assertThat(clientState.myPlayer(), is(notNullValue())),
                () -> assertThat(clientState.getOptionalEnemyPlayer().isPresent(), is(true))
        );
    }

    @Test
    @DisplayName("Converts server GameState and finds correct MyPlayerState data")
    void ServerGameState_ConvertGameState_ReturnsCorrectMyPlayerState() {
        var myPlayerState = fromServerConverter.convertGameState(myPlayer, serverGameState).myPlayer();
        assertAll(
                () -> assertThat(myPlayerState.playerInformation().firstName(), is("My")),
                () -> assertThat(myPlayerState.state(), is(EPlayerGameState.MustAct)),
                () -> assertThat(myPlayerState.hasCollectedTreasure(), is(false))
        );
    }

    @Test
    @DisplayName("Converts server GameState and finds correct EnemyPlayer data")
    void ServerGameState_ConvertGameState_ReturnsCorrectEnemyPlayerState() {
        var enemyPlayer = fromServerConverter.convertGameState(myPlayer, serverGameState)
                .getOptionalEnemyPlayer().orElseThrow();
        assertAll(
                () -> assertThat(enemyPlayer.playerInformation().firstName(), is("Enemy")),
                () -> assertThat(enemyPlayer.state(), is(EPlayerGameState.MustWait)),
                () -> assertThat(enemyPlayer.hasCollectedTreasure(), is(true))
        );
    }

    @Test
    @DisplayName("Negative Test: Throws exception if my player is not in the list")
    void ServerGameStateWithNoPlayers_ConvertGameState_ThrowsNoSuchElementException() {
        var emptyState = new messagesbase.messagesfromserver.GameState(
                new messagesbase.messagesfromserver.FullMap(), List.of(), "game-id-123");

        assertThrows(NoSuchElementException.class, () -> fromServerConverter.convertGameState(myPlayer, emptyState));
    }
}