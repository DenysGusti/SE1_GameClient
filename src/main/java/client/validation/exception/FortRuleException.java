package client.validation.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class FortRuleException extends HalfMapGenerationException {
    private static final Logger logger = LoggerFactory.getLogger(ConnectivityRuleException.class);

    public FortRuleException(String message) {
        super(Objects.requireNonNull(message, "message must not be null"));
    }
}