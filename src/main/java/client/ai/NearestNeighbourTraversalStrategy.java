package client.ai;

import client.data.XYPair;

import java.util.*;

public class NearestNeighbourTraversalStrategy implements NodeTraversalStrategy {
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

        Set<XYPair> remaining = new HashSet<>(nodes);
        List<XYPair> ordered = new ArrayList<>();
        ordered.add(start);
        XYPair current = start;

        while (!remaining.isEmpty()) {
            XYPair nearest = getNearest(current, remaining);
            ordered.add(nearest);
            remaining.remove(nearest);
            current = nearest;
        }

        return ordered;
    }

    private XYPair getNearest(XYPair current, Set<XYPair> remaining) {
        return remaining.stream()
                .min(Comparator.comparingInt(node -> fullMapGraph.getDistance(current, node)))
                .orElseThrow();
    }
}
