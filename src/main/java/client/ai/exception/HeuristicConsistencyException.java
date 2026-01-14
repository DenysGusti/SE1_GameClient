package client.ai.exception;

import java.util.Objects;

public class HeuristicConsistencyException extends AI_Exception {
    public HeuristicConsistencyException(String message) {
        super(Objects.requireNonNull(message, "message is null"));
    }
}