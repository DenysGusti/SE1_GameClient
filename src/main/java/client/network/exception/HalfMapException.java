package client.network.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class HalfMapException extends RuntimeException {
    private static final Logger logger = LoggerFactory.getLogger(HalfMapException.class);

    public HalfMapException(String message) {
        super(Objects.requireNonNull(message, "message must not be null"));
    }
}
