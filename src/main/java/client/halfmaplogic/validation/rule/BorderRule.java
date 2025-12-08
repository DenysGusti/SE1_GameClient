package client.halfmaplogic.validation.rule;

import client.data.ETerrain;
import client.data.XYPair;
import client.data.fromclient.HalfMap;
import client.halfmaplogic.validation.Notification;
import client.halfmaplogic.validation.exception.BorderRuleException;

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
    public void validate(HalfMap halfMap, Notification notification) {
        if (halfMap == null)
            throw new IllegalArgumentException("halfMap is null");
        if (notification == null)
            throw new IllegalArgumentException("notification is null");

        if (invalidBorder(halfMap, TOP_LEFT_CORNER, TOP_RIGHT_CORNER, REQUIRED_SIDE.x()))
            notification.addError(new BorderRuleException("BorderRule: Top border (y=0) is not >= 51% traversable (6 nodes)"));

        if (invalidBorder(halfMap, BOTTOM_LEFT_CORNER, BOTTOM_RIGHT_CORNER, REQUIRED_SIDE.x()))
            notification.addError(new BorderRuleException("BorderRule: Bottom border (y=4) is not >= 51% traversable (6 nodes)"));

        if (invalidBorder(halfMap, TOP_LEFT_CORNER, BOTTOM_LEFT_CORNER, REQUIRED_SIDE.y()))
            notification.addError(new BorderRuleException("BorderRule: Left border (x=0) is not >= 51% traversable (3 nodes)"));

        if (invalidBorder(halfMap, TOP_RIGHT_CORNER, BOTTOM_RIGHT_CORNER, REQUIRED_SIDE.y()))
            notification.addError(new BorderRuleException("BorderRule: Right border (x=9) is not >= 51% traversable (3 nodes)"));
    }

    private boolean invalidBorder(HalfMap halfMap, XYPair start, XYPair end, int requiredCount) {
        if (halfMap == null)
            throw new IllegalArgumentException("halfMap is null");
        if (start == null)
            throw new IllegalArgumentException("start is null");
        if (end == null)
            throw new IllegalArgumentException("end is null");
        if (requiredCount < 0)
            throw new IllegalArgumentException("requiredCount is negative");

        int traversableCount = 0;
        for (int x = start.x(); x <= end.x(); ++x)
            for (int y = start.y(); y <= end.y(); y++) {
                ETerrain terrain = halfMap.nodes().get(new XYPair(x, y));
                if (terrain != ETerrain.Water)
                    ++traversableCount;
            }

        return traversableCount < requiredCount;
    }
}
