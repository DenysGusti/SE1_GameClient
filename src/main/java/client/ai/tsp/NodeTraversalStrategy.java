package client.ai.tsp;

import client.ai.graph.FullMapGraph;
import client.data.XYPair;

import java.util.List;
import java.util.Set;

public interface NodeTraversalStrategy {
    record TraversalResult(List<XYPair> path, int distance) {
        public TraversalResult {
            if (path == null)
                throw new IllegalArgumentException("path must not be null");
            if (distance < 0)
                throw new IllegalArgumentException("distance must not be negative");
        }
    }

    default TraversalResult orderNodes(FullMapGraph fullMapGraph, XYPair start, Set<XYPair> nodes) {
        if (fullMapGraph == null)
            throw new IllegalArgumentException("fullMapGraph must not be null");
        if (start == null)
            throw new IllegalArgumentException("start must not be null");
        if (nodes == null)
            throw new IllegalArgumentException("nodes must not be null");
        if (nodes.contains(start))
            throw new IllegalArgumentException("nodes must not contain start");

        return computePath(fullMapGraph, start, nodes);
    }

    TraversalResult computePath(FullMapGraph fullMapGraph, XYPair start, Set<XYPair> nodes);
}
