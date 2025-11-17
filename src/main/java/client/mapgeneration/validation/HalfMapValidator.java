package client.mapgeneration.validation;

import client.data.fromclient.HalfMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Objects;

public class HalfMapValidator {
    private static final Logger logger = LoggerFactory.getLogger(HalfMapValidator.class);

    Collection<IHalfMapValidationRule> rules;

    public HalfMapValidator(Collection<IHalfMapValidationRule> rules) {
        this.rules = Objects.requireNonNull(rules, "rules must not be null");
    }

    public boolean isValid(HalfMap halfMap) {
        Objects.requireNonNull(halfMap, "halfMap must not be null");

        for (IHalfMapValidationRule rule : rules) {
            if (!rule.isValid(halfMap))
                return false;
        }
        return true;
    }
}
