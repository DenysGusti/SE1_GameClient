package client.network;

import client.data.*;
import client.data.fromclient.*;
import client.data.fromclient.EMove;
import client.data.fromserver.*;

import client.network.exception.ErrorResponseException;

import client.network.fromclient.FromClientConverter;
import client.network.fromserver.FromServerConverter;
import client.network.fromserver.FullMapAccumulator;
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

import java.time.Duration;
import java.util.Objects;

public class NetworkService {
    private static final Logger logger = LoggerFactory.getLogger(NetworkService.class);
    private static final long MIN_REQUEST_INTERVAL_NS = 400_000_000; // 0.4 seconds

    private final WebClient gameWebClient;
    private final FromClientConverter fromClientConverter;
    private final FromServerConverter fromServerConverter;
    private final FullMapAccumulator fullMapAccumulator;

    private String myPlayerID = null;
    private long lastGameStateRequestTime = 0; // in nanoseconds

    public static String createNewGame(String serverBaseUrl, boolean debugMode, boolean dummyCompetition) {
        Objects.requireNonNull(serverBaseUrl, "serverBaseUrl must not be null");
        logger.info("Attempting to create a new game, debugMode={}, dummyCompetition={}", debugMode, dummyCompetition);

        var webClient = WebClient
                .builder()
                .baseUrl(serverBaseUrl + "/games")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_VALUE)
                .build();

        Mono<UniqueGameIdentifier> webAccess = webClient
                .method(HttpMethod.GET)
                .uri(uriBuilder -> uriBuilder
                        .queryParam("enableDebugMode", debugMode)
                        .queryParam("enableDummyCompetition", dummyCompetition)
                        .build()
                )
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {
                });

        UniqueGameIdentifier uniqueGameIdentifier = webAccess.block();
        Objects.requireNonNull(uniqueGameIdentifier, "Server response for new game creation is null.");

        logger.info("New game created with ID: {}", uniqueGameIdentifier.getUniqueGameID());
        return uniqueGameIdentifier.getUniqueGameID();
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
        PlayerRegistration playerRegistration = this.fromClientConverter.convertPlayerInformation(playerInformation);

        return this.gameWebClient
                .method(HttpMethod.POST)
                .uri("/players")
                .body(BodyInserters.fromValue(playerRegistration))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ResponseEnvelope<UniquePlayerIdentifier>>() {
                })
                .doOnNext(response -> {
                            Objects.requireNonNull(response, "Server response for registering player is null.");
                            if (response.getState() == ERequestState.Error)
                                throw new ErrorResponseException(response.getExceptionMessage());

                            UniquePlayerIdentifier uniquePlayerIdentifier = response.getData().orElseThrow();
                            this.myPlayerID = uniquePlayerIdentifier.getUniquePlayerID();
                            logger.info("Player registered with ID: {}", this.myPlayerID);
                        }
                ).then();
    }

    public Mono<GameState> receiveGameState() {
        Objects.requireNonNull(this.myPlayerID, "myPlayerID must not be null");

        long delayNs = calculateRequestDelayNs();
        this.lastGameStateRequestTime = System.nanoTime() + delayNs;

        return Mono.delay(Duration.ofNanos(delayNs))
                .flatMap(tick -> {
                    logger.debug("Requesting game state...");
                    return this.gameWebClient
                            .method(HttpMethod.GET)
                            .uri("/states/" + this.myPlayerID)
                            .retrieve()
                            .bodyToMono(new ParameterizedTypeReference<ResponseEnvelope<messagesbase.messagesfromserver.GameState>>() {
                            });
                })
                .<messagesbase.messagesfromserver.GameState>handle((response, sink) -> {
                    Objects.requireNonNull(response, "Server response for receiving game state is null.");
                    if (response.getState() == ERequestState.Error) {
                        sink.error(new ErrorResponseException(response.getExceptionName() + ": " + response.getExceptionMessage()));
                        return;
                    }
                    sink.next(response.getData().orElseThrow());
                })
                .map(serverGameState -> {
                    GameState clientGameState = fromServerConverter.convertGameState(this.myPlayerID, serverGameState);

                    boolean hasCollectedTreasure = clientGameState.myPlayerHasCollectedTreasure();

                    this.fullMapAccumulator.accumulateFullMap(clientGameState.fullMap(), hasCollectedTreasure);

                    return clientGameState.withFullMap(this.fullMapAccumulator.getFullMap());
                });
    }

    public Mono<Void> sendHalfMap(HalfMap halfMap) {
        Objects.requireNonNull(halfMap, "halfMap must not be null");
        Objects.requireNonNull(this.myPlayerID, "myPlayerID must not be null");
        logger.info("Sending map to server...");

        PlayerHalfMap playerHalfMap = this.fromClientConverter.convertHalfMap(this.myPlayerID, halfMap);

        return this.gameWebClient
                .method(HttpMethod.POST)
                .uri("/halfmaps")
                .body(BodyInserters.fromValue(playerHalfMap))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ResponseEnvelope>() {
                })
                .doOnNext(response -> {
                    Objects.requireNonNull(response, "Server response for sending half map is null.");
                    if (response.getState() == ERequestState.Error)
                        throw new ErrorResponseException(response.getExceptionName() + ": " + response.getExceptionMessage());

                    logger.info("Map sent successfully.");
                })
                .then();
    }

    public Mono<Void> sendMove(EMove move) {
        Objects.requireNonNull(move, "move must not be null");
        Objects.requireNonNull(this.myPlayerID, "myPlayerID must not be null, register player first.");
        logger.info("Sending move: {}", move);

        PlayerMove playerMove = this.fromClientConverter.convertMove(this.myPlayerID, move);

        return this.gameWebClient
                .method(HttpMethod.POST)
                .uri("/moves")
                .body(BodyInserters.fromValue(playerMove))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ResponseEnvelope>() {
                })
                .doOnNext(response -> {
                    Objects.requireNonNull(response, "Server response for send move is null.");
                    if (response.getState() == ERequestState.Error)
                        throw new ErrorResponseException(response.getExceptionName() + ": " + response.getExceptionMessage());

                    logger.info("Move sent successfully.");
                })
                .then();
    }

    private long calculateRequestDelayNs() {
        long timeSinceLastRequest = System.nanoTime() - this.lastGameStateRequestTime;
        if (timeSinceLastRequest < MIN_REQUEST_INTERVAL_NS) {
            long sleepTimeNs = MIN_REQUEST_INTERVAL_NS - timeSinceLastRequest;
            logger.debug("Throttling request. Will wait for {}ms.", sleepTimeNs / 1_000_000);
            return sleepTimeNs;
        }
        return 0;
    }
}
