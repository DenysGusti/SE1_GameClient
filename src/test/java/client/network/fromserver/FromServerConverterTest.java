package client.network.fromserver;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import client.data.UniquePlayerIdentifier;
import client.data.fromserver.FullMap;
import messagesbase.messagesfromserver.EPlayerGameState;
import messagesbase.messagesfromserver.GameState;
import messagesbase.messagesfromserver.PlayerState;

public class FromServerConverterTest {
    private FromServerConverter converter;
    private FullMapConverter fullMapConverterMock;
    private UniquePlayerIdentifier myPlayerId;
    private messagesbase.UniquePlayerIdentifier serverMyId;
    private FullMap internalMapMock;

    @BeforeEach
    public void setUp() {
        fullMapConverterMock = mock(FullMapConverter.class);
        internalMapMock = mock(FullMap.class);

        when(fullMapConverterMock.convertFullMap(any(), anyBoolean())).thenReturn(internalMapMock);

        converter = new FromServerConverter(fullMapConverterMock);
        myPlayerId = new UniquePlayerIdentifier("my-id");
        serverMyId = messagesbase.UniquePlayerIdentifier.of("my-id");
    }

    private GameState stubGameState(Set<PlayerState> players, String id) {
        var serverGameState = mock(GameState.class);
        when(serverGameState.getPlayers()).thenReturn(players);
        when(serverGameState.getGameStateId()).thenReturn(id);
        return serverGameState;
    }

    @Test
    public void ValidServerPlayerId_ConvertPlayerID_ReturnsInternalId() {
        var result = converter.convertPlayerID(serverMyId);
        assertThat(result.uniquePlayerID(), is("my-id"));
    }

    @Test
    public void TwoPlayersPresent_ConvertGameState_CorrectlyPartitionsPlayers() {
        var serverEnemyId = messagesbase.UniquePlayerIdentifier.of("enemy-id");
        var myState = new PlayerState("MyFirst", "MyLast", "myUacc", EPlayerGameState.MustAct, serverMyId, true);
        var enemyState = new PlayerState("EnemyFirst", "EnemyLast", "enemyUacc", EPlayerGameState.MustWait, serverEnemyId, false);

        var serverGameState = stubGameState(Set.of(myState, enemyState), "gs-123");

        when(fullMapConverterMock.convertFullMap(eq(serverGameState.getMap()), eq(true))).thenReturn(internalMapMock);

        var result = converter.convertGameState(myPlayerId, serverGameState);

        assertThat(result.gameStateID(), is("gs-123"));
        assertThat(result.myPlayer().hasCollectedTreasure(), is(true));
        assertThat(result.enemyPlayer(), notNullValue());
        assertThat(result.enemyPlayer().playerInformation().firstName(), is("EnemyFirst"));
    }

    @Test
    public void OnlyMyPlayerPresent_ConvertGameState_EnemyStateIsNull() {
        var myState = new PlayerState("MyFirst", "MyLast", "myUacc", EPlayerGameState.MustAct, serverMyId, false);
        var serverGameState = stubGameState(Set.of(myState), "gs-only-me");

        var result = converter.convertGameState(myPlayerId, serverGameState);

        assertThat(result.myPlayer(), notNullValue());
        assertThat(result.enemyPlayer(), nullValue());
        assertThat(result.fullMap(), is(internalMapMock));
    }

    @ParameterizedTest
    @EnumSource(messagesbase.messagesfromserver.EPlayerGameState.class)
    public void AllGameStates_ConvertGameState_MapsEnumsCorrectly(EPlayerGameState serverEnum) {
        var myState = new PlayerState("MyFirst", "MyLast", "myUacc", serverEnum, serverMyId, false);
        var serverGameState = stubGameState(Set.of(myState), "gs-enum-test");

        var result = converter.convertGameState(myPlayerId, serverGameState);

        assertThat(result.myPlayer().gameState().name(), is(serverEnum.name()));
    }

    @Test
    public void NullConstructor_New_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new FromServerConverter(null));
    }

    @Test
    public void NullPlayerID_ConvertPlayerID_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> converter.convertPlayerID(null));
    }

    @Test
    public void NullPlayerID_ConvertGameState_ThrowsIllegalArgumentException() {
        var serverGameState = mock(GameState.class);
        assertThrows(IllegalArgumentException.class, () -> converter.convertGameState(null, serverGameState));
    }

    @Test
    public void NullGameState_ConvertGameState_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> converter.convertGameState(myPlayerId, null));
    }
}