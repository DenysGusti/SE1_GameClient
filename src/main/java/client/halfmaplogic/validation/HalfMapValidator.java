package client.halfmaplogic.validation;

import client.data.fromclient.HalfMap;

import client.halfmaplogic.validation.rule.IHalfMapValidationRule;
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

    public Notification validate(HalfMap halfMap) {
        Objects.requireNonNull(halfMap, "halfMap must not be null");

        var notification = new Notification();
        rules.forEach(rule -> rule.validate(halfMap, notification));
        return notification;
    }
}
