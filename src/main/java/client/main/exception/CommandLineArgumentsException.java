package client.main.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class CommandLineArgumentsException extends Exception {
    private static final Logger logger = LoggerFactory.getLogger(client.main.exception.CommandLineArgumentsException.class);

    public CommandLineArgumentsException(String message) {
        super(Objects.requireNonNull(message, "message must not be null"));
    }
}
