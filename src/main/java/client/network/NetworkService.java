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
import client.network.accumulator.FullMapAccumulator;
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
    private final FullMapAccumulator fullMapAccumulator;

    private UniquePlayerIdentifier myPlayer = null; // session player token

    public static Mono<UniqueGameIdentifier> createNewGame(String serverBaseURL, boolean debugMode, boolean dummyCompetition) {
        Objects.requireNonNull(serverBaseURL, "serverBaseURL must not be null");
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
                          FromServerConverter fromServerConverter, FullMapAccumulator fullMapAccumulator) {
        this.gameWebClient = Objects.requireNonNull(gameWebClient, "gameWebClient must not be null");
        this.fromClientConverter = Objects.requireNonNull(fromClientConverter, "fromClientConverter must not be null");
        this.fromServerConverter = Objects.requireNonNull(fromServerConverter, "fromServerConverter must not be null");
        this.fullMapAccumulator = Objects.requireNonNull(fullMapAccumulator, "fullMapAccumulator must not be null");
        logger.info("NetworkService initialized.");
    }

    public Mono<Void> registerPlayer(PlayerInformation playerInformation) {
        Objects.requireNonNull(playerInformation, "playerInformation must not be null");
        logger.info("Registering player...");
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
                        sink.error(new PlayerRegistrationException(response.getExceptionName() + ": " + response.getExceptionMessage()));
                        return;
                    }
                    sink.next(response.getData().orElseThrow());
                })
                .doOnNext(uniquePlayerIdentifier -> {
                            this.myPlayer = fromServerConverter.convertPlayerID(uniquePlayerIdentifier);
                            logger.info("Player registered with uniqueGameID: {}", this.myPlayer.uniquePlayerID());
                        }
                ).then();
    }

    public Mono<GameState> receiveGameState() {
        Objects.requireNonNull(myPlayer, "sessionToken must not be null");

        return gameWebClient
                .method(HttpMethod.GET)
                .uri("/states/" + myPlayer.uniquePlayerID())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ResponseEnvelope<messagesbase.messagesfromserver.GameState>>() {
                })
                .<messagesbase.messagesfromserver.GameState>handle((response, sink) -> {
                    Objects.requireNonNull(response, "Server response for receiving game state is null.");
                    if (response.getState() == ERequestState.Error) {
                        sink.error(new GameStateException(response.getExceptionName() + ": " + response.getExceptionMessage()));
                        return;
                    }
                    sink.next(response.getData().orElseThrow());
                })
                .map(serverGameState -> {
                    GameState clientGameState = fromServerConverter.convertGameState(myPlayer, serverGameState);

                    boolean hasCollectedTreasure = clientGameState.myPlayerHasCollectedTreasure();

                    this.fullMapAccumulator.accumulateFullMap(clientGameState.fullMap(), hasCollectedTreasure);

                    return clientGameState.withFullMap(this.fullMapAccumulator.getFullMap());
                });
    }

    public Mono<Void> sendHalfMap(HalfMap halfMap) {
        Objects.requireNonNull(halfMap, "halfMap must not be null");
        Objects.requireNonNull(myPlayer, "sessionToken must not be null");
        logger.info("Sending map to server...");

        PlayerHalfMap playerHalfMap = this.fromClientConverter.convertHalfMap(myPlayer, halfMap);

        return this.gameWebClient
                .method(HttpMethod.POST)
                .uri("/halfmaps")
                .body(BodyInserters.fromValue(playerHalfMap))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ResponseEnvelope>() {
                })
                .<ResponseEnvelope>handle((response, sink) -> {
                    Objects.requireNonNull(response, "Server response for sending half map is null.");
                    if (response.getState() == ERequestState.Error) {
                        sink.error(new HalfMapException(response.getExceptionName() + ": " + response.getExceptionMessage()));
                        return;
                    }
                    logger.info("Map sent successfully.");
                })
                .then();
    }

    public Mono<Void> sendMove(EMove move) {
        Objects.requireNonNull(move, "move must not be null");
        Objects.requireNonNull(myPlayer, "sessionToken must not be null, register player first.");
        logger.info("Sending move: {}", move);

        PlayerMove playerMove = this.fromClientConverter.convertMove(myPlayer, move);

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
                        sink.error(new MoveException(response.getExceptionName() + ": " + response.getExceptionMessage()));
                        return;
                    }
                    logger.info("Move sent successfully.");
                })
                .then();
    }
}
