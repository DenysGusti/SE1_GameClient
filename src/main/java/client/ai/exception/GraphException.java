package client.ai.exception;

import java.util.Objects;

public class GraphException extends AIException {
    public GraphException(String message) {
        super(Objects.requireNonNull(message, "message is null"));
    }
}