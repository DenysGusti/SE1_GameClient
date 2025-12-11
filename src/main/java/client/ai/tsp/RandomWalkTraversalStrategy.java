package client.ai.tsp;

import client.ai.graph.DistanceMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.random.RandomGenerator;

public class RandomWalkTraversalStrategy implements NodeTraversalStrategy {
    private static final Logger logger = LoggerFactory.getLogger(RandomWalkTraversalStrategy.class);

    private final RandomGenerator randomGenerator;

    public RandomWalkTraversalStrategy(RandomGenerator randomGenerator) {
        if (randomGenerator == null)
            throw new IllegalArgumentException("random is null");

        this.randomGenerator = randomGenerator;
    }

    @Override
    public int[] computePath(DistanceMatrix distanceMatrix) {
        long startTime = System.nanoTime();
        logger.debug("Random Walk started for {} nodes...", distanceMatrix.size());

        var path = new int[distanceMatrix.size()];
        for (int i = 0; i < distanceMatrix.size(); ++i)
            path[i] = i;

        for (int i = distanceMatrix.size() - 1; i > 1; --i) {
            int j = 1 + randomGenerator.nextInt(i);  // start (0) is fixed
            int tmp = path[i];
            path[i] = path[j];
            path[j] = tmp;
        }

        double duration = (System.nanoTime() - startTime) / 1_000_000_000.;
        logger.debug("Random Walk finished in {}s, distance: {}", duration,
                distanceMatrix.calculateTotalDistance(path));
        return path;
    }
}