package client.ai.exception;

import java.util.Objects;

public class PathException extends AI_Exception {
    public PathException(String message) {
        super(Objects.requireNonNull(message, "message is null"));
    }
}