package client.data.fromserver;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import client.data.PlayerInformation;

public class PlayerStateTest {
    private static PlayerInformation playerInformation;

    @BeforeAll
    public static void setUp() {
        playerInformation = new PlayerInformation("Max", "Mustermann", "u12345");
    }

    @ParameterizedTest
    @MethodSource("provideStateScenarios")
    public void PlayerInSpecificState_CheckStatusMethods_ReturnsCorrectBooleans(EPlayerGameState gameState,
                                                                                boolean wait, boolean act, boolean won, boolean lost) {
        var state = new PlayerState(playerInformation, false, gameState);

        assertThat(state.mustWait(), is(wait));
        assertThat(state.mustAct(), is(act));
        assertThat(state.won(), is(won));
        assertThat(state.lost(), is(lost));
    }

    private static Stream<Arguments> provideStateScenarios() {
        return Stream.of(
                Arguments.of(EPlayerGameState.MustWait, true, false, false, false),
                Arguments.of(EPlayerGameState.MustAct, false, true, false, false),
                Arguments.of(EPlayerGameState.Won, false, false, true, false),
                Arguments.of(EPlayerGameState.Lost, false, false, false, true)
        );
    }

    @Test
    public void PlayerHasTreasure_GetterCalled_ReturnsTrue() {
        var state = new PlayerState(playerInformation, true, EPlayerGameState.MustAct);
        assertThat(state.hasCollectedTreasure(), is(true));
    }

    @Test
    public void NullPlayerInformation_ConstructorCalled_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new PlayerState(null, false, EPlayerGameState.MustWait));
    }

    @Test
    public void NullGameState_ConstructorCalled_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new PlayerState(playerInformation, false, null));
    }
}