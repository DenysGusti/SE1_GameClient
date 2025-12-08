package client.main.exception;

import java.util.Objects;

public class CommandLineArgumentsException extends Exception {
    public CommandLineArgumentsException(String message) {
        super(Objects.requireNonNull(message, "message is null"));
    }
}
