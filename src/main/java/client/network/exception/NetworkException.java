package client.network.exception;

import java.util.Objects;

public class NetworkException extends RuntimeException {
    public NetworkException(String message) {
        super(Objects.requireNonNull(message, "message must not be null"));
    }
}