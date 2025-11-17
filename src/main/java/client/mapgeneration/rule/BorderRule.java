package client.mapgeneration.rule;

import client.data.ETerrain;
import client.data.XYPair;
import client.data.fromclient.HalfMap;
import client.mapgeneration.validation.IHalfMapValidationRule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class BorderRule implements IHalfMapValidationRule {
    private static final Logger logger = LoggerFactory.getLogger(BorderRule.class);

    private static final XYPair HALF_MAP_SIZE = new XYPair(10, 5);
    private static final XYPair REQUIRED_SIDE = new XYPair(6, 3);

    private static final XYPair TOP_LEFT_CORNER = new XYPair(0, 0);
    private static final XYPair TOP_RIGHT_CORNER = new XYPair(HALF_MAP_SIZE.x() - 1, 0);
    private static final XYPair BOTTOM_LEFT_CORNER = new XYPair(0, HALF_MAP_SIZE.y() - 1);
    private static final XYPair BOTTOM_RIGHT_CORNER = new XYPair(HALF_MAP_SIZE.x() - 1, HALF_MAP_SIZE.y() - 1);

    @Override
    public boolean isValid(HalfMap halfMap) {
        Objects.requireNonNull(halfMap, "halfMap must not be null");

        if (!validBorder(halfMap, TOP_LEFT_CORNER, TOP_RIGHT_CORNER, REQUIRED_SIDE.x()))
            return false;

        if (!validBorder(halfMap, BOTTOM_LEFT_CORNER, BOTTOM_RIGHT_CORNER, REQUIRED_SIDE.x()))
            return false;

        if (!validBorder(halfMap, TOP_LEFT_CORNER, BOTTOM_LEFT_CORNER, REQUIRED_SIDE.y()))
            return false;

        return validBorder(halfMap, TOP_RIGHT_CORNER, BOTTOM_RIGHT_CORNER, REQUIRED_SIDE.y());
    }

    private boolean validBorder(HalfMap halfMap, XYPair start, XYPair end, int requiredCount) {
        Objects.requireNonNull(halfMap, "halfMap must not be null");
        Objects.requireNonNull(start, "start must not be null");
        Objects.requireNonNull(end, "end must not be null");

        int traversableCount = 0;
        for (int x = start.x(); x <= end.x(); ++x)
            for (int y = start.y(); y <= end.y(); y++) {
                ETerrain terrain = halfMap.nodes().get(new XYPair(x, y));
                if (terrain != ETerrain.Water)
                    ++traversableCount;
            }

        return traversableCount >= requiredCount;
    }
}
