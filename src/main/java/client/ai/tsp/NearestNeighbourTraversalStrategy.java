package client.ai.tsp;

import client.ai.graph.FullMapGraph;
import client.data.XYPair;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class NearestNeighbourTraversalStrategy implements NodeTraversalStrategy {
    private static final Logger logger = LoggerFactory.getLogger(NearestNeighbourTraversalStrategy.class);

    @Override
    public TraversalResult computePath(FullMapGraph fullMapGraph, XYPair start, Set<XYPair> nodes) {
        long startTime = System.nanoTime();
        logger.debug("Nearest Neighbour started for {} nodes...", nodes.size());

        Set<XYPair> remainingNodes = new HashSet<>(nodes);
        List<XYPair> orderedPath = new ArrayList<>();
        orderedPath.add(start);
        XYPair current = start;

        while (!remainingNodes.isEmpty()) {
            XYPair nearest = getNearestNode(fullMapGraph, current, remainingNodes);

            orderedPath.add(nearest);
            remainingNodes.remove(nearest);
            current = nearest;
        }

        int distance = fullMapGraph.getDistance(orderedPath);

        long endTime = System.nanoTime();
        double duration = (endTime - startTime) / 1_000_000_000.;
        logger.debug("Nearest Neighbour finished: time: {}s, distance: {}", duration, distance);

        return new TraversalResult(orderedPath, distance);
    }

    private XYPair getNearestNode(FullMapGraph fullMapGraph, XYPair current, Set<XYPair> remainingNodes) {
        return remainingNodes.stream()
                .min(Comparator.comparingInt(node -> fullMapGraph.getDistance(current, node)))
                .orElseThrow();
    }
}