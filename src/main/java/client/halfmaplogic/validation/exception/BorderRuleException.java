package client.halfmaplogic.validation.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class BorderRuleException extends HalfMapGenerationException {
    private static final Logger logger = LoggerFactory.getLogger(BorderRuleException.class);

    public BorderRuleException(String message) {
        super(Objects.requireNonNull(message, "message must not be null"));
    }
}