package client.halfmaplogic.validation.exception;

import java.util.Objects;

public class BorderRuleException extends HalfMapGenerationException {
    public BorderRuleException(String message) {
        super(Objects.requireNonNull(message, "message must not be null"));
    }
}