package client.network;

import client.data.*;
import client.data.UniqueGameIdentifier;
import client.data.UniquePlayerIdentifier;
import client.data.fromclient.*;
import client.data.fromclient.EMove;
import client.data.fromserver.*;

import client.network.exception.*;

import client.network.fromclient.FromClientConverter;
import client.network.fromserver.FromServerConverter;
import messagesbase.*;
import messagesbase.messagesfromclient.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

import java.util.Objects;

public class NetworkService {
    private static final Logger logger = LoggerFactory.getLogger(NetworkService.class);

    private final WebClient gameWebClient;
    private final FromClientConverter fromClientConverter;
    private final FromServerConverter fromServerConverter;

    public static Mono<UniqueGameIdentifier> createNewGame(String serverBaseURL, boolean debugMode, boolean dummyCompetition) {
        if (serverBaseURL == null)
            return Mono.error(new IllegalArgumentException("serverBaseURL is null"));

        logger.info("Attempting to create a new game, debugMode={}, dummyCompetition={}", debugMode, dummyCompetition);

        var webClient = WebClient
                .builder()
                .baseUrl(serverBaseURL + "/games")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_VALUE)
                .build();

        return webClient
                .method(HttpMethod.GET)
                .uri(uriBuilder -> uriBuilder
                        .queryParam("enableDebugMode", debugMode)
                        .queryParam("enableDummyCompetition", dummyCompetition)
                        .build()
                )
                .retrieve()
                .<messagesbase.UniqueGameIdentifier>bodyToMono(new ParameterizedTypeReference<>() {
                })
                .map(uniqueGameIdentifier -> {
                    Objects.requireNonNull(uniqueGameIdentifier, "Server response for new game creation is null.");
                    logger.info("New game created with uniqueGameID: {}", uniqueGameIdentifier.getUniqueGameID());
                    return new UniqueGameIdentifier(uniqueGameIdentifier.getUniqueGameID());
                });
    }

    public NetworkService(WebClient gameWebClient, FromClientConverter fromClientConverter,
                          FromServerConverter fromServerConverter) {
        if (gameWebClient == null)
            throw new IllegalArgumentException("gameWebClient is null");
        if (fromClientConverter == null)
            throw new IllegalArgumentException("fromClientConverter is null");
        if (fromServerConverter == null)
            throw new IllegalArgumentException("fromServerConverter is null");

        this.gameWebClient = gameWebClient;
        this.fromClientConverter = fromClientConverter;
        this.fromServerConverter = fromServerConverter;
    }

    public Mono<UniquePlayerIdentifier> registerPlayer(PlayerInformation playerInformation) {
        if (playerInformation == null)
            return Mono.error(new IllegalArgumentException("playerInformation is null"));

        PlayerRegistration playerRegistration = fromClientConverter.convertPlayerInformation(playerInformation);

        return gameWebClient
                .method(HttpMethod.POST)
                .uri("/players")
                .body(BodyInserters.fromValue(playerRegistration))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ResponseEnvelope<messagesbase.UniquePlayerIdentifier>>() {
                })
                .<messagesbase.UniquePlayerIdentifier>handle((response, sink) -> {
                    Objects.requireNonNull(response, "Server response for registering player is null.");
                    if (response.getState() == ERequestState.Error) {
                        sink.error(new NetworkPlayerRegistrationException(response.getExceptionName() + ": " + response.getExceptionMessage()));
                        return;
                    }
                    sink.next(response.getData().orElseThrow());
                })
                .map(fromServerConverter::convertPlayerID);
    }

    public Mono<GameState> receiveGameState(UniquePlayerIdentifier uniquePlayerIdentifier) {
        if (uniquePlayerIdentifier == null)
            return Mono.error(new IllegalArgumentException("uniquePlayerIdentifier is null"));

        return gameWebClient
                .method(HttpMethod.GET)
                .uri("/states/" + uniquePlayerIdentifier.uniquePlayerID())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ResponseEnvelope<messagesbase.messagesfromserver.GameState>>() {
                })
                .<messagesbase.messagesfromserver.GameState>handle((response, sink) -> {
                    Objects.requireNonNull(response, "Server response for receiving game state is null.");
                    if (response.getState() == ERequestState.Error) {
                        sink.error(new NetworkGameStateException(response.getExceptionName() + ": " + response.getExceptionMessage()));
                        return;
                    }
                    sink.next(response.getData().orElseThrow());
                })
                .map(serverGameState -> fromServerConverter.convertGameState(uniquePlayerIdentifier, serverGameState));
    }

    public Mono<Void> sendHalfMap(UniquePlayerIdentifier uniquePlayerIdentifier, HalfMap halfMap) {
        if (uniquePlayerIdentifier == null)
            return Mono.error(new IllegalArgumentException("uniquePlayerIdentifier is null"));
        if (halfMap == null)
            return Mono.error(new IllegalArgumentException("halfMap is null"));

        PlayerHalfMap playerHalfMap = fromClientConverter.convertHalfMap(uniquePlayerIdentifier, halfMap);

        return gameWebClient
                .method(HttpMethod.POST)
                .uri("/halfmaps")
                .body(BodyInserters.fromValue(playerHalfMap))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ResponseEnvelope>() {
                })
                .<ResponseEnvelope>handle((response, sink) -> {
                    Objects.requireNonNull(response, "Server response for sending half map is null.");
                    if (response.getState() == ERequestState.Error) {
                        sink.error(new NetworkHalfMapException(response.getExceptionName() + ": " + response.getExceptionMessage()));
                    }
                })
                .then();
    }

    public Mono<Void> sendMove(UniquePlayerIdentifier uniquePlayerIdentifier, EMove move) {
        if (uniquePlayerIdentifier == null)
            return Mono.error(new IllegalArgumentException("uniquePlayerIdentifier is null"));
        if (move == null)
            return Mono.error(new IllegalArgumentException("move is null"));

        PlayerMove playerMove = fromClientConverter.convertMove(uniquePlayerIdentifier, move);

        return gameWebClient
                .method(HttpMethod.POST)
                .uri("/moves")
                .body(BodyInserters.fromValue(playerMove))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ResponseEnvelope>() {
                })
                .<ResponseEnvelope>handle((response, sink) -> {
                    Objects.requireNonNull(response, "Server response for sending move is null.");
                    if (response.getState() == ERequestState.Error) {
                        sink.error(new NetworkMoveException(response.getExceptionName() + ": " + response.getExceptionMessage()));
                    }
                })
                .then();
    }
}