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

    public void addErrors(List<HalfMapGenerationException> exceptions) {
        if (exceptions == null)
            throw new IllegalArgumentException("exceptions is null");

        errors.addAll(exceptions);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public List<HalfMapGenerationException> getErrors() {
        return List.copyOf(errors);
    }
}
