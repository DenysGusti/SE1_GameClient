package client.network.fromserver;

import client.data.fromserver.EPlayerGameState;
import client.data.fromserver.FullMap;
import client.data.fromserver.GameState;
import messagesbase.UniquePlayerIdentifier;
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
    private FullMap fullMapMock; // The object our mock converter will return

    @InjectMocks
    private FromServerConverter fromServerConverter;

    private final String myPlayerID = "my-player-id";
    private final String enemyPlayerID = "enemy-player-id";
    private messagesbase.messagesfromserver.GameState serverGameState;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        var myPlayer = new messagesbase.messagesfromserver.PlayerState(
                "My", "Player", "myuser",
                messagesbase.messagesfromserver.EPlayerGameState.MustAct,
                UniquePlayerIdentifier.of(myPlayerID), false);

        var enemyPlayer = new messagesbase.messagesfromserver.PlayerState(
                "Enemy", "Player", "enemyuser",
                messagesbase.messagesfromserver.EPlayerGameState.MustWait,
                UniquePlayerIdentifier.of(enemyPlayerID), true);

        var serverFullMap = new messagesbase.messagesfromserver.FullMap();

        serverGameState = new messagesbase.messagesfromserver.GameState(
                serverFullMap, List.of(myPlayer, enemyPlayer), "game-id-123");

        when(fullMapConverterMock.convertFullMap(any(messagesbase.messagesfromserver.FullMap.class)))
                .thenReturn(fullMapMock);
    }

    @Test
    @DisplayName("Should convert server GameState to client GameState")
    void convertGameState() {
        GameState clientState = fromServerConverter.convertGameState(myPlayerID, serverGameState);

        assertAll(
                () -> assertThat(clientState.ID(), is("game-id-123")),
                () -> assertThat(clientState.fullMap(), is(fullMapMock)),
                () -> assertThat(clientState.myPlayer(), is(notNullValue())),
                () -> assertThat(clientState.getOptionalEnemyPlayer().isPresent(), is(true))
        );
    }

    @Test
    @DisplayName("Should correctly convert my player state")
    void convertGameState_myPlayerState() {
        var myPlayer = fromServerConverter.convertGameState(myPlayerID, serverGameState).myPlayer();
        assertAll(
                () -> assertThat(myPlayer.playerInformation().firstName(), is("My")),
                () -> assertThat(myPlayer.state(), is(EPlayerGameState.MustAct)),
                () -> assertThat(myPlayer.hasCollectedTreasure(), is(false))
        );
    }

    @Test
    @DisplayName("Should correctly convert enemy player state")
    void convertGameState_enemyPlayerState() {
        var enemyPlayer = fromServerConverter.convertGameState(myPlayerID, serverGameState)
                .getOptionalEnemyPlayer().orElseThrow();
        assertAll(
                () -> assertThat(enemyPlayer.playerInformation().firstName(), is("Enemy")),
                () -> assertThat(enemyPlayer.state(), is(EPlayerGameState.MustWait)),
                () -> assertThat(enemyPlayer.hasCollectedTreasure(), is(true))
        );
    }

    @Test
    @DisplayName("Should throw exception if my player is not in the list")
    void convertGameState_myPlayerMissing_shouldThrow() {
        messagesbase.messagesfromserver.GameState emptyState = new messagesbase.messagesfromserver.GameState(
                new messagesbase.messagesfromserver.FullMap(), List.of(), "game-id-123");

        assertThrows(NoSuchElementException.class, () -> fromServerConverter.convertGameState(myPlayerID, emptyState));
    }
}