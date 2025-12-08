package client.network.exception;

import java.util.Objects;

public class NetworkHalfMapException extends NetworkException {
    public NetworkHalfMapException(String message) {
        super(Objects.requireNonNull(message, "message is null"));
    }
}
