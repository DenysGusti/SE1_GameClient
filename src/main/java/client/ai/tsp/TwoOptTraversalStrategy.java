package client.ai.tsp;

import client.ai.graph.FullMapGraph;
import client.data.XYPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class TwoOptTraversalStrategy implements NodeTraversalStrategy {
    private static final Logger logger = LoggerFactory.getLogger(TwoOptTraversalStrategy.class);

    private final NodeTraversalStrategy initialStrategy;
    private final TwoOptHelper twoOptHelper;

    public TwoOptTraversalStrategy(NodeTraversalStrategy initialStrategy, TwoOptHelper twoOptHelper) {
        if (initialStrategy == null)
            throw new IllegalArgumentException("initialStrategy must not be null");
        if (twoOptHelper == null)
            throw new IllegalArgumentException("twoOptHelper must not be null");

        this.initialStrategy = initialStrategy;
        this.twoOptHelper = twoOptHelper;
    }

    @Override
    public TraversalResult computePath(FullMapGraph fullMapGraph, XYPair start, Set<XYPair> nodes) {
        long startTime = System.nanoTime();
        logger.debug("Starting 2-Opt for {} nodes...", nodes.size());

        TraversalResult initialTraversalResult = initialStrategy.orderNodes(fullMapGraph, start, nodes);

        List<XYPair> currentPath = new ArrayList<>(initialTraversalResult.path());
        int currentDistance = initialTraversalResult.distance();

        int iteration = 0;

        for (boolean improvementMade = true; improvementMade; ++iteration) {
            improvementMade = false;

            // we start at i = 1 because the start node is fixed
            // we go up to size - 2 because we need at least one edge after i to swap
            for (int i = 1; i < currentPath.size() - 1; ++i) {
                for (int j = i + 1; j < currentPath.size(); ++j) {
                    int delta = twoOptHelper.calculateDelta(fullMapGraph, currentPath, i, j);
                    int newDistance = currentDistance + delta;

                    if (newDistance < currentDistance) {
                        logger.trace("Improvement at iteration {}: distance reduced from {} to {}",
                                iteration, currentDistance, newDistance);

                        twoOptHelper.performSwap(currentPath, i, j);

                        currentDistance = newDistance;
                        improvementMade = true;
                        break;
                    }
                }
                if (improvementMade)
                    break;
            }
        }

        long endTime = System.nanoTime();
        double duration = (endTime - startTime) / 1_000_000_000.;
        logger.debug("2-Opt finished: iterations: {}, time: {}s, distance: {}",
                iteration, duration, currentDistance);

        return new TraversalResult(currentPath, currentDistance);
    }
}