package client.halfmaplogic.validation;

import client.data.fromclient.HalfMap;

import client.halfmaplogic.validation.rule.HalfMapValidationRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class HalfMapValidator {
    private static final Logger logger = LoggerFactory.getLogger(HalfMapValidator.class);

    Set<HalfMapValidationRule> rules;

    public HalfMapValidator(Set<HalfMapValidationRule> rules) {
        if (rules == null)
            throw new IllegalArgumentException("rules is null");

        this.rules = rules;
    }

    public void addRule(HalfMapValidationRule rule) {
        if (rule == null)
            throw new IllegalArgumentException("rule is null");

        rules.add(rule);
    }

    public Notification validate(HalfMap halfMap) {
        if (halfMap == null)
            throw new IllegalArgumentException("halfMap is null");

        var notification = new Notification();
        for (HalfMapValidationRule rule : rules)
            notification.addErrors(rule.validate(halfMap));

        return notification;
    }
}
