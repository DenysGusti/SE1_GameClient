package client.ai.tsp;

import client.ai.graph.DistanceMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeneralNodeTraversalStrategy implements NodeTraversalStrategy {
    private static final Logger logger = LoggerFactory.getLogger(GeneralNodeTraversalStrategy.class);
    private static final int EXACT_STRATEGY_NODES_THRESHOLD = 20;

    private final NodeTraversalStrategy exactStrategy;
    private final NodeTraversalStrategy heuristicStrategy;
    private final NodeTraversalStrategy heuristicStrategyBackup;

    public GeneralNodeTraversalStrategy(NodeTraversalStrategy exactStrategy, NodeTraversalStrategy heuristicStrategy, NodeTraversalStrategy heuristicStrategyBackup) {
        if (exactStrategy == null)
            throw new IllegalArgumentException("exactStrategy is null");
        if (heuristicStrategy == null)
            throw new IllegalArgumentException("heuristicStrategy is null");
        if (heuristicStrategyBackup == null)
            throw new IllegalArgumentException("heuristicStrategyBackup is null");

        this.exactStrategy = exactStrategy;
        this.heuristicStrategy = heuristicStrategy;
        this.heuristicStrategyBackup = heuristicStrategyBackup;
    }

    @Override
    public int[] computePath(DistanceMatrix distanceMatrix) {
        long startTime = System.nanoTime();
        logger.debug("General started for {} nodes...", distanceMatrix.size());

        logger.debug("Exact strategy nodes threshold: {}", EXACT_STRATEGY_NODES_THRESHOLD);

        int[] resultPath;
        int resultPathDistance;

        if (distanceMatrix.size() <= EXACT_STRATEGY_NODES_THRESHOLD) {
            logger.debug("Using exact strategy");
            resultPath = exactStrategy.computePath(distanceMatrix);
            resultPathDistance = distanceMatrix.calculateTotalDistance(resultPath);
        } else {
            logger.debug("Using heuristic strategy");
            resultPath = heuristicStrategy.computePath(distanceMatrix);
            resultPathDistance = distanceMatrix.calculateTotalDistance(resultPath);

            logger.debug("Using heuristic strategy again");
            int[] tmp = heuristicStrategyBackup.computePath(distanceMatrix);
            int tmpDistance = distanceMatrix.calculateTotalDistance(tmp);

            if (resultPathDistance != tmpDistance)
                throw new RuntimeException("Heuristic strategies not good enough! M: "
                        + resultPathDistance + " != SA: " + tmpDistance);
        }

        double duration = (System.nanoTime() - startTime) / 1_000_000_000.;
        logger.debug("General finished in {}s: distance: {}", duration, resultPathDistance);
        return resultPath;
    }
}