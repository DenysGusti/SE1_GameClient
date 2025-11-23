package client.ai;

import client.data.XYPair;

import java.util.*;

public class NodeTraversalStrategy {
    private final FullMapGraph fullMapGraph;

    public NodeTraversalStrategy(FullMapGraph fullMapGraph) {
        if (fullMapGraph == null)
            throw new IllegalArgumentException("fullMapGraph must not be null");

        this.fullMapGraph = fullMapGraph;
    }

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
            XYPair finalCurrent = current;
            XYPair nearest = remaining.stream()
                    .min(Comparator.comparingInt(node -> fullMapGraph.getDistance(finalCurrent, node)))
                    .orElseThrow();

            ordered.add(nearest);
            remaining.remove(nearest);
            current = nearest;
        }

        return ordered;
    }
}
