package client.ai;

import client.data.XYPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class TwoOptTraversalStrategy implements NodeTraversalStrategy {
    private static final Logger logger = LoggerFactory.getLogger(TwoOptTraversalStrategy.class);
    private static final int MAX_ITERATIONS = 1000;

    private final FullMapGraph fullMapGraph;
    private final NodeTraversalStrategy baselineStrategy;

    public TwoOptTraversalStrategy(FullMapGraph fullMapGraph, NodeTraversalStrategy baselineStrategy) {
        if (fullMapGraph == null)
            throw new IllegalArgumentException("fullMapGraph must not be null");

        if (baselineStrategy == null)
            throw new IllegalArgumentException("baselineStrategy must not be null");

        this.fullMapGraph = fullMapGraph;
        this.baselineStrategy = baselineStrategy;
    }

    @Override
    public List<XYPair> orderNodes(XYPair start, Set<XYPair> nodes) {
        if (start == null)
            throw new IllegalArgumentException("start must not be null");
        if (nodes == null)
            throw new IllegalArgumentException("nodes must not be null");

        logger.debug("Starting 2-Opt optimization for {} nodes (Max Iterations: {})", nodes.size(), MAX_ITERATIONS);

        List<XYPair> currentPath = Objects.requireNonNull(baselineStrategy.orderNodes(start, nodes), "currentPath must not be null");
        if (currentPath.size() <= 1)
            throw new RuntimeException("currentPath must have at least start and end");

        boolean improvementMade = true;
        int iterations = 0;

        while (improvementMade && iterations < MAX_ITERATIONS) {
            improvementMade = false;
            int currentDistance = fullMapGraph.getDistance(currentPath);

            // we start at i = 1 because the start node is fixed
            // we go up to size - 2 because we need at least one edge after i to swap
            for (int i = 1; i < currentPath.size() - 1; ++i) {
                for (int j = i + 1; j < currentPath.size(); ++j) {
                    List<XYPair> newPath = twoOptSwap(currentPath, i, j);
                    int newDistance = fullMapGraph.getDistance(newPath);

                    if (newDistance < currentDistance) {
                        logger.trace("2-Opt improvement at iter {}: distance reduced from {} to {}", iterations, currentDistance, newDistance);

                        currentPath = newPath;
                        improvementMade = true;
                        break;
                    }
                }
                if (improvementMade)
                    break;
            }
            ++iterations;
        }

        if (iterations >= MAX_ITERATIONS)
            throw new RuntimeException("Too many iterations");

        logger.debug("2-Opt finished in {} iterations. Final Path Size: {}", iterations, currentPath.size());
        return currentPath;
    }

    // reverses the segment of the path between indices i and j (inclusive).
    // [a, b, c, d, e], i=1, j=3 -> [a, d, c, b, e]
    private List<XYPair> twoOptSwap(List<XYPair> path, int i, int j) {
        if (path == null)
            throw new IllegalArgumentException("path must not be null");
        if (i < 0 || i >= path.size() || j < 0 || j >= path.size())
            throw new IndexOutOfBoundsException("index out of bounds");
        if (i >= j)
            throw new IllegalArgumentException("i must not less than j");

        List<XYPair> newPath = new ArrayList<>(path.subList(0, i));

        List<XYPair> segment = new ArrayList<>(path.subList(i, j + 1));
        Collections.reverse(segment);
        newPath.addAll(segment);
        newPath.addAll(path.subList(j + 1, path.size()));

        return newPath;
    }
}