package client.ai.tsp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class RandomWalkTraversalStrategy implements NodeTraversalStrategy {
    private static final Logger logger = LoggerFactory.getLogger(RandomWalkTraversalStrategy.class);

    private final Random random;

    public RandomWalkTraversalStrategy(Random random) {
        if (random == null)
            throw new IllegalArgumentException("random is null");

        this.random = random;
    }

    @Override
    public int[] computePath(byte[][] distanceMatrix) {
        long startTime = System.nanoTime();
        int n = distanceMatrix.length;
        logger.debug("Random Walk started for {} nodes...", n);

        var path = new int[n];
        for (int i = 0; i < n; ++i)
            path[i] = i;

        for (int i = n - 1; i > 1; --i) {
            int j = 1 + random.nextInt(i);  // start (0) is fixed
            int tmp = path[i];
            path[i] = path[j];
            path[j] = tmp;
        }

        double duration = (System.nanoTime() - startTime) / 1_000_000_000.;
        logger.debug("Random Walk finished in {}s, distance: {}", duration,
                calculateTotalDistance(distanceMatrix, path));
        return path;
    }
}