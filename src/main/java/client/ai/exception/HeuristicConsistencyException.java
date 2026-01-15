package client.ai.exception;

import java.util.Objects;

public class HeuristicConsistencyException extends AIException {
    public HeuristicConsistencyException(String message) {
        super(Objects.requireNonNull(message, "message is null"));
    }
}