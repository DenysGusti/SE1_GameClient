package client.ai.tsp;

import client.ai.FullMapGraph;
import client.data.XYPair;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class RandomWalkTraversalStrategy implements NodeTraversalStrategy {
    private static final Logger logger = LoggerFactory.getLogger(RandomWalkTraversalStrategy.class);

    private final Random random;

    public RandomWalkTraversalStrategy(Random random) {
        if (random == null)
            throw new IllegalArgumentException("random must not be null");

        this.random = random;
    }

    @Override
    public TraversalResult computePath(FullMapGraph fullMapGraph, XYPair start, Set<XYPair> nodes) {
        long startTime = System.nanoTime();
        logger.debug("Random Walk started for {} nodes...", nodes.size());

        List<XYPair> targets = new ArrayList<>(nodes);

        Collections.shuffle(targets, random);

        List<XYPair> path = new ArrayList<>();
        path.add(start);
        path.addAll(targets);

        int distance = fullMapGraph.getDistance(path);

        long endTime = System.nanoTime();
        double duration = (endTime - startTime) / 1_000_000_000.;
        logger.debug("Random Walk finished: time: {}s, distance: {}", duration, distance);

        return new TraversalResult(path, distance);
    }
}