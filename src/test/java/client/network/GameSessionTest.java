package client.network;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import client.data.PlayerInformation;
import client.data.UniquePlayerIdentifier;
import client.data.fromclient.EMove;
import client.data.fromclient.HalfMap;
import client.data.fromserver.FullMap;
import client.data.fromserver.GameState;
import client.network.accumulator.FullMapAccumulator;
import reactor.core.publisher.Mono;

public class GameSessionTest {
    private GameSession gameSession;
    private NetworkService networkServiceMock;
    private FullMapAccumulator accumulatorMock;

    private final UniquePlayerIdentifier myId = new UniquePlayerIdentifier("player-1");

    @BeforeEach
    public void setUp() {
        networkServiceMock = mock(NetworkService.class);
        accumulatorMock = mock(FullMapAccumulator.class);
        gameSession = new GameSession(networkServiceMock, accumulatorMock);
    }

    @Test
    public void Constructor_NullNetworkService_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new GameSession(null, accumulatorMock));
    }

    @Test
    public void Constructor_NullFullMapAccumulator_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new GameSession(networkServiceMock, null));
    }

    @Test
    public void RegisterPlayer_Successful_SetsInternalState() {
        var playerInformation = mock(PlayerInformation.class);
        when(networkServiceMock.registerPlayer(playerInformation)).thenReturn(Mono.just(myId));

        gameSession.registerPlayer(playerInformation).block();

        when(networkServiceMock.sendMove(eq(myId), any())).thenReturn(Mono.empty());
        gameSession.sendMove(EMove.Up).block();
        verify(networkServiceMock).sendMove(eq(myId), any());
    }

    @Test
    public void RegisterPlayer_NullInput_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> gameSession.registerPlayer(null).block());
    }

    @Test
    public void PollForNewGameState_BeforeRegistration_ThrowsIllegalStateException() {
        assertThrows(IllegalStateException.class, () -> gameSession.pollForNewGameState().blockFirst());
    }

    @Test
    public void SendHalfMap_BeforeRegistration_ThrowsIllegalStateException() {
        assertThrows(IllegalStateException.class, () -> gameSession.sendHalfMap(mock(HalfMap.class)).block());
    }

    @Test
    public void SendMove_BeforeRegistration_ThrowsIllegalStateException() {
        assertThrows(IllegalStateException.class, () -> gameSession.sendMove(EMove.Up).block());
    }

    @Test
    public void PollForNewGameState_FiltersDuplicatesAndInjectsMap() {
        when(networkServiceMock.registerPlayer(any())).thenReturn(Mono.just(myId));
        gameSession.registerPlayer(mock(PlayerInformation.class)).block();

        var state1 = mock(GameState.class);
        when(state1.gameStateID()).thenReturn("ID_A");
        when(state1.fullMap()).thenReturn(mock(FullMap.class));

        var state2 = mock(GameState.class);
        when(state2.gameStateID()).thenReturn("ID_A");

        var state3 = mock(GameState.class);
        when(state3.gameStateID()).thenReturn("ID_B");
        when(state3.fullMap()).thenReturn(mock(FullMap.class));

        when(networkServiceMock.receiveGameState(myId))
                .thenReturn(Mono.just(state1))
                .thenReturn(Mono.just(state2))
                .thenReturn(Mono.just(state3));

        var combinedMap = mock(FullMap.class);
        when(accumulatorMock.getFullMap()).thenReturn(combinedMap);
        when(state1.withFullMap(any())).thenReturn(state1);
        when(state3.withFullMap(any())).thenReturn(state3);

        List<GameState> results = Objects.requireNonNull(gameSession.pollForNewGameState().take(2).collectList().block());

        assertThat(results.size(), is(2));
        assertThat(results.getFirst().gameStateID(), is("ID_A"));
        assertThat(results.getLast().gameStateID(), is("ID_B"));

        verify(accumulatorMock, times(2)).accumulateFullMap(any());
        verify(state1, times(1)).withFullMap(combinedMap);
    }

    @Test
    public void SendHalfMap_Registered_CallsNetworkService() {
        when(networkServiceMock.registerPlayer(any())).thenReturn(Mono.just(myId));
        gameSession.registerPlayer(mock(PlayerInformation.class)).block();

        var halfMap = mock(HalfMap.class);
        when(networkServiceMock.sendHalfMap(myId, halfMap)).thenReturn(Mono.empty());

        gameSession.sendHalfMap(halfMap).block();
        verify(networkServiceMock).sendHalfMap(myId, halfMap);
    }

    @Test
    public void SendMove_Registered_CallsNetworkService() {
        when(networkServiceMock.registerPlayer(any())).thenReturn(Mono.just(myId));
        gameSession.registerPlayer(mock(PlayerInformation.class)).block();

        when(networkServiceMock.sendMove(myId, EMove.Up)).thenReturn(Mono.empty());

        gameSession.sendMove(EMove.Up).block();
        verify(networkServiceMock).sendMove(myId, EMove.Up);
    }

    @Test
    public void PollForNewGameState_ServerReturnsNull_ThrowsException() {
        when(networkServiceMock.registerPlayer(any())).thenReturn(Mono.just(myId));
        gameSession.registerPlayer(mock(PlayerInformation.class)).block();

        when(networkServiceMock.receiveGameState(myId)).thenReturn(Mono.just(new Object()).map(o -> null));

        assertThrows(Exception.class, () -> gameSession.pollForNewGameState().blockFirst());
    }
}