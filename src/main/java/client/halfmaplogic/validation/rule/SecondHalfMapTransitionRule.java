package client.halfmaplogic.validation.rule;

import client.data.ETerrain;
import client.data.XYPair;
import client.data.fromclient.HalfMap;
import client.data.fromserver.FullMap;
import client.data.fromserver.FullMapNode;
import client.halfmaplogic.validation.exception.BorderRuleException;
import client.halfmaplogic.validation.exception.HalfMapGenerationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SecondHalfMapTransitionRule implements HalfMapValidationRule {
    private static final Logger logger = LoggerFactory.getLogger(SecondHalfMapTransitionRule.class);

    private static final XYPair HALF_MAP_SIZE = new XYPair(10, 5);
    private static final XYPair REQUIRED_TRAVERSABLE_SIDE = new XYPair(4, 2);

    private static final XYPair TOP_LEFT_CORNER = new XYPair(0, 0);
    private static final XYPair TOP_RIGHT_CORNER = new XYPair(HALF_MAP_SIZE.x() - 1, 0);
    private static final XYPair BOTTOM_LEFT_CORNER = new XYPair(0, HALF_MAP_SIZE.y() - 1);
    private static final XYPair BOTTOM_RIGHT_CORNER = new XYPair(HALF_MAP_SIZE.x() - 1, HALF_MAP_SIZE.y() - 1);

    private final ETerrain[] opponentTopBorder = new ETerrain[HALF_MAP_SIZE.x()];
    private final ETerrain[] opponentBottomBorder = new ETerrain[HALF_MAP_SIZE.x()];
    private final ETerrain[] opponentLeftBorder = new ETerrain[HALF_MAP_SIZE.y()];
    private final ETerrain[] opponentRightBorder = new ETerrain[HALF_MAP_SIZE.y()];

    public SecondHalfMapTransitionRule(FullMap fullMap) {
        if (fullMap == null)
            throw new IllegalArgumentException("fullMap is null");
        if (fullMap.nodes().size() != 50)
            throw new HalfMapGenerationException("fullMap must have exactly 50 nodes");

        Map<XYPair, FullMapNode> nodes = fullMap.nodes();
        XYPair topLeft = fullMap.getOptionalTopLeftCoordinate().orElseThrow();

        for (int x = 0; x < HALF_MAP_SIZE.x(); ++x) {
            var coordinate = new XYPair(topLeft.x() + x, topLeft.y());
            FullMapNode node = nodes.get(coordinate);
            opponentTopBorder[x] = node.terrain();
        }
        for (int x = 0; x < HALF_MAP_SIZE.x(); ++x) {
            var coordinate = new XYPair(topLeft.x() + x, topLeft.y() + HALF_MAP_SIZE.y() - 1);
            FullMapNode node = nodes.get(coordinate);
            opponentBottomBorder[x] = node.terrain();
        }
        for (int y = 0; y < HALF_MAP_SIZE.y(); ++y) {
            var coordinate = new XYPair(topLeft.x(), topLeft.y() + y);
            FullMapNode node = nodes.get(coordinate);
            opponentLeftBorder[y] = node.terrain();
        }
        for (int y = 0; y < HALF_MAP_SIZE.y(); ++y) {
            var coordinate = new XYPair(topLeft.x() + HALF_MAP_SIZE.x() - 1, topLeft.y() + y);
            FullMapNode node = nodes.get(coordinate);
            opponentRightBorder[y] = node.terrain();
        }
    }

    @Override
    public List<HalfMapGenerationException> validate(HalfMap halfMap) {
        if (halfMap == null)
            throw new IllegalArgumentException("halfMap is null");

        List<HalfMapGenerationException> errors = new ArrayList<>();

        if (invalidBorder(halfMap, TOP_LEFT_CORNER, TOP_RIGHT_CORNER, opponentBottomBorder, REQUIRED_TRAVERSABLE_SIDE.x()))
            errors.add(new BorderRuleException("BorderRule: Top border (y=0) violation."));

        if (invalidBorder(halfMap, BOTTOM_LEFT_CORNER, BOTTOM_RIGHT_CORNER, opponentTopBorder, REQUIRED_TRAVERSABLE_SIDE.x()))
            errors.add(new BorderRuleException("BorderRule: Bottom border (y=4) violation."));

        if (invalidBorder(halfMap, TOP_LEFT_CORNER, BOTTOM_LEFT_CORNER, opponentRightBorder, REQUIRED_TRAVERSABLE_SIDE.y()))
            errors.add(new BorderRuleException("BorderRule: Left border (x=0) violation."));

        if (invalidBorder(halfMap, TOP_RIGHT_CORNER, BOTTOM_RIGHT_CORNER, opponentLeftBorder, REQUIRED_TRAVERSABLE_SIDE.y()))
            errors.add(new BorderRuleException("BorderRule: Right border (x=9) violation."));

        return errors;
    }

    private static boolean invalidBorder(HalfMap halfMap, XYPair start, XYPair end, ETerrain[] opponentBorder,
                                         int requiredTraversableCount) {
        int traversableCount = 0;
        var delta = new XYPair(Integer.compare(end.x(), start.x()), Integer.compare(end.y(), start.y()));
        int length = Math.max(end.x() - start.x(), end.y() - start.y()) + 1;

        for (int i = 0; i < length; ++i) {
            var coordinate = new XYPair(start.x() + i * delta.x(), start.y() + i * delta.y());
            ETerrain myTerrain = halfMap.nodes().get(coordinate);
            ETerrain opponentTerrain = opponentBorder[i];

            if (myTerrain != ETerrain.Water && opponentTerrain != ETerrain.Water)
                ++traversableCount;
        }

        return traversableCount < requiredTraversableCount;
    }
}