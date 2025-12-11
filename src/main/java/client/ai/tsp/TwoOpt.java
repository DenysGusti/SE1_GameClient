package client.ai.tsp;

public interface TwoOpt {
    default int calculateDelta(byte[][] distanceMatrix, int[] path, int i, int j) {
        int nodeBeforeI = path[i - 1];
        int nodeI = path[i];
        int nodeJ = path[j];

        int currentDistance = Byte.toUnsignedInt(distanceMatrix[nodeBeforeI][nodeI]);
        int newDistance = Byte.toUnsignedInt(distanceMatrix[nodeBeforeI][nodeJ]);

        if (j + 1 < path.length) {
            int nodeAfterJ = path[j + 1];

            currentDistance += Byte.toUnsignedInt(distanceMatrix[nodeJ][nodeAfterJ]);
            newDistance += Byte.toUnsignedInt(distanceMatrix[nodeI][nodeAfterJ]);
        }

        return newDistance - currentDistance;
    }

    // reverses the segment of the path between indices i and j (inclusive).
    // [a, b, c, d, e], i = 1, j = 3 -> [a, d, c, b, e]
    default void reverseSegment(int[] path, int i, int j) {
        for (; i < j; ++i, --j) {
            int temp = path[i];
            path[i] = path[j];
            path[j] = temp;
        }
    }
}