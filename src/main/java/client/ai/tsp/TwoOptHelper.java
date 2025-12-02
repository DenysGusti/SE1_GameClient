package client.ai.tsp;

import client.ai.graph.FullMapGraph;
import client.data.XYPair;

import java.util.Collections;
import java.util.List;

public class TwoOptHelper {
    public int calculateDelta(FullMapGraph fullMapGraph, List<XYPair> path, int i, int j) {
        if (fullMapGraph == null)
            throw new IllegalArgumentException("fullMapGraph must not be null");

        validate(path, i, j);

        XYPair nodeA = path.get(i - 1);
        XYPair nodeB = path.get(i);
        XYPair nodeC = path.get(j);

        int currentDistance = fullMapGraph.getDistance(nodeA, nodeB);
        int newDistance = fullMapGraph.getDistance(nodeA, nodeC);

        if (j + 1 < path.size()) {
            XYPair nodeD = path.get(j + 1);
            currentDistance += fullMapGraph.getDistance(nodeC, nodeD);
            newDistance += fullMapGraph.getDistance(nodeB, nodeD);
        }

        return newDistance - currentDistance;
    }

    // reverses the segment of the path between indices i and j (inclusive).
    // [a, b, c, d, e], i = 1, j = 3 -> [a, d, c, b, e]
    public void performSwap(List<XYPair> path, int i, int j) {
        validate(path, i, j);

        while (i < j)
            Collections.swap(path, i++, j--);
    }

    private void validate(List<XYPair> path, int i, int j) {
        if (path == null)
            throw new IllegalArgumentException("path must not be null");
        if (i < 0 || i >= path.size() || j < 0 || j >= path.size())
            throw new IndexOutOfBoundsException("index out of bounds");
        if (i >= j)
            throw new IllegalArgumentException("i must not be less than j");
    }
}
