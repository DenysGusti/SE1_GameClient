package client.ai.tsp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TwoOptTraversalStrategy implements NodeTraversalStrategy, TwoOpt {
    private static final Logger logger = LoggerFactory.getLogger(TwoOptTraversalStrategy.class);

    private final NodeTraversalStrategy initialStrategy;

    public TwoOptTraversalStrategy(NodeTraversalStrategy initialStrategy) {
        if (initialStrategy == null)
            throw new IllegalArgumentException("initialStrategy is null");

        this.initialStrategy = initialStrategy;
    }

    @Override
    public int[] computePath(byte[][] distanceMatrix) {
        long startTime = System.nanoTime();
        int n = distanceMatrix.length;
        logger.debug("2-Opt started for {} nodes...", n);

        int[] path = initialStrategy.computePath(distanceMatrix);

        int iteration = 0;
        for (boolean improvementMade = true; improvementMade; ++iteration) {
            improvementMade = false;

            // we start at i = 1 because the start node (0) is fixed
            // we go up to size - 2 because we need at least one edge j after i to swap
            for (int i = 1; i < n - 1; ++i)
                for (int j = i + 1; j < n; ++j) {
                    int delta = calculateDelta(distanceMatrix, path, i, j);

                    if (delta < 0) {
                        throw new RuntimeException("test");
//                        reverseSegment(path, i, j);
//                        improvementMade = true;
                    }
                }
        }

        double duration = (System.nanoTime() - startTime) / 1_000_000_000.;
        logger.debug("2-Opt finished in {}s: iterations: {}, distance: {}", duration, iteration,
                calculateTotalDistance(distanceMatrix, path));
        return path;
    }
}