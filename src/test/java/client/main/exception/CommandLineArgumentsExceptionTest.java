package client.main.exception;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class CommandLineArgumentsExceptionTest {
    @Test
    public void CustomMessage_ConstructorCalled_MessageIsStored() {
        String expectedMessage = "Invalid server address provided";
        var exception = new CommandLineArgumentsException(expectedMessage);

        assertThat(exception.getMessage(), is(expectedMessage));
    }

    @Test
    public void NullMessage_ConstructorCalled_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new CommandLineArgumentsException(null));
    }
}