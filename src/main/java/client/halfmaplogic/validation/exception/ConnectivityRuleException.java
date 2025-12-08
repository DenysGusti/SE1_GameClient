package client.halfmaplogic.validation.exception;

import java.util.Objects;

public class ConnectivityRuleException extends HalfMapGenerationException {
    public ConnectivityRuleException(String message) {
        super(Objects.requireNonNull(message, "message is null"));
    }
}