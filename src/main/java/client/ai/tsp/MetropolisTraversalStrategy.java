package client.ai.tsp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class MetropolisTraversalStrategy implements NodeTraversalStrategy, TwoOpt {
    private static final Logger logger = LoggerFactory.getLogger(MetropolisTraversalStrategy.class);
    private static final int NUMBER_OF_ITERATIONS = 2_000_000;

    private final NodeTraversalStrategy initialStrategy;
    private final Random random;

    public MetropolisTraversalStrategy(NodeTraversalStrategy initialStrategy, Random random) {
        if (initialStrategy == null)
            throw new IllegalArgumentException("initialStrategy is null");
        if (random == null)
            throw new IllegalArgumentException("random is null");

        this.initialStrategy = initialStrategy;
        this.random = random;
    }

    @Override
    public int[] computePath(byte[][] distanceMatrix) {
        long startTime = System.nanoTime();
        int n = distanceMatrix.length;
        logger.debug("Simulated Annealing started for {} nodes...", n);

        int[] currentPath = initialStrategy.computePath(distanceMatrix);
        int currentDistance = calculateTotalDistance(distanceMatrix, currentPath);

        if (currentPath.length <= 2)
            return currentPath;

        int[] bestPath = Arrays.copyOf(currentPath, n);
        int bestDistance = currentDistance;

        int iteration = 0;

        for (; iteration < NUMBER_OF_ITERATIONS; ++iteration) {
            int i = 1 + random.nextInt(n - 1);
            int j;
            do {
                j = 1 + random.nextInt(n - 1);
            } while (i == j);

            if (i > j) {
                int tmp = i;
                i = j;
                j = tmp;
            }

            int delta = calculateDelta(distanceMatrix, currentPath, i, j);
            if (delta < 0 || Math.exp(-delta) > random.nextDouble()) {
                reverseSegment(currentPath, i, j);
                currentDistance += delta;

                if (currentDistance < bestDistance) {
//                    logger.trace("Improvement at iteration {}: distance: {} -> {}",
//                            iteration, bestDistance, currentDistance);

                    System.arraycopy(currentPath, 0, bestPath, 0, n);
                    bestDistance = currentDistance;
                }
            }
        }

        double duration = (System.nanoTime() - startTime) / 1_000_000_000.;
        logger.debug("Simulated Annealing finished in {}s: iterations: {}, distance: {}", duration, iteration,
                bestDistance);
        return bestPath;
    }
}