package client.ai.tsp;

import client.ai.graph.FullMapGraph;
import client.data.XYPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class HeldKarpTraversalStrategy implements NodeTraversalStrategy {
    private static final Logger logger = LoggerFactory.getLogger(HeldKarpTraversalStrategy.class);

    private static final int MAX_NODES_LIMIT = 28;
    private static final int INF = 255;

    @Override
    public TraversalResult computePath(FullMapGraph fullMapGraph, XYPair start, Set<XYPair> nodes) {
//        if (nodes.size() > MAX_NODES_LIMIT)
//            throw new RuntimeException("(1 << n) is too big for Java heap space");

        long startTime = System.nanoTime();
        logger.debug("Held-Karp started for {} nodes...", nodes.size());

        List<XYPair> allNodes = new ArrayList<>(nodes);
        int n = allNodes.size();

        var dist = new byte[n][n];
        for (int i = 0; i < n; ++i)
            for (int j = 0; j < n; ++j) {
                int distance = fullMapGraph.getDistance(allNodes.get(i), allNodes.get(j));
                if (distance >= INF)
                    throw new RuntimeException("distance exceeded INF");
                dist[i][j] = (byte) distance;
            }

        // dp[i][mask] means the minimum cost to visit the set of nodes marked by mask, ending the journey at node i
        // mask is a bitmask where the k-th bit set means node k is visited
        var dp = new byte[n][1 << n];
        for (var row : dp)
            Arrays.fill(row, (byte) INF);

        for (int i = 0; i < n; ++i) {
            int distance = fullMapGraph.getDistance(start, allNodes.get(i));
            if (distance >= INF)
                throw new RuntimeException("distance exceeded INF");
            dp[i][1 << i] = (byte) distance;
        }

        int allNodesVisitedMask = (1 << n) - 1;
        // nodeFrom -> nodeTo, visiting all nodes in mask visitedNodes
        for (int visitedNodesMask = 1; visitedNodesMask <= allNodesVisitedMask; ++visitedNodesMask) {
            // flip all the bits after the rightmost 1-bit and remove that 1-bit -> nodeFromMask without nodeFrom
            for (int nodeFromMask = visitedNodesMask; nodeFromMask != 0; nodeFromMask &= nodeFromMask - 1) {
                var nodeFrom = (byte) Integer.numberOfTrailingZeros(nodeFromMask); // rightmost 1-bit index

                int distanceFrom = Byte.toUnsignedInt(dp[nodeFrom][visitedNodesMask]);
                if (distanceFrom == INF)
                    throw new RuntimeException("distanceFrom is INF");

                // nodeToMask - unvisitedNodesMask, all except visited
                // flip all the bits after the rightmost 1-bit and remove that 1-bit -> nodeToMask without nodeTo
                for (int nodeToMask = allNodesVisitedMask ^ visitedNodesMask; nodeToMask != 0; nodeToMask &= nodeToMask - 1) {
                    var nodeTo = (byte) Integer.numberOfTrailingZeros(nodeToMask); // rightmost 1-bit index

                    int visitedNodesAfterVisitedNodeToMask = visitedNodesMask | (1 << nodeTo);

                    int distance = Byte.toUnsignedInt(dist[nodeFrom][nodeTo]);
                    int newDistance = distanceFrom + distance;

                    if (newDistance >= INF)
                        throw new RuntimeException("newDistance exceeded INF");

                    int currentCost = Byte.toUnsignedInt(dp[nodeTo][visitedNodesAfterVisitedNodeToMask]);

                    if (newDistance < currentCost)
                        dp[nodeTo][visitedNodesAfterVisitedNodeToMask] = (byte) newDistance;
                }
            }
        }

        int minDistance = INF;
        int bestEndNode = -1;

        for (int i = 0; i < n; ++i) {
            int currentDistance = Byte.toUnsignedInt(dp[i][allNodesVisitedMask]);

            if (currentDistance < minDistance) {
                minDistance = currentDistance;
                bestEndNode = i;
            }
        }

        if (bestEndNode == -1)
            throw new RuntimeException("Held-Karp failed: could not find a valid end node.");

        List<XYPair> optimalPath = new ArrayList<>();
        int currentVisitedNodesMask = allNodesVisitedMask;
        int currentNode = bestEndNode;

        while (true) {
            optimalPath.add(allNodes.get(currentNode));

            // prevVisitedMask is currentVisitedNodesMask without currentNode
            int prevVisitedMask = currentVisitedNodesMask ^ (1 << currentNode);
            if (prevVisitedMask == 0)
                break;

            int currentDistance = Byte.toUnsignedInt(dp[currentNode][currentVisitedNodesMask]);
            int prevNode = -1;

            for (int parentNodeMask = prevVisitedMask; parentNodeMask != 0; parentNodeMask &= parentNodeMask - 1) {
                var parentNode = (byte) Integer.numberOfTrailingZeros(parentNodeMask); // rightmost 1-bit index

                int parentCost = Byte.toUnsignedInt(dp[parentNode][prevVisitedMask]);
                int parentToCurrentDistance = Byte.toUnsignedInt(dist[parentNode][currentNode]);

                if (parentToCurrentDistance == INF)
                    throw new RuntimeException("parentToCurrentDistance is INF");

                int distance = parentCost + parentToCurrentDistance;
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

        optimalPath.add(start);
        Collections.reverse(optimalPath);

        if (fullMapGraph.getDistance(optimalPath) != minDistance)
            throw new RuntimeException("optimalPath is wrong!");

        long endTime = System.nanoTime();
        double duration = (endTime - startTime) / 1_000_000_000.;
        logger.debug("Held-Karp finished: time: {}s, distance: {}", duration, minDistance);

        return new TraversalResult(optimalPath, minDistance);
    }
}