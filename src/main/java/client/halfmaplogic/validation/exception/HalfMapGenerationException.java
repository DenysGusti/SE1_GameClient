package client.halfmaplogic.validation.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class HalfMapGenerationException extends RuntimeException {
    private static final Logger logger = LoggerFactory.getLogger(HalfMapGenerationException.class);

    public HalfMapGenerationException(String message) {
        super(Objects.requireNonNull(message, "message must not be null"));
    }
}