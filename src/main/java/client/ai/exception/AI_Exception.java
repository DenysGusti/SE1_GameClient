package client.ai.exception;

import java.util.Objects;

public class AI_Exception extends RuntimeException {
    public AI_Exception(String message) {
        super(Objects.requireNonNull(message, "message is null"));
    }
}
