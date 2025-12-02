package client.ai.tsp;

import client.ai.graph.FullMapGraph;
import client.data.XYPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class GeneralNodeTraversalStrategy implements NodeTraversalStrategy {
    private static final Logger logger = LoggerFactory.getLogger(GeneralNodeTraversalStrategy.class);
    private static final int EXACT_STRATEGY_NODES_THRESHOLD = 17;

    private final NodeTraversalStrategy exactStrategy;
    private final NodeTraversalStrategy heuristicStrategy;

    public GeneralNodeTraversalStrategy(NodeTraversalStrategy exactStrategy, NodeTraversalStrategy heuristicStrategy) {
        if (exactStrategy == null)
            throw new IllegalArgumentException("exactStrategy must not be null");
        if (heuristicStrategy == null)
            throw new IllegalArgumentException("heuristicStrategy must not be null");

        this.exactStrategy = exactStrategy;
        this.heuristicStrategy = heuristicStrategy;
    }

    @Override
    public TraversalResult computePath(FullMapGraph fullMapGraph, XYPair start, Set<XYPair> nodes) {
        long startTime = System.nanoTime();
        logger.debug("General started for {} nodes...", nodes.size());
        logger.debug("Nodes size ({}), exact strategy nodes threshold ({})",
                nodes.size(), EXACT_STRATEGY_NODES_THRESHOLD);

        TraversalResult traversalResult;
        if (nodes.size() <= EXACT_STRATEGY_NODES_THRESHOLD) {
            logger.debug("Using exact strategy");
            traversalResult = exactStrategy.orderNodes(fullMapGraph, start, nodes);
        } else {
            logger.debug("Using heuristic strategy");
            traversalResult = heuristicStrategy.orderNodes(fullMapGraph, start, nodes);
        }

        long endTime = System.nanoTime();
        double duration = (endTime - startTime) / 1_000_000_000.;
        logger.debug("General finished: time: {}s, distance: {}", duration, traversalResult.distance());

        return traversalResult;
    }
}
