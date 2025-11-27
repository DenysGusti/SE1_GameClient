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

        logger.debug("Starting Nearest Neighbour calculation from {} to visit {} nodes", start, nodes.size());

        Set<XYPair> remaining = new HashSet<>(nodes);
        List<XYPair> orderedPath = new ArrayList<>();
        orderedPath.add(start);
        XYPair current = start;

        while (!remaining.isEmpty()) {
            XYPair nearest = getNearest(current, remaining);
            logger.trace("Nearest node to {} is {}", current, nearest);

            orderedPath.add(nearest);
            remaining.remove(nearest);
            current = nearest;
        }

        logger.debug("Path calculation finished. Final path size: {}", orderedPath.size());
        logger.debug("Final Path Distance: {}", fullMapGraph.getDistance(orderedPath));
        logger.debug("Path: {}", orderedPath);
        return orderedPath;
    }

    private XYPair getNearest(XYPair current, Set<XYPair> remaining) {
        return remaining.stream()
                .min(Comparator.comparingInt(node -> fullMapGraph.getDistance(current, node)))
                .orElseThrow();
    }
}