package client.modelviewcontroller.controller;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import client.ai.AIPlayer;
import client.data.PlayerInformation;
import client.data.fromclient.EMove;
import client.data.fromclient.HalfMap;
import client.data.fromserver.FullMap;
import client.data.fromserver.GameState;
import client.data.fromserver.EPlayerGameState;
import client.halfmaplogic.generation.HalfMapGenerator;
import client.halfmaplogic.validation.HalfMapValidator;
import client.halfmaplogic.validation.Notification;
import client.halfmaplogic.validation.exception.HalfMapGenerationException;
import client.main.GameClientFactory;
import client.modelviewcontroller.model.MapModel;
import client.modelviewcontroller.model.PlayerModel;
import client.network.GameSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class GameControllerTest {
    private GameController controller;
    private PlayerModel playerModelMock;
    private MapModel mapModelMock;
    private GameSession gameSessionMock;
    private HalfMapGenerator generatorMock;
    private HalfMapValidator validatorMock;
    private GameClientFactory factoryMock;

    @BeforeEach
    void setUp() {
        playerModelMock = mock(PlayerModel.class);
        mapModelMock = mock(MapModel.class);
        gameSessionMock = mock(GameSession.class);
        generatorMock = mock(HalfMapGenerator.class);
        validatorMock = mock(HalfMapValidator.class);
        factoryMock = mock(GameClientFactory.class);

        controller = new GameController(playerModelMock, mapModelMock, gameSessionMock,
                generatorMock, validatorMock, factoryMock);
    }

    @Test
    void Constructor_NullPlayerModel_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new GameController(null, mapModelMock, gameSessionMock, generatorMock, validatorMock, factoryMock));
    }

    @Test
    void Constructor_NullMapModel_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new GameController(playerModelMock, null, gameSessionMock, generatorMock, validatorMock, factoryMock));
    }

    @Test
    void Constructor_NullGameSession_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new GameController(playerModelMock, mapModelMock, null, generatorMock, validatorMock, factoryMock));
    }

    @Test
    void Constructor_NullHalfMapGenerator_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new GameController(playerModelMock, mapModelMock, gameSessionMock, null, validatorMock, factoryMock));
    }

    @Test
    void Constructor_NullHalfMapValidator_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new GameController(playerModelMock, mapModelMock, gameSessionMock, generatorMock, null, factoryMock));
    }

    @Test
    void Constructor_NullGameClientFactory_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new GameController(playerModelMock, mapModelMock, gameSessionMock, generatorMock, validatorMock, null));
    }

    @Test
    void RunGame_NullPlayerInformation_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> controller.runGame(null));
    }

    @Test
    void RunGame_FullGameExecution_CoversGameLoopAndAiLogic() {
        var playerInformation = new PlayerInformation("FirstName", "LastName", "uAccount");
        var setupState = mock(GameState.class);
        var turnWait = mock(GameState.class);
        var turnAct = mock(GameState.class);
        var endState = mock(GameState.class);

        var halfMap = mock(HalfMap.class);
        var fullMap = mock(FullMap.class);
        var aiMock = mock(AIPlayer.class);

        when(gameSessionMock.registerPlayer(playerInformation)).thenReturn(Mono.empty());
        when(setupState.myPlayerMustAct()).thenReturn(true);
        when(setupState.fullMapIsEmpty()).thenReturn(true);
        when(setupState.fullMap()).thenReturn(fullMap);

        when(generatorMock.generateHalfMap()).thenReturn(halfMap);
        var note = mock(Notification.class);
        when(note.hasErrors()).thenReturn(false);
        when(validatorMock.validate(halfMap)).thenReturn(note);
        when(gameSessionMock.sendHalfMap(halfMap)).thenReturn(Mono.empty());

        when(gameSessionMock.pollForNewGameState()).thenReturn(
                Flux.just(setupState),
                Flux.just(turnWait),
                Flux.just(turnAct),
                Flux.just(endState)
        );

        when(turnWait.myPlayerMustNotWait()).thenReturn(true);
        when(turnWait.myPlayerWonOrLost()).thenReturn(false);
        when(turnWait.myPlayerMustAct()).thenReturn(false);

        when(turnAct.myPlayerMustNotWait()).thenReturn(true);
        when(turnAct.myPlayerWonOrLost()).thenReturn(false);
        when(turnAct.myPlayerMustAct()).thenReturn(true);
        when(turnAct.fullMap()).thenReturn(fullMap);

        when(factoryMock.createAIPlayer(fullMap)).thenReturn(aiMock);
        when(aiMock.getNextMove()).thenReturn(EMove.Up);
        when(gameSessionMock.sendMove(EMove.Up)).thenReturn(Mono.empty());

        when(endState.myPlayerMustNotWait()).thenReturn(true);
        when(endState.myPlayerWonOrLost()).thenReturn(true);
        when(endState.myPlayerGameState()).thenReturn(EPlayerGameState.Won);

        controller.runGame(playerInformation);

        verify(factoryMock, times(1)).createAIPlayer(fullMap);
        verify(aiMock).updateKnowledgeBase(fullMap);
        verify(gameSessionMock).sendMove(EMove.Up);
        verify(playerModelMock).updateGameEnd(EPlayerGameState.Won);
    }

    @Test
    void RunGame_Player2Entry_AddsTransitionRule() {
        var playerInformation = new PlayerInformation("F", "L", "U");
        var stateAct = mock(GameState.class);
        var fullMap = mock(FullMap.class);

        when(gameSessionMock.registerPlayer(playerInformation)).thenReturn(Mono.empty());
        when(gameSessionMock.pollForNewGameState()).thenReturn(Flux.just(stateAct));

        when(stateAct.myPlayerMustAct()).thenReturn(true);
        when(stateAct.fullMapIsEmpty()).thenReturn(false);
        when(stateAct.fullMap()).thenReturn(fullMap);

        when(generatorMock.generateHalfMap()).thenReturn(mock(HalfMap.class));
        var note = mock(Notification.class);
        when(note.hasErrors()).thenReturn(true);
        when(validatorMock.validate(any())).thenReturn(note);

        assertThrows(HalfMapGenerationException.class, () -> controller.runGame(playerInformation));

        verify(factoryMock).createSecondHalfMapTransitionRule(fullMap);
        verify(validatorMock).addRule(any());
    }

    @Test
    void RunGame_TwoConsecutiveTurns_CoversBothAIInitializationBranches() {
        var playerInformation = new PlayerInformation("F", "L", "U");
        var setupState = mock(GameState.class);
        var turn1Act = mock(GameState.class);
        var turn2Act = mock(GameState.class);
        var endState = mock(GameState.class);

        var fullMap = mock(FullMap.class);
        var aiMock = mock(AIPlayer.class);

        when(gameSessionMock.registerPlayer(playerInformation)).thenReturn(Mono.empty());
        when(setupState.myPlayerMustAct()).thenReturn(true);
        when(setupState.fullMapIsEmpty()).thenReturn(true);
        when(setupState.fullMap()).thenReturn(fullMap);
        when(generatorMock.generateHalfMap()).thenReturn(mock(HalfMap.class));
        var note = mock(Notification.class);
        when(note.hasErrors()).thenReturn(false);
        when(validatorMock.validate(any())).thenReturn(note);
        when(gameSessionMock.sendHalfMap(any())).thenReturn(Mono.empty());

        when(gameSessionMock.pollForNewGameState()).thenReturn(
                Flux.just(setupState),
                Flux.just(turn1Act),
                Flux.just(turn2Act),
                Flux.just(endState)
        );

        var states = new GameState[]{turn1Act, turn2Act};
        for (GameState state : states) {
            when(state.myPlayerMustNotWait()).thenReturn(true);
            when(state.myPlayerWonOrLost()).thenReturn(false);
            when(state.myPlayerMustAct()).thenReturn(true);
            when(state.fullMap()).thenReturn(fullMap);
        }

        when(factoryMock.createAIPlayer(fullMap)).thenReturn(aiMock);
        when(aiMock.getNextMove()).thenReturn(EMove.Up);
        when(gameSessionMock.sendMove(any())).thenReturn(Mono.empty());

        when(endState.myPlayerMustNotWait()).thenReturn(true);
        when(endState.myPlayerWonOrLost()).thenReturn(true);
        when(endState.myPlayerGameState()).thenReturn(EPlayerGameState.Won);

        controller.runGame(playerInformation);

        verify(factoryMock).createAIPlayer(fullMap);

        verify(aiMock, times(2)).updateKnowledgeBase(fullMap);
        verify(gameSessionMock, times(2)).sendMove(EMove.Up);
    }

    @Test
    void RunGame_MapGenerationExhausted_ThrowsHalfMapGenerationException() {
        var playerInformation = new PlayerInformation("F", "L", "U");
        var gameState = mock(GameState.class);

        when(gameSessionMock.registerPlayer(playerInformation)).thenReturn(Mono.empty());
        when(gameSessionMock.pollForNewGameState()).thenReturn(Flux.just(gameState));
        when(gameState.myPlayerMustAct()).thenReturn(true);
        when(gameState.fullMapIsEmpty()).thenReturn(true);

        var errorNote = mock(Notification.class);
        when(errorNote.hasErrors()).thenReturn(true);
        when(errorNote.getErrors()).thenReturn(List.of());
        when(validatorMock.validate(any())).thenReturn(errorNote);
        when(generatorMock.generateHalfMap()).thenReturn(mock(HalfMap.class));

        assertThrows(HalfMapGenerationException.class, () -> controller.runGame(playerInformation));

        verify(generatorMock, times(1000)).generateHalfMap();
        verify(mapModelMock, times(1000)).updateHalfMapValidationErrors(any());
    }
}