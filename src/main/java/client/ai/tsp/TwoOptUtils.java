package client.ai.tsp;

import client.ai.graph.DistanceMatrix;

public class TwoOptUtils {
    public int calculateDelta(DistanceMatrix distanceMatrix, int[] path, int i, int j) {
        int nodeBeforeI = path[i - 1];
        int nodeI = path[i];
        int nodeJ = path[j];

        int currentDistance = distanceMatrix.getDistance(nodeBeforeI, nodeI);
        int newDistance = distanceMatrix.getDistance(nodeBeforeI, nodeJ);

        if (j + 1 < path.length) {
            int nodeAfterJ = path[j + 1];

            currentDistance += distanceMatrix.getDistance(nodeJ, nodeAfterJ);
            newDistance += distanceMatrix.getDistance(nodeI, nodeAfterJ);
        }

        return newDistance - currentDistance;
    }

    // reverses the segment of the path between indices i and j (inclusive).
    // [a, b, c, d, e], i = 1, j = 3 -> [a, d, c, b, e]
    public void reverseSegment(int[] path, int i, int j) {
        for (; i < j; ++i, --j) {
            int temp = path[i];
            path[i] = path[j];
            path[j] = temp;
        }
    }
}