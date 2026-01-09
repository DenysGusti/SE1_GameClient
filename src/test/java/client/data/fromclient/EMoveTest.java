package client.data.fromclient;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import client.data.XYPair;

public class EMoveTest {
    @ParameterizedTest
    @MethodSource("provideValidDeltas")
    public void ValidDeltaProvided_FromDeltaCalled_CorrectEMoveReturned(XYPair delta, EMove expectedMove) {
        EMove result = EMove.fromDelta(delta);
        assertThat(result, is(expectedMove));
    }

    private static Stream<Arguments> provideValidDeltas() {
        return Stream.of(
                Arguments.of(new XYPair(0, -1), EMove.Up),
                Arguments.of(new XYPair(0, 1), EMove.Down),
                Arguments.of(new XYPair(-1, 0), EMove.Left),
                Arguments.of(new XYPair(1, 0), EMove.Right)
        );
    }

    @Test
    public void NullDeltaProvided_FromDeltaCalled_IllegalArgumentExceptionThrown() {
        assertThrows(IllegalArgumentException.class, () -> EMove.fromDelta(null));
    }

    @ParameterizedTest
    @MethodSource("provideInvalidDeltas")
    public void InvalidDeltaProvided_FromDeltaCalled_IllegalStateExceptionThrown(XYPair invalidDelta) {
        assertThrows(IllegalStateException.class, () -> EMove.fromDelta(invalidDelta));
    }

    private static Stream<Arguments> provideInvalidDeltas() {
        return Stream.of(
                Arguments.of(new XYPair(0, 0)),
                Arguments.of(new XYPair(1, 1)),
                Arguments.of(new XYPair(-1, -1)),
                Arguments.of(new XYPair(1, 2))
        );
    }
}