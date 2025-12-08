package client.halfmaplogic.validation.exception;

import java.util.Objects;

public class HalfMapGenerationException extends RuntimeException {
    public HalfMapGenerationException(String message) {
        super(Objects.requireNonNull(message, "message is null"));
    }
}