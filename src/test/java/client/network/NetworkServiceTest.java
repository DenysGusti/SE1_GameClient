package client.network;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;

import client.data.PlayerInformation;
import client.data.UniquePlayerIdentifier;
import client.data.fromclient.EMove;
import client.data.fromclient.HalfMap;
import client.data.fromserver.GameState;
import client.network.exception.*;
import client.network.fromclient.FromClientConverter;
import client.network.fromserver.FromServerConverter;
import messagesbase.ResponseEnvelope;
import messagesbase.messagesfromclient.*;
import reactor.core.publisher.Mono;

public class NetworkServiceTest {
    private NetworkService networkService;
    private WebClient webClientMock;
    private FromClientConverter fromClientConverterMock;
    private FromServerConverter fromServerConverterMock;

    private WebClient.RequestBodyUriSpec requestBodyUriSpecMock;
    private WebClient.RequestHeadersSpec requestHeadersSpecMock;
    private WebClient.RequestBodySpec requestBodySpecMock;
    private WebClient.ResponseSpec responseSpecMock;

    private final UniquePlayerIdentifier myId = new UniquePlayerIdentifier("my-id");

    @BeforeEach
    public void setUp() {
        webClientMock = mock(WebClient.class);
        fromClientConverterMock = mock(FromClientConverter.class);
        fromServerConverterMock = mock(FromServerConverter.class);

        requestBodyUriSpecMock = mock(WebClient.RequestBodyUriSpec.class);
        requestHeadersSpecMock = mock(WebClient.RequestHeadersSpec.class);
        requestBodySpecMock = mock(WebClient.RequestBodySpec.class);
        responseSpecMock = mock(WebClient.ResponseSpec.class);

        when(webClientMock.method(any())).thenReturn(requestBodyUriSpecMock);
        when(requestBodyUriSpecMock.uri(any(String.class))).thenReturn(requestBodySpecMock);
        when(requestBodySpecMock.retrieve()).thenReturn(responseSpecMock);
        when(requestBodySpecMock.body(any())).thenReturn(requestHeadersSpecMock);
        when(requestHeadersSpecMock.retrieve()).thenReturn(responseSpecMock);

        networkService = new NetworkService(webClientMock, fromClientConverterMock, fromServerConverterMock);
    }

