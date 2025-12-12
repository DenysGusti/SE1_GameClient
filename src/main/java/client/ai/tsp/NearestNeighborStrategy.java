package client.ai.tsp;

import client.ai.graph.DistanceMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NearestNeighborStrategy implements NodeTraversalStrategy {
    private static final Logger logger = LoggerFactory.getLogger(NearestNeighborStrategy.class);

    public int[] computePath(DistanceMatrix distanceMatrix) {
        long startTime = System.nanoTime();
        logger.debug("Nearest Neighbor started for {} nodes...", distanceMatrix.size());

        var path = new int[distanceMatrix.size()];
        var visited = new boolean[distanceMatrix.size()];

        path[0] = 0;  // start (0) is fixed
        visited[0] = true;

        for (int i = 1; i < distanceMatrix.size(); ++i) {
            int currentNode = path[i - 1];
            int nearestNode = getNearestNode(distanceMatrix, visited, currentNode);
            path[i] = nearestNode;
            visited[nearestNode] = true;
        }

        double duration = (System.nanoTime() - startTime) / 1_000_000_000.;
        logger.debug("Nearest Neighbor finished in {}s, distance: {}", duration,
                distanceMatrix.calculateTotalDistance(path));
        return path;
    }

    private static int getNearestNode(DistanceMatrix distanceMatrix, boolean[] visited, int currentNode) {
        int nearestNode = -1;
        int shortestDistance = Integer.MAX_VALUE;

        for (int candidateNode = 0; candidateNode < distanceMatrix.size(); ++candidateNode)
            if (!visited[candidateNode]) {
                int currentDistance = distanceMatrix.getDistance(currentNode, candidateNode);

                if (currentDistance < shortestDistance) {
                    shortestDistance = currentDistance;
                    nearestNode = candidateNode;
                }
            }

        if (nearestNode == -1)
            throw new RuntimeException("Nearest Neighbor traversal failed");

        return nearestNode;
    }
}