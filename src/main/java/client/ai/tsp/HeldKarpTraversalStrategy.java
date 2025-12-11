package client.ai.tsp;

import client.ai.graph.DistanceMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class HeldKarpTraversalStrategy implements NodeTraversalStrategy {
    private static final Logger logger = LoggerFactory.getLogger(HeldKarpTraversalStrategy.class);

    private static final int MAX_NODES_LIMIT = 31;
    private static final int INF = 255;

    @Override
    public int[] computePath(DistanceMatrix distanceMatrix) {
        long startTime = System.nanoTime();
        logger.debug("Held-Karp started for {} nodes...", distanceMatrix.size());

        if (distanceMatrix.size() > MAX_NODES_LIMIT)
            throw new RuntimeException("Node count (" + distanceMatrix.size() + ") too high for Java Heap");

        if (distanceMatrix.size() <= 2) {
            var path = new int[distanceMatrix.size()];
            for (int i = 0; i < distanceMatrix.size(); ++i)
                path[i] = i;
            return path;
        }

        int flexibleNodeSize = distanceMatrix.size() - 1;

        // dp[i][mask] means the minimum distance to visit the set of nodes marked by mask, ending the journey at node 'i'
        // mask is a bitmask where the k-th bit set means node k is visited
        // only valid states (where node 'i' is in the mask) are stored
        int compressedMaskSize = 1 << (flexibleNodeSize - 1);
        var dp = new byte[flexibleNodeSize][compressedMaskSize];
        for (var row : dp)
            Arrays.fill(row, (byte) INF);

        for (int i = 0; i < flexibleNodeSize; ++i) {
            int distance = distanceMatrix.getDistance(0, i + 1);
            dp[i][0] = (byte) distance; // for a single-bit mask (1 << i), removing bit i results in 0
        }

        int allNodesVisitedMask = (1 << flexibleNodeSize) - 1;
        // nodeFrom -> nodeTo, visiting all nodes in mask visitedNodes
        for (int visitedNodesMask = 1; visitedNodesMask <= allNodesVisitedMask; ++visitedNodesMask) {
            // flip all the bits after the rightmost 1-bit and remove that 1-bit -> nodeFromMask without nodeFrom
            for (int nodeFromMask = visitedNodesMask; nodeFromMask != 0; nodeFromMask &= nodeFromMask - 1) {
                int nodeFrom = Integer.numberOfTrailingZeros(nodeFromMask); // rightmost 1-bit index

                int indexFrom = compress(visitedNodesMask, nodeFrom);
                int distanceFrom = Byte.toUnsignedInt(dp[nodeFrom][indexFrom]);
                if (distanceFrom == INF)
                    throw new RuntimeException("distanceFrom is INF");

                // nodeToMask - unvisitedNodesMask, all except visited
                // flip all the bits after the rightmost 1-bit and remove that 1-bit -> nodeToMask without nodeTo
                for (int nodeToMask = allNodesVisitedMask ^ visitedNodesMask; nodeToMask != 0; nodeToMask &= nodeToMask - 1) {
                    int nodeTo = Integer.numberOfTrailingZeros(nodeToMask); // rightmost 1-bit index

                    int visitedNodesAfterVisitedNodeToMask = visitedNodesMask | (1 << nodeTo);

                    int distance = distanceMatrix.getDistance(nodeFrom + 1, nodeTo + 1);
                    int newDistance = distanceFrom + distance;

                    if (newDistance >= INF)
                        throw new RuntimeException("newDistance exceeded INF");

                    int indexTo = compress(visitedNodesAfterVisitedNodeToMask, nodeTo);
                    int currentDistance = Byte.toUnsignedInt(dp[nodeTo][indexTo]);

                    if (newDistance < currentDistance)
                        dp[nodeTo][indexTo] = (byte) newDistance;
                }
            }
        }

        int minDistance = INF;
        int bestEndNode = -1;

        // removing any bit 'i' results in all 1s
        int allNodesVisitedCompressedMaskIndex = compressedMaskSize - 1;

        for (int i = 0; i < flexibleNodeSize; ++i) {
            int currentDistance = Byte.toUnsignedInt(dp[i][allNodesVisitedCompressedMaskIndex]);
            if (currentDistance < minDistance) {
                minDistance = currentDistance;
                bestEndNode = i;
            }
        }

        if (bestEndNode == -1)
            throw new RuntimeException("Held-Karp failed: could not find a valid end node.");

        var path = new int[distanceMatrix.size()];
        path[0] = 0;  // start (0) is fixed

        int currentVisitedNodesMask = allNodesVisitedMask;
        int currentNode = bestEndNode;

        for (int pathIndex = distanceMatrix.size() - 1; pathIndex > 1; --pathIndex) {
            path[pathIndex] = currentNode + 1; // convert 0-based flexible index back to 1-based matrix index

            // prevVisitedMask is currentVisitedNodesMask without currentNode
            int prevVisitedMask = currentVisitedNodesMask ^ (1 << currentNode);
            if (prevVisitedMask == 0)
                throw new RuntimeException("Path reconstruction failed: could not find a valid end node.");

            int currentIndex = compress(currentVisitedNodesMask, currentNode);
            int currentDistance = Byte.toUnsignedInt(dp[currentNode][currentIndex]);

            int prevNode = -1;

            for (int parentNodeMask = prevVisitedMask; parentNodeMask != 0; parentNodeMask &= parentNodeMask - 1) {
                int parentNode = Integer.numberOfTrailingZeros(parentNodeMask); // rightmost 1-bit index

                int prevIndex = compress(prevVisitedMask, parentNode);
                int parentDistance = Byte.toUnsignedInt(dp[parentNode][prevIndex]);
                int parentToCurrentDistance = distanceMatrix.getDistance(parentNode + 1, currentNode + 1);

                if (parentToCurrentDistance == INF)
                    throw new RuntimeException("parentToCurrentDistance is INF");

                int distance = parentDistance + parentToCurrentDistance;
                if (distance == currentDistance) {
                    prevNode = parentNode;
                    break;
                }
            }

            if (prevNode == -1)
                throw new RuntimeException("Path reconstruction failed: Broken DP chain.");

            currentNode = prevNode;
            currentVisitedNodesMask = prevVisitedMask;
        }
        path[1] = currentNode + 1; // convert 0-based flexible index back to 1-based matrix index

        double duration = (System.nanoTime() - startTime) / 1_000_000_000.;
        logger.debug("Held-Karp finished in {}s: distance: {}", duration, minDistance);
        return path;
    }

    private static int compress(int mask, int bitToRemove) {
        // all bits above the bit we want to remove | all bits below the bit we want to remove
        return (mask >>> (bitToRemove + 1)) << bitToRemove | (mask & ((1 << bitToRemove) - 1));
    }
}