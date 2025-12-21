package client.modelviewcontroller.view;

import client.halfmaplogic.validation.exception.HalfMapGenerationException;
import client.modelviewcontroller.observer.Subscriber;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

public class HalfMapValidationErrorView implements Subscriber<List<HalfMapGenerationException>> {
    private static final Logger logger = LoggerFactory.getLogger(HalfMapValidationErrorView.class);

    @Override
    public void update(List<HalfMapGenerationException> errors) {
        if (errors == null)
            throw new IllegalArgumentException("errors is null");
        if (errors.isEmpty())
            return;

        System.err.println("--- HALF-MAP VALIDATION ERRORS ---");

        for (HalfMapGenerationException halfMapGenerationException : errors) {
            String errorType = halfMapGenerationException.getClass().getSimpleName();
            String description = halfMapGenerationException.getMessage();
            StackTraceElement stackTraceElement = findValidationLogicSource(halfMapGenerationException);
            Objects.requireNonNull(stackTraceElement, "stackTraceElement is null");

            String buffer = String.format("Type: %s\nDescription: %s\nLogic Reference: %s.%s (line %d)\n",
                    errorType, description,
                    stackTraceElement.getClassName(),
                    stackTraceElement.getMethodName(),
                    stackTraceElement.getLineNumber());
            System.err.println(buffer);
        }
    }

    private StackTraceElement findValidationLogicSource(HalfMapGenerationException exception) {
        for (StackTraceElement stackTraceElement : exception.getStackTrace())
            if (stackTraceElement.getClassName().contains("client.halfmaplogic.validation.rule"))
                return stackTraceElement;
        return null;
    }
}