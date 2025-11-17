package client.mapgeneration.rule;

import client.data.ETerrain;
import client.data.XYPair;
import client.data.fromclient.HalfMap;
import client.mapgeneration.validation.IHalfMapValidationRule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class FortRule implements IHalfMapValidationRule {
    private static final Logger logger = LoggerFactory.getLogger(FortRule.class);

    private static final int REQUIRED_FORTS = 1;

    @Override
    public boolean isValid(HalfMap halfMap) {
        Objects.requireNonNull(halfMap, "halfMap must not be null");

        if (halfMap.potentialForts().size() != REQUIRED_FORTS)
            return false;

        for (XYPair fortPosition : halfMap.potentialForts())
            if (halfMap.nodes().get(fortPosition) != ETerrain.Grass)
                return false;

        return true;
    }
}
