package client.ai.tsp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeneralNodeTraversalStrategy implements NodeTraversalStrategy {
    private static final Logger logger = LoggerFactory.getLogger(GeneralNodeTraversalStrategy.class);
    private static final int EXACT_STRATEGY_NODES_THRESHOLD = 18;

    private final NodeTraversalStrategy exactStrategy;
    private final NodeTraversalStrategy heuristicStrategy;

    public GeneralNodeTraversalStrategy(NodeTraversalStrategy exactStrategy, NodeTraversalStrategy heuristicStrategy) {
        if (exactStrategy == null)
            throw new IllegalArgumentException("exactStrategy is null");
        if (heuristicStrategy == null)
            throw new IllegalArgumentException("heuristicStrategy is null");

        this.exactStrategy = exactStrategy;
        this.heuristicStrategy = heuristicStrategy;
    }

    @Override
    public int[] computePath(byte[][] distanceMatrix) {
        long startTime = System.nanoTime();
        int n = distanceMatrix.length;
        logger.debug("General started for {} nodes...", n);

        logger.debug("Exact strategy nodes threshold: {}", EXACT_STRATEGY_NODES_THRESHOLD);

        int[] resultPath;
        int resultPathDistance;

        if (n <= EXACT_STRATEGY_NODES_THRESHOLD) {
            logger.debug("Using exact strategy");
            resultPath = exactStrategy.computePath(distanceMatrix);
            resultPathDistance = calculateTotalDistance(distanceMatrix, resultPath);
        } else {
            logger.debug("Using heuristic strategy");
            resultPath = heuristicStrategy.computePath(distanceMatrix);
            resultPathDistance = calculateTotalDistance(distanceMatrix, resultPath);

//            logger.debug("Using heuristic strategy again");
//            int[] tmp = heuristicStrategy.computePath(distanceMatrix);
//            int tmpDistance = calculateTotalDistance(distanceMatrix, tmp);
//
//            if (resultPathDistance != tmpDistance)
//                throw new RuntimeException("Heuristic strategy is not good enough! "
//                        + resultPathDistance + " != " + tmpDistance);
        }

        double duration = (System.nanoTime() - startTime) / 1_000_000_000.;
        logger.debug("General finished in {}s: distance: {}", duration, resultPathDistance);
        return resultPath;
    }
}