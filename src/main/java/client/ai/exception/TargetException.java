package client.ai.exception;

import java.util.Objects;

public class TargetException extends AI_Exception {
    public TargetException(String message) {
        super(Objects.requireNonNull(message, "message is null"));
    }
}