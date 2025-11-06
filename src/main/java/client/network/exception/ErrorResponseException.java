package client.network.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorResponseException extends RuntimeException {
    private static final Logger logger = LoggerFactory.getLogger(ErrorResponseException.class);

    public ErrorResponseException(String message) {
        super(message);
    }
}
