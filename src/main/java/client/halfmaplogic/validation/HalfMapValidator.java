package client.halfmaplogic.validation;

import client.data.fromclient.HalfMap;

import client.halfmaplogic.validation.rule.IHalfMapValidationRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

public class HalfMapValidator {
    private static final Logger logger = LoggerFactory.getLogger(HalfMapValidator.class);

    Set<IHalfMapValidationRule> rules;

    public HalfMapValidator(Set<IHalfMapValidationRule> rules) {
        if (rules == null)
            throw new IllegalArgumentException("rules must not be null");

        this.rules = rules;
    }

    public Notification validate(HalfMap halfMap) {
        if (halfMap == null)
            throw new IllegalArgumentException("halfMap must not be null");

        var notification = new Notification();
        rules.forEach(rule -> rule.validate(halfMap, notification));
        return notification;
    }
}
