package client.ai.tsp;

import client.ai.graph.DistanceMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.random.RandomGenerator;

public class MetropolisStrategy implements NodeTraversalStrategy {
    private static final Logger logger = LoggerFactory.getLogger(MetropolisStrategy.class);
    private static final int ITERATIONS_PER_NODE = 50_000;
    private static final int RESTART_PERIOD = 500_000;
    private static final double[] DELTA_ACCEPTANCE_THRESHOLD = new double[64];

    static {
        for (int i = 0; i < DELTA_ACCEPTANCE_THRESHOLD.length; ++i)
            DELTA_ACCEPTANCE_THRESHOLD[i] = Math.exp(-i);
    }

    private final NodeTraversalStrategy initialStrategy;
    private final RandomGenerator randomGenerator;
    private final TwoOptUtils twoOptUtils;

    /*
     because this algorithm is random and heuristic, there is a bug with really stupid paths
     HalfMapGenerator with seed -8929787966741954084, Metropolis with seed 1724325189346784055
     */
    public MetropolisStrategy(NodeTraversalStrategy initialStrategy, RandomGenerator randomGenerator, TwoOptUtils twoOptUtils) {
        if (initialStrategy == null)
            throw new IllegalArgumentException("initialStrategy is null");
        if (randomGenerator == null)
            throw new IllegalArgumentException("randomGenerator is null");
        if (twoOptUtils == null)
            throw new IllegalArgumentException("twoOptUtils is null");

        this.initialStrategy = initialStrategy;
        this.randomGenerator = randomGenerator;
        this.twoOptUtils = twoOptUtils;
    }

    @Override
    public int[] computePath(DistanceMatrix distanceMatrix) {
        long startTime = System.nanoTime();
        logger.debug("Metropolis started for {} nodes...", distanceMatrix.size());

        int[] currentPath = initialStrategy.computePath(distanceMatrix);
        int currentDistance = distanceMatrix.calculateTotalDistance(currentPath);

        if (currentPath.length <= 2)
            return currentPath;

        int n = distanceMatrix.size();
        int[] bestPath = Arrays.copyOf(currentPath, n);
        int bestDistance = currentDistance;

        int numberOfIterations = n * ITERATIONS_PER_NODE;
        for (int iteration = 0; iteration < numberOfIterations; ++iteration) {
            if (iteration % RESTART_PERIOD == 0) {
                System.arraycopy(bestPath, 0, currentPath, 0, n);
                currentDistance = bestDistance;
            }

            int i = 1 + randomGenerator.nextInt(n - 2);
            int j = 1 + i + randomGenerator.nextInt(n - i - 1);

            int delta = twoOptUtils.calculateDelta(distanceMatrix, currentPath, i, j);
            if (delta < 0) {
                twoOptUtils.reverseSegment(currentPath, i, j);
                currentDistance += delta;

                if (currentDistance < bestDistance) {
//                    logger.trace("Improvement at iteration {}: distance: {} -> {}",
//                            iteration, bestDistance, currentDistance);

                    System.arraycopy(currentPath, 0, bestPath, 0, n);
                    bestDistance = currentDistance;
                }
            } else if (delta < DELTA_ACCEPTANCE_THRESHOLD.length)
                if (DELTA_ACCEPTANCE_THRESHOLD[delta] > randomGenerator.nextDouble()) {
                    twoOptUtils.reverseSegment(currentPath, i, j);
                    currentDistance += delta;
                }
        }

        double duration = (System.nanoTime() - startTime) / 1_000_000_000.;
        logger.debug("Metropolis finished in {}s: iterations: {}, distance: {}",
                duration, numberOfIterations, bestDistance);
        return bestPath;
    }
}