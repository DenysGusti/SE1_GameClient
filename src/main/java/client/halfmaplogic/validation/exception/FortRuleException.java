package client.halfmaplogic.validation.exception;

import java.util.Objects;

public class FortRuleException extends HalfMapGenerationException {
    public FortRuleException(String message) {
        super(Objects.requireNonNull(message, "message is null"));
    }
}