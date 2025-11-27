package client.ai.tsp;

import client.ai.FullMapGraph;
import client.data.XYPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class HeldKarpTraversalStrategy implements NodeTraversalStrategy {
    private static final Logger logger = LoggerFactory.getLogger(HeldKarpTraversalStrategy.class);

    private static final int INF = 255;
    private static final int MAX_NODES_LIMIT = 30;

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

        if (n >= 31)
            throw new RuntimeException("(1 << n) too big");

        // stores the minimum cost to reach node i having visited all nodes in mask
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

        // nodeFrom -> nodeTo, visiting all nodes in mask visitedNodes
        for (int visitedNodes = 0; visitedNodes < (1 << n); ++visitedNodes) {
            for (byte nodeFrom = 0; nodeFrom < n; ++nodeFrom) {
                // if nodeFrom is not in visitedNodes, skip
                if ((visitedNodes & (1 << nodeFrom)) == 0)
                    continue;

                int costFrom = Byte.toUnsignedInt(dp[visitedNodes][nodeFrom]);

                if (costFrom == INF)
                    throw new RuntimeException("distance is INF");

                for (byte nodeTo = 0; nodeTo < n; ++nodeTo) {
                    // if nodeTo is in visitedNodes, skip
                    if ((visitedNodes & (1 << nodeTo)) != 0)
                        continue;

                    int visitedNodesAfterVisitedNodeTo = visitedNodes | (1 << nodeTo);

                    int distVal = Byte.toUnsignedInt(dist[nodeFrom][nodeTo]);
                    int newCost = costFrom + distVal;

                    if (newCost >= INF)
                        throw new RuntimeException("distance exceeded INF");

                    int currentCost = Byte.toUnsignedInt(dp[visitedNodesAfterVisitedNodeTo][nodeTo]);

                    if (newCost < currentCost) {
                        dp[visitedNodesAfterVisitedNodeTo][nodeTo] = (byte) newCost;
                        parent[visitedNodesAfterVisitedNodeTo][nodeTo] = nodeFrom;
                    }
                }
            }
        }

        int allNodesVisited = (1 << n) - 1;
        int minCost = INF;
        int bestEndNode = -1;

        for (int i = 0; i < n; ++i) {
            int currentCost = Byte.toUnsignedInt(dp[allNodesVisited][i]);

            if (currentCost < minCost) {
                minCost = currentCost;
                bestEndNode = i;
            }
        }

        if (bestEndNode == -1)
            throw new RuntimeException("Held-Karp failed: could not find a valid end node.");

        List<XYPair> optimalPath = new ArrayList<>();
        int currentVisitedNodes = allNodesVisited;
        int currentNode = bestEndNode;

        while (Integer.bitCount(currentVisitedNodes) > 0) {
            optimalPath.add(allNodes.get(currentNode));

            int visitedNode = currentNode;
            currentNode = parent[currentVisitedNodes][currentNode];
            currentVisitedNodes ^= (1 << visitedNode);  // currentVisitedNodes before visiting visitedNode
        }

        optimalPath.add(start);
        Collections.reverse(optimalPath);

        logger.debug("Held-Karp found optimal path with cost {}", minCost);
        logger.debug("Path: {}", optimalPath);

        if (minCost != fullMapGraph.getDistance(optimalPath))
            throw new RuntimeException("minCost is wrong!");

        return optimalPath;
    }
}