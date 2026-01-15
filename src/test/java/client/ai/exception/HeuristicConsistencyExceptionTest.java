package client.ai.exception;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class HeuristicConsistencyExceptionTest {
    @Test
    public void ValidMessage_ConstructorCalled_MessageIsCorrectlyStored() {
        String expectedMessage = "Heuristic is not consistent";
        var exception = new HeuristicConsistencyException(expectedMessage);

        assertThat(exception.getMessage(), is(expectedMessage));
    }

    @Test
    public void NullMessage_ConstructorCalled_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new HeuristicConsistencyException(null));
    }
}