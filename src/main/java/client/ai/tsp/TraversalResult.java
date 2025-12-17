package client.ai.tsp;

import client.data.XYPair;

import java.util.List;

public record TraversalResult(List<XYPair> path, int distance) {
    public TraversalResult {
        if (path == null)
            throw new IllegalArgumentException("path is null");
        if (distance < 0)
            throw new IllegalArgumentException("distance is negative");
    }
}