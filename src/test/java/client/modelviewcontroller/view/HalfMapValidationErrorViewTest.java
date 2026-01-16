package client.modelviewcontroller.view;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import client.halfmaplogic.validation.exception.HalfMapGenerationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.NoSuchElementException;

class HalfMapValidationErrorViewTest {
    private HalfMapValidationErrorView view;
    private final PrintStream standardErr = System.err;
    private final ByteArrayOutputStream errorStreamCaptor = new ByteArrayOutputStream();

    @BeforeEach
    void setUp() {
        view = new HalfMapValidationErrorView();
        System.setErr(new PrintStream(errorStreamCaptor));
    }

    @AfterEach
    void tearDown() {
        System.setErr(standardErr);
    }

    @Test
    void Update_NullErrors_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> view.update(null));
    }

    @Test
    void Update_ValidErrorList_PrintsFormattedErrors() {
        var exception = new HalfMapGenerationException("Invalid terrain");
        var ruleElement = new StackTraceElement(
                "client.halfmaplogic.validation.rule.TerrainRule", "validate", "TerrainRule.java", 10);
        exception.setStackTrace(new StackTraceElement[]{ruleElement});

        view.update(List.of(exception));

        String output = errorStreamCaptor.toString();
        assertThat(output, containsString("--- HALF-MAP VALIDATION ERRORS ---"));
        assertThat(output, containsString("Type: HalfMapGenerationException"));
        assertThat(output, containsString("Description: Invalid terrain"));
        assertThat(output, containsString("Logic Reference: client.halfmaplogic.validation.rule.TerrainRule.validate (line 10)"));
    }

    @Test
    void Update_ErrorWithoutValidationRuleInStack_ThrowsNoSuchElementException() {
        var exception = new HalfMapGenerationException("Generic error");
        var genericElement = new StackTraceElement(
                "java.util.ArrayList", "add", "ArrayList.java", 50);
        exception.setStackTrace(new StackTraceElement[]{genericElement});

        assertThrows(NoSuchElementException.class, () -> view.update(List.of(exception)));
    }
}