    @Test
    public void Constructor_NullWebClient_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new NetworkService(null, fromClientConverterMock, fromServerConverterMock));
    }

    @Test
    public void Constructor_NullFromClientConverter_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new NetworkService(webClientMock, null, fromServerConverterMock));
    }

    @Test
    public void Constructor_NullFromServerConverter_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new NetworkService(webClientMock, fromClientConverterMock, null));
    }

    @Test
    public void CreateNewGame_NullServerBaseURL_ThrowsError() {
        assertThrows(IllegalArgumentException.class, () -> NetworkService.createNewGame(null, false, false).block());
    }

    @Test
    public void RegisterPlayer_Successful_ReturnsId() {
        var serverId = messagesbase.UniquePlayerIdentifier.of("id");
        var envelope = new ResponseEnvelope<>(serverId);
        when(fromClientConverterMock.convertPlayerInformation(any())).thenReturn(mock(PlayerRegistration.class));
        when(responseSpecMock.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(envelope));
        when(fromServerConverterMock.convertPlayerID(any())).thenReturn(myId);

        assertThat(networkService.registerPlayer(mock(PlayerInformation.class)).block(), is(myId));
    }

    @Test
    public void RegisterPlayer_ServerError_ThrowsNetworkPlayerRegistrationException() {
        var errorEnv = new ResponseEnvelope<messagesbase.UniquePlayerIdentifier>("RegErr", "Failed");
        when(fromClientConverterMock.convertPlayerInformation(any())).thenReturn(mock(PlayerRegistration.class));
        when(responseSpecMock.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(errorEnv));

        assertThrows(NetworkPlayerRegistrationException.class, () -> networkService.registerPlayer(mock(PlayerInformation.class)).block());
    }

    @Test
    public void RegisterPlayer_NullInput_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> networkService.registerPlayer(null).block());
    }

    @Test
    public void RegisterPlayer_NullResponse_ThrowsNullPointerException() {
        when(fromClientConverterMock.convertPlayerInformation(any())).thenReturn(mock(PlayerRegistration.class));
        when(responseSpecMock.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(new Object()).map(o -> null));
        assertThrows(NullPointerException.class, () -> networkService.registerPlayer(mock(PlayerInformation.class)).block());
    }

    @Test
    public void ReceiveGameState_Successful_ReturnsState() {
        var serverGameState = mock(messagesbase.messagesfromserver.GameState.class);
        var envelope = new ResponseEnvelope<>(serverGameState);
        var internalGameState = mock(GameState.class);
        when(responseSpecMock.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(envelope));
        when(fromServerConverterMock.convertGameState(eq(myId), any())).thenReturn(internalGameState);

        assertThat(networkService.receiveGameState(myId).block(), is(internalGameState));
    }

    @Test
    public void ReceiveGameState_ServerError_ThrowsNetworkGameStateException() {
        var errorEnv = new ResponseEnvelope<messagesbase.messagesfromserver.GameState>("StateErr", "Failed");
        when(responseSpecMock.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(errorEnv));

        assertThrows(NetworkGameStateException.class, () -> networkService.receiveGameState(myId).block());
    }

    @Test
    public void ReceiveGameState_NullInput_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> networkService.receiveGameState(null).block());
    }

    @Test
    public void ReceiveGameState_NullResponse_ThrowsNullPointerException() {
        when(responseSpecMock.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(new Object()).map(o -> null));
        assertThrows(NullPointerException.class, () -> networkService.receiveGameState(myId).block());
    }

    @Test
    public void SendHalfMap_Successful_Completes() {
        var envelope = new ResponseEnvelope();
        when(fromClientConverterMock.convertHalfMap(any(), any())).thenReturn(mock(PlayerHalfMap.class));
        when(responseSpecMock.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(envelope));

        assertThat(networkService.sendHalfMap(myId, mock(HalfMap.class)).block(), is(nullValue()));
    }

    @Test
    public void SendHalfMap_ServerError_ThrowsNetworkHalfMapException() {
        var errorEnv = new ResponseEnvelope("MapErr", "Failed");
        when(fromClientConverterMock.convertHalfMap(any(), any())).thenReturn(mock(PlayerHalfMap.class));
        when(responseSpecMock.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(errorEnv));

        assertThrows(NetworkHalfMapException.class, () -> networkService.sendHalfMap(myId, mock(HalfMap.class)).block());
    }

    @Test
    public void SendHalfMap_NullId_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> networkService.sendHalfMap(null, mock(HalfMap.class)).block());
    }

    @Test
    public void SendHalfMap_NullMap_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> networkService.sendHalfMap(myId, null).block());
    }

    @Test
    public void SendMove_Successful_Completes() {
        var envelope = new ResponseEnvelope();
        when(fromClientConverterMock.convertMove(any(), any())).thenReturn(mock(PlayerMove.class));
        when(responseSpecMock.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(envelope));

        assertThat(networkService.sendMove(myId, EMove.Up).block(), nullValue());
    }

    @Test
    public void SendMove_ServerError_ThrowsNetworkMoveException() {
        var errorEnv = new ResponseEnvelope("MoveErr", "Failed");
        when(fromClientConverterMock.convertMove(any(), any())).thenReturn(mock(PlayerMove.class));
        when(responseSpecMock.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(errorEnv));

        assertThrows(NetworkMoveException.class, () -> networkService.sendMove(myId, EMove.Up).block());
    }

    @Test
    public void SendMove_NullId_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> networkService.sendMove(null, EMove.Up).block());
    }

    @Test
    public void SendMove_NullMove_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> networkService.sendMove(myId, null).block());
    }
}