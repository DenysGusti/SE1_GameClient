package client.ai.tsp;

import client.ai.graph.DistanceMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.random.RandomGenerator;

public class SimulatedAnnealingStrategy implements NodeTraversalStrategy, TwoOpt {
    private static final Logger logger = LoggerFactory.getLogger(SimulatedAnnealingStrategy.class);

    private static final double STARTING_TEMPERATURE = 100.;
    private static final double COOLING_RATE = 0.999995;
    private static final double MIN_TEMPERATURE = 0.01;

    private final NodeTraversalStrategy initialStrategy;
    private final RandomGenerator randomGenerator;

    public SimulatedAnnealingStrategy(NodeTraversalStrategy initialStrategy, RandomGenerator randomGenerator) {
        if (initialStrategy == null)
            throw new IllegalArgumentException("initialStrategy is null");
        if (randomGenerator == null)
            throw new IllegalArgumentException("randomGenerator is null");

        this.initialStrategy = initialStrategy;
        this.randomGenerator = randomGenerator;
    }

    @Override
    public int[] computePath(DistanceMatrix distanceMatrix) {
        long startTime = System.nanoTime();
        logger.debug("Simulated Annealing started for {} nodes...", distanceMatrix.size());

        int[] currentPath = initialStrategy.computePath(distanceMatrix);
        int currentDistance = distanceMatrix.calculateTotalDistance(currentPath);

        if (currentPath.length <= 2)
            return currentPath;

        int n = distanceMatrix.size();
        int[] bestPath = Arrays.copyOf(currentPath, n);
        int bestDistance = currentDistance;

        int iteration = 0;
        for (double temperature = STARTING_TEMPERATURE; temperature > MIN_TEMPERATURE;
             temperature *= COOLING_RATE, ++iteration) {
            int i = 1 + randomGenerator.nextInt(n - 2);
            int j = 1 + i + randomGenerator.nextInt(n - i - 1);

            int delta = calculateDelta(distanceMatrix, currentPath, i, j);
            if (delta < 0) {
                reverseSegment(currentPath, i, j);
                currentDistance += delta;

                if (currentDistance < bestDistance) {
//                    logger.trace("Improvement at iteration {}: distance: {} -> {}",
//                            iteration, bestDistance, currentDistance);

                    System.arraycopy(currentPath, 0, bestPath, 0, n);
                    bestDistance = currentDistance;
                }
            } else if (Math.exp(-delta / temperature) > randomGenerator.nextDouble()) {
                reverseSegment(currentPath, i, j);
                currentDistance += delta;
            }
        }

        double duration = (System.nanoTime() - startTime) / 1_000_000_000.;
        logger.debug("Simulated Annealing finished in {}s: iterations: {}, distance: {}", duration, iteration, bestDistance);
        return bestPath;
    }
}