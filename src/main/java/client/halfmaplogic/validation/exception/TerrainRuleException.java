package client.halfmaplogic.validation.exception;

import java.util.Objects;

public class TerrainRuleException extends HalfMapGenerationException {
    public TerrainRuleException(String message) {
        super(Objects.requireNonNull(message, "message is null"));
    }
}