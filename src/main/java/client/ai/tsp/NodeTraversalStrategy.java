package client.ai.tsp;

import client.ai.graph.FullMapGraph;
import client.data.XYPair;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public interface NodeTraversalStrategy {
    record TraversalResult(List<XYPair> path, int distance) {
        public TraversalResult {
            if (path == null)
                throw new IllegalArgumentException("path is null");
            if (distance < 0)
                throw new IllegalArgumentException("distance is negative");
        }
    }

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

        byte[][] distanceMatrix = fullMapGraph.getDistanceMatrix(allNodes);
        if (distanceMatrix == null)
            throw new IllegalArgumentException("distanceMatrix is null");

        int[] indexPath = computePath(distanceMatrix);

        List<XYPair> path = new ArrayList<>(allNodes.size());
        for (int idx : indexPath)
            path.add(allNodes.get(idx));

        int distance = calculateTotalDistance(distanceMatrix, indexPath);

        if (distance != fullMapGraph.getDistance(path))
            throw new RuntimeException("distance does not match");

        return new TraversalResult(path, distance);
    }

    default int calculateTotalDistance(byte[][] distanceMatrix, int[] path) {
        int distance = 0;
        for (int i = 0; i < path.length - 1; ++i)
            distance += Byte.toUnsignedInt(distanceMatrix[path[i]][path[i + 1]]);
        return distance;
    }

    // 0 index is always start node and is fixed
    int[] computePath(byte[][] distanceMatrix);
}