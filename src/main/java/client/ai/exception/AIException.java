package client.ai.exception;

import java.util.Objects;

public class AIException extends RuntimeException {
    public AIException(String message) {
        super(Objects.requireNonNull(message, "message is null"));
    }
}
