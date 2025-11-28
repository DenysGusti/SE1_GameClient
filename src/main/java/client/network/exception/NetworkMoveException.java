package client.network.exception;

import java.util.Objects;

public class NetworkMoveException extends NetworkException {
    public NetworkMoveException(String message) {
        super(Objects.requireNonNull(message, "message must not be null"));
    }
}
