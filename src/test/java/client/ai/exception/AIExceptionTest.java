package client.ai.exception;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class AIExceptionTest {
    @Test
    public void CustomMessage_ConstructorCalled_MessageIsStored() {
        String expectedMessage = "AI pathfinding failed";
        var exception = new AIException(expectedMessage);

        assertThat(exception.getMessage(), is(expectedMessage));
    }

    @Test
    public void NullMessage_ConstructorCalled_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new AIException(null));
    }
}