package client.halfmaplogic.validation.rule;

import client.data.ETerrain;
import client.data.XYPair;
import client.data.fromclient.HalfMap;
import client.halfmaplogic.validation.exception.BorderRuleException;

import client.halfmaplogic.validation.exception.HalfMapGenerationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class BorderRule implements IHalfMapValidationRule {
    private static final Logger logger = LoggerFactory.getLogger(BorderRule.class);

    private static final XYPair HALF_MAP_SIZE = new XYPair(10, 5);
    private static final XYPair REQUIRED_TRAVERSABLE_SIDE = new XYPair(4, 2);
    private static final XYPair REQUIRED_NON_TRAVERSABLE_SIDE = new XYPair(2, 1);

    private static final XYPair TOP_LEFT_CORNER = new XYPair(0, 0);
    private static final XYPair TOP_RIGHT_CORNER = new XYPair(HALF_MAP_SIZE.x() - 1, 0);
    private static final XYPair BOTTOM_LEFT_CORNER = new XYPair(0, HALF_MAP_SIZE.y() - 1);
    private static final XYPair BOTTOM_RIGHT_CORNER = new XYPair(HALF_MAP_SIZE.x() - 1, HALF_MAP_SIZE.y() - 1);

    @Override
    public List<HalfMapGenerationException> validate(HalfMap halfMap) {
        if (halfMap == null)
            throw new IllegalArgumentException("halfMap is null");

        List<HalfMapGenerationException> exceptions = new ArrayList<>();

        if (invalidBorder(halfMap, TOP_LEFT_CORNER, TOP_RIGHT_CORNER, REQUIRED_TRAVERSABLE_SIDE.x(), REQUIRED_NON_TRAVERSABLE_SIDE.x()))
            exceptions.add(new BorderRuleException("BorderRule: Top border (y=0) violation."));

        if (invalidBorder(halfMap, BOTTOM_LEFT_CORNER, BOTTOM_RIGHT_CORNER, REQUIRED_TRAVERSABLE_SIDE.x(), REQUIRED_NON_TRAVERSABLE_SIDE.x()))
            exceptions.add(new BorderRuleException("BorderRule: Bottom border (y=4) violation."));

        if (invalidBorder(halfMap, TOP_LEFT_CORNER, BOTTOM_LEFT_CORNER, REQUIRED_TRAVERSABLE_SIDE.y(), REQUIRED_NON_TRAVERSABLE_SIDE.y()))
            exceptions.add(new BorderRuleException("BorderRule: Left border (x=0) violation."));

        if (invalidBorder(halfMap, TOP_RIGHT_CORNER, BOTTOM_RIGHT_CORNER, REQUIRED_TRAVERSABLE_SIDE.y(), REQUIRED_NON_TRAVERSABLE_SIDE.y()))
            exceptions.add(new BorderRuleException("BorderRule: Right border (x=9) violation."));

        return exceptions;
    }

    private static boolean invalidBorder(HalfMap halfMap, XYPair start, XYPair end,
                                         int requiredTraversableCount, int requiredNonTraversableCount) {
        if (halfMap == null)
            throw new IllegalArgumentException("halfMap is null");
        if (start == null)
            throw new IllegalArgumentException("start is null");
        if (end == null)
            throw new IllegalArgumentException("end is null");
        if (requiredTraversableCount < 0)
            throw new IllegalArgumentException("requiredTraversableCount is negative");
        if (requiredNonTraversableCount < 0)
            throw new IllegalArgumentException("requiredNonTraversableCount is negative");

        int traversableCount = 0;
        int nonTraversableCount = 0;

        for (int x = start.x(); x <= end.x(); ++x)
            for (int y = start.y(); y <= end.y(); y++) {
                ETerrain terrain = halfMap.nodes().get(new XYPair(x, y));

                if (terrain == ETerrain.Water)
                    ++nonTraversableCount;
                else
                    ++traversableCount;
            }

        return traversableCount < requiredTraversableCount || nonTraversableCount < requiredNonTraversableCount;
    }
}