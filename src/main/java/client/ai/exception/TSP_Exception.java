package client.ai.exception;

import java.util.Objects;

public class TSP_Exception extends AI_Exception {
    public TSP_Exception(String message) {
        super(Objects.requireNonNull(message, "message is null"));
    }
}