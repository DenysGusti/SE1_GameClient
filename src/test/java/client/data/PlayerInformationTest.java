package client.data;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class PlayerInformationTest {
    @Test
    public void ValidData_ConstructorCalled_PropertiesAreStored() {
        String f = "Max";
        String l = "Mustermann";
        String u = "u12345";

        var playerInformation = new PlayerInformation(f, l, u);

        assertThat(playerInformation.firstName(), is(f));
        assertThat(playerInformation.lastName(), is(l));
        assertThat(playerInformation.uAccount(), is(u));
    }

    @ParameterizedTest
    @MethodSource("provideNullInputs")
    public void NullInputs_ConstructorCalled_ThrowsIllegalArgumentException(String f, String l, String u) {
        assertThrows(IllegalArgumentException.class, () -> new PlayerInformation(f, l, u));
    }

    private static Stream<Arguments> provideNullInputs() {
        return Stream.of(
                Arguments.of(null, "Last", "u123"),
                Arguments.of("First", null, "u123"),
                Arguments.of("First", "Last", null)
        );
    }

    @ParameterizedTest
    @MethodSource("provideEmptyInputs")
    public void EmptyInputs_ConstructorCalled_ThrowsIllegalArgumentException(String f, String l, String u) {
        assertThrows(IllegalArgumentException.class, () -> new PlayerInformation(f, l, u));
    }

    private static Stream<Arguments> provideEmptyInputs() {
        return Stream.of(
                Arguments.of("", "Last", "u123"),
                Arguments.of("First", "", "u123"),
                Arguments.of("First", "Last", "")
        );
    }

    @ParameterizedTest
    @MethodSource("provideLongInputs")
    public void TooLongInputs_ConstructorCalled_ThrowsIllegalArgumentException(String f, String l, String u) {
        assertThrows(IllegalArgumentException.class, () -> new PlayerInformation(f, l, u));
    }

    private static Stream<Arguments> provideLongInputs() {
        String longStr = "A".repeat(51);
        return Stream.of(
                Arguments.of(longStr, "Last", "u123"),
                Arguments.of("First", longStr, "u123"),
                Arguments.of("First", "Last", longStr)
        );
    }
}