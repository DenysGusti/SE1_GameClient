package client.data;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class UniquePlayerIdentifierTest {
    @ParameterizedTest
    @MethodSource("provideValidPlayerIDs")
    public void ValidIDProvided_ConstructorCalled_IdentifierCreated(String id) {
        var uniquePlayerIdentifier = new UniquePlayerIdentifier(id);
        assertThat(uniquePlayerIdentifier.uniquePlayerID(), is(id));
    }

    private static Stream<Arguments> provideValidPlayerIDs() {
        return Stream.of(
                Arguments.of("Player1"),
                Arguments.of(""),
                Arguments.of("UUID-1234-5678")
        );
    }

    @Test
    public void NullIDProvided_ConstructorCalled_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new UniquePlayerIdentifier(null));
    }
}