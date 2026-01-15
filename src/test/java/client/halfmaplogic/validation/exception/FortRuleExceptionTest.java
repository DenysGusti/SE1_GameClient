package client.halfmaplogic.validation.exception;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class FortRuleExceptionTest {
    @Test
    public void CustomMessage_ConstructorCalled_MessageIsStored() {
        String expectedMessage = "Fort must be placed on a grass field";
        var exception = new FortRuleException(expectedMessage);

        assertThat(exception.getMessage(), is(expectedMessage));
    }

    @Test
    public void NullMessage_ConstructorCalled_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new FortRuleException(null));
    }
}