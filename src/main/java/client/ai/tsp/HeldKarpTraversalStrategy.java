package client.ai.tsp;

import client.ai.FullMapGraph;
import client.data.XYPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class HeldKarpTraversalStrategy implements NodeTraversalStrategy {
    private static final Logger logger = LoggerFactory.getLogger(HeldKarpTraversalStrategy.class);

    private static final int MAX_NODES_LIMIT = 27;
    private static final int INF = 255;

    @Override
    public TraversalResult computePath(FullMapGraph fullMapGraph, XYPair start, Set<XYPair> nodes) {
        if (nodes.size() > MAX_NODES_LIMIT)
            throw new RuntimeException("(1 << n) is too big for Java heap space");

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

        // dp[mask][i] means the minimum cost to visit the set of nodes marked by mask, ending the journey at node i
        // mask is a bitmask where the k-th bit set means node k is visited
        var dp = new byte[1 << n][n];
        for (var row : dp)
            Arrays.fill(row, (byte) INF);

        var parent = new byte[1 << n][n];

        for (int i = 0; i < n; ++i) {
            int distance = fullMapGraph.getDistance(start, allNodes.get(i));
            if (distance >= INF)
                throw new RuntimeException("distance exceeded INF");
            dp[1 << i][i] = (byte) distance;
        }

        int allNodesVisitedMask = (1 << n) - 1;
        // nodeFrom -> nodeTo, visiting all nodes in mask visitedNodes
        for (int visitedNodesMask = 1; visitedNodesMask <= allNodesVisitedMask; ++visitedNodesMask) {
            // flip all the bits after the rightmost 1-bit and remove that 1-bit -> nodeFromMask without nodeFrom
            for (int nodeFromMask = visitedNodesMask; nodeFromMask > 0; nodeFromMask &= nodeFromMask - 1) {
                byte nodeFrom = (byte) Integer.numberOfTrailingZeros(nodeFromMask); // rightmost 1-bit index

                int distanceFrom = Byte.toUnsignedInt(dp[visitedNodesMask][nodeFrom]);
                if (distanceFrom == INF)
                    throw new RuntimeException("distanceFrom is INF");

                // nodeToMask - unvisitedNodesMask, all except visited
                // flip all the bits after the rightmost 1-bit and remove that 1-bit -> nodeToMask without nodeTo
                for (int nodeToMask = allNodesVisitedMask ^ visitedNodesMask; nodeToMask > 0; nodeToMask &= nodeToMask - 1) {
                    byte nodeTo = (byte) Integer.numberOfTrailingZeros(nodeToMask); // rightmost 1-bit index

                    int visitedNodesAfterVisitedNodeToMask = visitedNodesMask | (1 << nodeTo);

                    int distance = Byte.toUnsignedInt(dist[nodeFrom][nodeTo]);
                    int newDistance = distanceFrom + distance;

                    if (newDistance >= INF)
                        throw new RuntimeException("newDistance exceeded INF");

                    int currentCost = Byte.toUnsignedInt(dp[visitedNodesAfterVisitedNodeToMask][nodeTo]);

                    if (newDistance < currentCost) {
                        dp[visitedNodesAfterVisitedNodeToMask][nodeTo] = (byte) newDistance;
                        parent[visitedNodesAfterVisitedNodeToMask][nodeTo] = nodeFrom;
                    }
                }
            }
        }

        int minDistance = INF;
        int bestEndNode = -1;

        for (int i = 0; i < n; ++i) {
            int currentDistance = Byte.toUnsignedInt(dp[allNodesVisitedMask][i]);

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

        while (Integer.bitCount(currentVisitedNodesMask) > 0) {
            optimalPath.add(allNodes.get(currentNode));

            int visitedNode = currentNode;
            currentNode = parent[currentVisitedNodesMask][currentNode];
            currentVisitedNodesMask ^= (1 << visitedNode);  // currentVisitedNodesMask without visitedNode
        }

        optimalPath.add(start);
        Collections.reverse(optimalPath);

        long endTime = System.nanoTime();
        double duration = (endTime - startTime) / 1_000_000_000.;
        logger.debug("Held-Karp finished: time: {}s, distance: {}", duration, minDistance);

        return new TraversalResult(optimalPath, minDistance);
    }
}