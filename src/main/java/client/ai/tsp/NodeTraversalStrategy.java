package client.ai.tsp;

import client.ai.graph.DistanceMatrix;
import client.ai.graph.FullMapGraph;
import client.data.XYPair;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public interface NodeTraversalStrategy {
    default TraversalResult orderNodes(FullMapGraph fullMapGraph, XYPair start, Set<XYPair> nodes) {
        if (fullMapGraph == null)
            throw new IllegalArgumentException("fullMapGraph is null");
        if (start == null)
            throw new IllegalArgumentException("start is null");
        if (nodes == null)
            throw new IllegalArgumentException("nodes is null");
        if (nodes.contains(start))
            throw new IllegalArgumentException("nodes contains start");

        List<XYPair> allNodes = new ArrayList<>(nodes.size() + 1);
        allNodes.add(start);
        allNodes.addAll(nodes);

        DistanceMatrix distanceMatrix = fullMapGraph.getDistanceMatrix(allNodes);
        if (distanceMatrix == null)
            throw new IllegalArgumentException("distanceMatrix is null");

        int[] indexPath = computePath(distanceMatrix);

        List<XYPair> path = new ArrayList<>(allNodes.size());
        for (int idx : indexPath)
            path.add(allNodes.get(idx));

        int distance = distanceMatrix.calculateTotalDistance(indexPath);
        return new TraversalResult(path, distance);
    }

    // 0 index is always start node and is fixed
    int[] computePath(DistanceMatrix distanceMatrix);
}