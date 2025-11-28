package client.network.exception;

import java.util.Objects;

public class NetworkPlayerRegistrationException extends NetworkException {
    public NetworkPlayerRegistrationException(String message) {
        super(Objects.requireNonNull(message, "message must not be null"));
    }
}
