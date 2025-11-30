package client.ai.tsp;

import client.ai.FullMapGraph;
import client.data.XYPair;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class NearestNeighbourTraversalStrategy implements NodeTraversalStrategy {
    private static final Logger logger = LoggerFactory.getLogger(NearestNeighbourTraversalStrategy.class);

    private final FullMapGraph fullMapGraph;

    public NearestNeighbourTraversalStrategy(FullMapGraph fullMapGraph) {
        if (fullMapGraph == null)
            throw new IllegalArgumentException("fullMapGraph must not be null");

        this.fullMapGraph = fullMapGraph;
    }

    @Override
    public List<XYPair> orderNodes(XYPair start, Set<XYPair> nodes) {
        if (start == null)
            throw new IllegalArgumentException("start must not be null");
        if (nodes == null)
            throw new IllegalArgumentException("nodes must not be null");
        if (nodes.contains(start))
            throw new IllegalArgumentException("nodes contains start");

        long startTime = System.nanoTime();

        logger.debug("Starting Nearest Neighbour calculation from {} to visit {} nodes", start, nodes.size());

        Set<XYPair> remainingNodes = new HashSet<>(nodes);
        List<XYPair> orderedPath = new ArrayList<>();
        orderedPath.add(start);
        XYPair current = start;

        while (!remainingNodes.isEmpty()) {
            XYPair nearest = getNearestNode(current, remainingNodes);
            logger.trace("Nearest node to {} is {}", current, nearest);

            orderedPath.add(nearest);
            remainingNodes.remove(nearest);
            current = nearest;
        }

        logger.debug("Path calculation finished. Final path size: {}", orderedPath.size());
        logger.debug("Final Path Distance: {}", fullMapGraph.getDistance(orderedPath));
        logger.debug("Path: {}", orderedPath);

        long endTime = System.nanoTime();
        long durationNs = endTime - startTime;
        double durationMs = durationNs / 1_000_000_000.;
        logger.info("Nearest Neighbour finished in {} s", String.format("%.4f", durationMs));

        return orderedPath;
    }

    private XYPair getNearestNode(XYPair current, Set<XYPair> remainingNodes) {
        return remainingNodes.stream()
                .min(Comparator.comparingInt(node -> fullMapGraph.getDistance(current, node)))
                .orElseThrow();
    }
}