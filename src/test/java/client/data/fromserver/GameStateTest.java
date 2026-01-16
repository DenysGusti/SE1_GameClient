package client.data.fromserver;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import client.data.PlayerInformation;

public class GameStateTest {
    private static FullMap emptyFullMap;
    private static PlayerState myPlayerState;
    private static PlayerInformation playerInfo;

    @BeforeAll
    public static void setUp() {
        emptyFullMap = FullMap.emptyFullMap();
        playerInfo = new PlayerInformation("Max", "Mustermann", "u12345");
        myPlayerState = new PlayerState(playerInfo, false, EPlayerGameState.MustAct);
    }

    @Test
    public void ValidArguments_ConstructorCalled_PropertiesAreStored() {
        var gameState = new GameState("id123", emptyFullMap, myPlayerState, null);

        assertAll(
                () -> assertThat(gameState.gameStateID(), is("id123")),
                () -> assertThat(gameState.fullMap(), is(emptyFullMap)),
                () -> assertThat(gameState.myPlayer(), is(myPlayerState)),
                () -> assertThat(gameState.getOptionalEnemyPlayer(), is(Optional.empty()))
        );
    }

    @Test
    public void GameStateWithEnemy_getOptionalEnemyPlayerCalled_ReturnsPresentOptional() {
        var enemy = new PlayerState(playerInfo, false, EPlayerGameState.MustWait);
        var gameState = new GameState("id123", emptyFullMap, myPlayerState, enemy);

        assertThat(gameState.getOptionalEnemyPlayer(), is(Optional.of(enemy)));
    }

    @Test
    public void ValidGameState_withFullMapCalled_ReturnsNewInstanceWithUpdatedMap() {
        var initialGameState = new GameState("id1", emptyFullMap, myPlayerState, null);
        FullMap newMap = FullMap.emptyFullMap();

        GameState updatedGameState = initialGameState.withFullMap(newMap);

        assertThat(updatedGameState.fullMap(), is(newMap));
        assertThat(updatedGameState, not(sameInstance(initialGameState)));
    }

    @ParameterizedTest
    @MethodSource("providePlayerStateScenarios")
    public void GameStateWithSpecificPlayerState_CheckStatusMethods_ReturnsCorrectBooleans(
            EPlayerGameState playerGameState, boolean mustNotWait, boolean mustAct, boolean wonOrLost) {
        var playerState = new PlayerState(playerInfo, false, playerGameState);
        var gameState = new GameState("id", emptyFullMap, playerState, null);

        assertAll(
                () -> assertThat(gameState.myPlayerMustNotWait(), is(mustNotWait)),
                () -> assertThat(gameState.myPlayerMustAct(), is(mustAct)),
                () -> assertThat(gameState.myPlayerWonOrLost(), is(wonOrLost)),
                () -> assertThat(gameState.myPlayerGameState(), is(playerGameState))
        );
    }

    private static Stream<Arguments> providePlayerStateScenarios() {
        return Stream.of(
                Arguments.of(EPlayerGameState.MustWait, false, false, false),
                Arguments.of(EPlayerGameState.MustAct, true, true, false),
                Arguments.of(EPlayerGameState.Won, true, false, true),
                Arguments.of(EPlayerGameState.Lost, true, false, true)
        );
    }

    @Test
    public void GameStateWithEmptyMap_fullMapIsEmptyCalled_ReturnsTrue() {
        var gameState = new GameState("id", emptyFullMap, myPlayerState, null);
        assertThat(gameState.fullMapIsEmpty(), is(true));
    }

    @Test
    public void NullGameStateID_ConstructorCalled_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new GameState(null, emptyFullMap, myPlayerState, null));
    }

    @Test
    public void NullFullMap_ConstructorCalled_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new GameState("id", null, myPlayerState, null));
    }

    @Test
    public void NullMyPlayer_ConstructorCalled_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new GameState("id", emptyFullMap, null, null));
    }

    @Test
    public void ValidGameState_withFullMapNull_ThrowsIllegalArgumentException() {
        var initialGameState = new GameState("id", emptyFullMap, myPlayerState, null);
        assertThrows(IllegalArgumentException.class, () -> initialGameState.withFullMap(null));
    }
}