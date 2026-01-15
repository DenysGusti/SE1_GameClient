package client.halfmaplogic.validation.exception;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class TerrainRuleExceptionTest {
    @Test
    public void CustomMessage_ConstructorCalled_MessageIsStored() {
        String expectedMessage = "Insufficient number of grass fields";
        var exception = new TerrainRuleException(expectedMessage);

        assertThat(exception.getMessage(), is(expectedMessage));
    }

    @Test
    public void NullMessage_ConstructorCalled_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new TerrainRuleException(null));
    }
}