package client.network.exception;

import java.util.Objects;

public class NetworkGameStateException extends NetworkException {
    public NetworkGameStateException(String message) {
        super(Objects.requireNonNull(message, "message is null"));
    }
}