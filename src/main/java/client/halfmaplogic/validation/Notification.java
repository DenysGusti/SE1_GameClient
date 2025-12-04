package client.halfmaplogic.validation;

import client.halfmaplogic.validation.exception.HalfMapGenerationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Notification {
    private static final Logger logger = LoggerFactory.getLogger(Notification.class);

    private final Collection<HalfMapGenerationException> errors = new ArrayList<>();

    public void addError(HalfMapGenerationException exception) {
        if (exception == null)
            throw new IllegalArgumentException("exception must not be null");

        errors.add(exception);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public Collection<HalfMapGenerationException> getErrors() {
        return List.copyOf(errors);
    }
}
