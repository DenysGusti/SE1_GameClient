package client.halfmaplogic.validation.rule;

import client.data.ETerrain;
import client.data.XYPair;
import client.data.fromclient.HalfMap;
import client.halfmaplogic.validation.Notification;

import client.halfmaplogic.validation.exception.FortRuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class FortRule implements IHalfMapValidationRule {
    private static final Logger logger = LoggerFactory.getLogger(FortRule.class);

    private static final int REQUIRED_FORTS = 1;

    @Override
    public void validate(HalfMap halfMap, Notification notification) {
        Objects.requireNonNull(halfMap, "halfMap must not be null");

        if (halfMap.potentialForts().size() != REQUIRED_FORTS)
            notification.addError(new FortRuleException("FortRule: Wrong number of forts. Found " + halfMap.potentialForts().size() + ", Required " + REQUIRED_FORTS));

        for (XYPair fortPosition : halfMap.potentialForts())
            if (halfMap.nodes().get(fortPosition) != ETerrain.Grass)
                notification.addError(new FortRuleException("FortRule: A fort was placed on a non-Grass tile at " + fortPosition));
    }
}
