package client.ai.tsp;

import client.ai.FullMapGraph;
import client.data.XYPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SimulatedAnnealingTraversalStrategy implements NodeTraversalStrategy {
    private static final Logger logger = LoggerFactory.getLogger(SimulatedAnnealingTraversalStrategy.class);

    // very hot
    private static final double STARTING_TEMPERATURE = 1.5;
    private static final double COOLING_RATE = 0.999999;
    private static final double MIN_TEMPERATURE = 1;

    private final NodeTraversalStrategy initialStrategy;
    private final Random random;
    private final TwoOptHelper twoOptHelper;

    public SimulatedAnnealingTraversalStrategy(NodeTraversalStrategy initialStrategy, Random random, TwoOptHelper twoOptHelper) {
        if (initialStrategy == null)
            throw new IllegalArgumentException("initialStrategy must not be null");
        if (random == null)
            throw new IllegalArgumentException("random must not be null");
        if (twoOptHelper == null)
            throw new IllegalArgumentException("twoOptHelper must not be null");

        this.initialStrategy = initialStrategy;
        this.random = random;
        this.twoOptHelper = twoOptHelper;
    }

    @Override
    public TraversalResult computePath(FullMapGraph fullMapGraph, XYPair start, Set<XYPair> nodes) {
        long startTime = System.nanoTime();
        logger.debug("Simulated Annealing started for {} nodes...", nodes.size());

        TraversalResult initialResult = initialStrategy.orderNodes(fullMapGraph, start, nodes);

        List<XYPair> currentPath = new ArrayList<>(initialResult.path());
        int currentDistance = initialResult.distance();

        if (currentPath.size() <= 2)
            return new TraversalResult(currentPath, currentDistance);

        List<XYPair> bestPath = new ArrayList<>(currentPath);
        int bestDistance = currentDistance;

        int iteration = 0;

        for (double temperature = STARTING_TEMPERATURE; temperature > MIN_TEMPERATURE; temperature *= COOLING_RATE) {
            int i = 1 + random.nextInt(currentPath.size() - 1);
            int j;
            do {
                j = 1 + random.nextInt(currentPath.size() - 1);
            } while (i == j);

            if (i > j) {
                int temp = i;
                i = j;
                j = temp;
            }

            int delta = twoOptHelper.calculateDelta(fullMapGraph, currentPath, i, j);
            int newDistance = currentDistance + delta;

            if (acceptanceProbability(currentDistance, newDistance, temperature) > random.nextDouble()) {
                twoOptHelper.performSwap(currentPath, i, j);
                currentDistance = newDistance;

                if (currentDistance < bestDistance) {
                    logger.trace("Improvement at iteration {}: distance reduced from {} to {}",
                            iteration, bestDistance, currentDistance);

                    bestPath = new ArrayList<>(currentPath);
                    bestDistance = currentDistance;
                }
            }

            ++iteration;
        }

        long endTime = System.nanoTime();
        double duration = (endTime - startTime) / 1_000_000_000.;
        logger.debug("Simulated Annealing finished: iterations: {}, time: {}s, distance: {}",
                iteration, duration, bestDistance);

        return new TraversalResult(bestPath, bestDistance);
    }

    private double acceptanceProbability(int currentDistance, int newDistance, double temperature) {
        if (currentDistance > newDistance)
            return 1.;
        return Math.exp((currentDistance - newDistance) / temperature);
    }
}