package client.data;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class UniqueGameIdentifierTest {
    @ParameterizedTest
    @MethodSource("provideValidGameIDs")
    public void ValidIDWithLengthFive_ConstructorCalled_IdentifierIsStored(String id) {
        var gameIdentifier = new UniqueGameIdentifier(id);
        assertThat(gameIdentifier.uniqueGameID(), is(id));
    }

    private static Stream<Arguments> provideValidGameIDs() {
        return Stream.of(
                Arguments.of("ABCDE"),
                Arguments.of("12345"),
                Arguments.of("A1B2C")
        );
    }

    @Test
    public void NullID_ConstructorCalled_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new UniqueGameIdentifier(null));
    }

    @ParameterizedTest
    @MethodSource("provideInvalidLengthIDs")
    public void InvalidLengthID_ConstructorCalled_ThrowsIllegalArgumentException(String invalidId) {
        assertThrows(IllegalArgumentException.class, () -> new UniqueGameIdentifier(invalidId));
    }

    private static Stream<Arguments> provideInvalidLengthIDs() {
        return Stream.of(
                Arguments.of(""),
                Arguments.of("1234"),
                Arguments.of("123456")
        );
    }
}