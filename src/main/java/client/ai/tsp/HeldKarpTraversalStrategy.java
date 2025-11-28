package client.ai.tsp;

import client.ai.FullMapGraph;
import client.data.XYPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class HeldKarpTraversalStrategy implements NodeTraversalStrategy {
    private static final Logger logger = LoggerFactory.getLogger(HeldKarpTraversalStrategy.class);

    private static final int INF = 255;
    private static final int MAX_NODES_LIMIT = 27;

    private final FullMapGraph fullMapGraph;
    private final NodeTraversalStrategy fallbackStrategy;

    public HeldKarpTraversalStrategy(FullMapGraph fullMapGraph, NodeTraversalStrategy fallbackStrategy) {
        if (fullMapGraph == null)
            throw new IllegalArgumentException("fullMapGraph must not be null");
        if (fallbackStrategy == null)
            throw new IllegalArgumentException("fallbackStrategy must not be null");

        this.fullMapGraph = fullMapGraph;
        this.fallbackStrategy = fallbackStrategy;
    }

    @Override
    public List<XYPair> orderNodes(XYPair start, Set<XYPair> nodes) {
        if (start == null)
            throw new IllegalArgumentException("start must not be null");
        if (nodes == null)
            throw new IllegalArgumentException("nodes must not be null");
        if (nodes.contains(start))
            throw new IllegalArgumentException("nodes contains start");

        if (nodes.size() > MAX_NODES_LIMIT) {
            logger.info("Too many nodes ({}) for Held-Karp. Switching to fallback.", nodes.size());
            return fallbackStrategy.orderNodes(start, nodes);
        }

        long startTime = System.nanoTime();

        List<XYPair> allNodes = new ArrayList<>(nodes);
        int n = allNodes.size();

        logger.debug("Running exact Held-Karp solver for {} nodes...", n);

        var dist = new byte[n][n];
        for (int i = 0; i < n; ++i)
            for (int j = 0; j < n; ++j) {
                int distance = fullMapGraph.getDistance(allNodes.get(i), allNodes.get(j));
                if (distance >= INF)
                    throw new RuntimeException("distance exceeded INF");
                dist[i][j] = (byte) distance;
            }

        if (n >= 28)
            throw new RuntimeException("(1 << n) is too big for Java heap space");

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

                int costFrom = Byte.toUnsignedInt(dp[visitedNodesMask][nodeFrom]);
                if (costFrom == INF)
                    throw new RuntimeException("distance is INF");

                // nodeToMask - unvisitedNodesMask, all except visited
                // flip all the bits after the rightmost 1-bit and remove that 1-bit -> nodeToMask without nodeTo
                for (int nodeToMask = allNodesVisitedMask ^ visitedNodesMask; nodeToMask > 0; nodeToMask &= nodeToMask - 1) {
                    byte nodeTo = (byte) Integer.numberOfTrailingZeros(nodeToMask); // rightmost 1-bit index

                    int visitedNodesAfterVisitedNodeToMask = visitedNodesMask | (1 << nodeTo);

                    int distVal = Byte.toUnsignedInt(dist[nodeFrom][nodeTo]);
                    int newCost = costFrom + distVal;

                    if (newCost >= INF)
                        throw new RuntimeException("distance exceeded INF");

                    int currentCost = Byte.toUnsignedInt(dp[visitedNodesAfterVisitedNodeToMask][nodeTo]);

                    if (newCost < currentCost) {
                        dp[visitedNodesAfterVisitedNodeToMask][nodeTo] = (byte) newCost;
                        parent[visitedNodesAfterVisitedNodeToMask][nodeTo] = nodeFrom;
                    }
                }
            }
        }

        int minCost = INF;
        int bestEndNode = -1;

        for (int i = 0; i < n; ++i) {
            int currentCost = Byte.toUnsignedInt(dp[allNodesVisitedMask][i]);

            if (currentCost < minCost) {
                minCost = currentCost;
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

        logger.debug("Held-Karp found optimal path with cost {}", minCost);
        logger.debug("Path: {}", optimalPath);

        int realCost = fullMapGraph.getDistance(optimalPath);

        if (minCost != realCost)
            throw new RuntimeException("minCost is wrong!");

        long endTime = System.nanoTime();
        long durationNs = endTime - startTime;
        double durationMs = durationNs / 1_000_000_000.0;
        logger.info("Held-Karp finished in {}s", String.format("%.4f", durationMs));

        return optimalPath;
    }
}