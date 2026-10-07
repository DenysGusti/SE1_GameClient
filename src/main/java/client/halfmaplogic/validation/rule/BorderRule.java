package client.halfmaplogic.validation.rule;

import client.data.XYPair;
import client.data.fromclient.HalfMap;
import client.halfmaplogic.validation.exception.BorderRuleException;

import client.halfmaplogic.validation.exception.HalfMapGenerationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class BorderRule implements HalfMapValidationRule {
    private static final Logger logger = LoggerFactory.getLogger(BorderRule.class);

    private static final XYPair HALF_MAP_SIZE = new XYPair(10, 5);
    private static final XYPair REQUIRED_TRAVERSABLE_SIDE = new XYPair(6, 3);
    private static final XYPair REQUIRED_NON_TRAVERSABLE_SIDE = new XYPair(0, 0);

    private static final XYPair TOP_LEFT_CORNER = new XYPair(0, 0);
    private static final XYPair TOP_RIGHT_CORNER = new XYPair(HALF_MAP_SIZE.x() - 1, 0);
    private static final XYPair BOTTOM_LEFT_CORNER = new XYPair(0, HALF_MAP_SIZE.y() - 1);
    private static final XYPair BOTTOM_RIGHT_CORNER = new XYPair(HALF_MAP_SIZE.x() - 1, HALF_MAP_SIZE.y() - 1);

    @Override
    public List<HalfMapGenerationException> validate(HalfMap halfMap) {
        if (halfMap == null)
            throw new IllegalArgumentException("halfMap is null");

        List<HalfMapGenerationException> errors = new ArrayList<>();

        if (invalidBorder(halfMap, TOP_LEFT_CORNER, TOP_RIGHT_CORNER, REQUIRED_TRAVERSABLE_SIDE.x(), REQUIRED_NON_TRAVERSABLE_SIDE.x()))
            errors.add(new BorderRuleException("Top border (y=0) violation."));

        if (invalidBorder(halfMap, BOTTOM_LEFT_CORNER, BOTTOM_RIGHT_CORNER, REQUIRED_TRAVERSABLE_SIDE.x(), REQUIRED_NON_TRAVERSABLE_SIDE.x()))
            errors.add(new BorderRuleException("Bottom border (y=4) violation."));

        if (invalidBorder(halfMap, TOP_LEFT_CORNER, BOTTOM_LEFT_CORNER, REQUIRED_TRAVERSABLE_SIDE.y(), REQUIRED_NON_TRAVERSABLE_SIDE.y()))
            errors.add(new BorderRuleException("Left border (x=0) violation."));

        if (invalidBorder(halfMap, TOP_RIGHT_CORNER, BOTTOM_RIGHT_CORNER, REQUIRED_TRAVERSABLE_SIDE.y(), REQUIRED_NON_TRAVERSABLE_SIDE.y()))
            errors.add(new BorderRuleException("Right border (x=9) violation."));

        return errors;
    }

    private static boolean invalidBorder(HalfMap halfMap, XYPair start, XYPair end,
                                         int requiredTraversableCount, int requiredNonTraversableCount) {
        int traversableCount = 0;
        int nonTraversableCount = 0;
        var delta = new XYPair(Integer.compare(end.x(), start.x()), Integer.compare(end.y(), start.y()));
        int length = Math.max(end.x() - start.x(), end.y() - start.y()) + 1;

        for (int i = 0; i < length; ++i) {
            var coordinate = new XYPair(start.x() + i * delta.x(), start.y() + i * delta.y());
            if (halfMap.isWater(coordinate))
                ++nonTraversableCount;
            else
                ++traversableCount;
        }

        return traversableCount < requiredTraversableCount || nonTraversableCount < requiredNonTraversableCount;
    }
}