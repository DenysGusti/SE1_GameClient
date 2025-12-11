package client.ai.tsp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NearestNeighborTraversalStrategy implements NodeTraversalStrategy {
    private static final Logger logger = LoggerFactory.getLogger(NearestNeighborTraversalStrategy.class);

    public int[] computePath(byte[][] distanceMatrix) {
        long startTime = System.nanoTime();
        int n = distanceMatrix.length;
        logger.debug("Nearest Neighbor started for {} nodes...", n);

        var path = new int[n];
        var visited = new boolean[n];

        path[0] = 0;  // start (0) is fixed
        visited[0] = true;

        for (int i = 1; i < n; ++i) {
            int currentNode = path[i - 1];
            int nearestNode = -1;
            int shortestDistance = Integer.MAX_VALUE;

            for (int candidateNode = 0; candidateNode < n; ++candidateNode)
                if (!visited[candidateNode]) {
                    int currentDistance = Byte.toUnsignedInt(distanceMatrix[currentNode][candidateNode]);

                    if (currentDistance < shortestDistance) {
                        shortestDistance = currentDistance;
                        nearestNode = candidateNode;
                    }
                }

            if (nearestNode == -1)
                throw new RuntimeException("Nearest Neighbor traversal failed");

            path[i] = nearestNode;
            visited[nearestNode] = true;
        }

        double duration = (System.nanoTime() - startTime) / 1_000_000_000.;
        logger.debug("Nearest Neighbor finished in {}s, distance: {}", duration,
                calculateTotalDistance(distanceMatrix, path));
        return path;
    }
}