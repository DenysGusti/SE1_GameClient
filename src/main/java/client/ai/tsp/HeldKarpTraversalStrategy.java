package client.ai.tsp;

import client.ai.FullMapGraph;
import client.data.XYPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class HeldKarpTraversalStrategy implements NodeTraversalStrategy {
    private static final Logger logger = LoggerFactory.getLogger(HeldKarpTraversalStrategy.class);

    private final FullMapGraph fullMapGraph;

    public HeldKarpTraversalStrategy(FullMapGraph fullMapGraph) {
        if (fullMapGraph == null)
            throw new IllegalArgumentException("fullMapGraph must not be null");

        this.fullMapGraph = fullMapGraph;
    }

    @Override
    public List<XYPair> orderNodes(XYPair start, Set<XYPair> nodes) {
        if (start == null)
            throw new IllegalArgumentException("start must not be null");
        if (nodes == null)
            throw new IllegalArgumentException("nodes must not be null");
        if (nodes.contains(start))
            throw new IllegalArgumentException("nodes contains start");

        List<XYPair> allNodes = new ArrayList<>(nodes);
        int n = allNodes.size();

        logger.debug("Running exact Held-Karp solver for {} nodes...", n);

        var dist = new short[n][n];
        for (int i = 0; i < n; ++i)
            for (int j = 0; j < n; ++j)
                dist[i][j] = (short) fullMapGraph.getDistance(allNodes.get(i), allNodes.get(j));

        // stores the minimum cost to reach node i having visited all nodes in mask
        // mask is a bitmask where the k-th bit set means node k is visited
        var dp = new short[1 << n][n];
        for (var row : dp)
            Arrays.fill(row, Short.MAX_VALUE);

        var parent = new short[1 << n][n];

        for (int i = 0; i < n; ++i)
            dp[1 << i][i] = (short) fullMapGraph.getDistance(start, allNodes.get(i));

        // nodeFrom -> nodeTo, visiting all nodes in mask visitedNodes
        for (int visitedNodes = 0; visitedNodes < (1 << n); ++visitedNodes) {
            for (short nodeFrom = 0; nodeFrom < n; ++nodeFrom) {
                // if nodeFrom is not in visitedNodes, skip
                if ((visitedNodes & (1 << nodeFrom)) == 0)
                    continue;

                for (short nodeTo = 0; nodeTo < n; ++nodeTo) {
                    // if nodeTo is in visitedNodes, skip
                    if ((visitedNodes & (1 << nodeTo)) != 0)
                        continue;

                    int visitedNodesAfterVisitedNodeTo = visitedNodes | (1 << nodeTo);
                    var newCost = (short) (dp[visitedNodes][nodeFrom] + dist[nodeFrom][nodeTo]);
                    short currentCost = dp[visitedNodesAfterVisitedNodeTo][nodeTo];

                    if (newCost < currentCost) {
                        dp[visitedNodesAfterVisitedNodeTo][nodeTo] = newCost;
                        parent[visitedNodesAfterVisitedNodeTo][nodeTo] = nodeFrom;
                    }
                }
            }
        }

        int allNodesVisited = (1 << n) - 1;
        short minCost = Short.MAX_VALUE;
        int bestEndNode = -1;

        for (int i = 0; i < n; ++i) {
            short currentCost = dp[allNodesVisited][i];

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
        while (currentVisitedNodes > 0) {
            optimalPath.add(allNodes.get(currentNode));
            int visitedNode = currentNode;
            currentNode = parent[currentVisitedNodes][currentNode];
            currentVisitedNodes ^= (1 << visitedNode);  // currentVisitedNodes before visiting visitedNode
        }

        optimalPath.add(start);
        Collections.reverse(optimalPath);

        logger.debug("Held-Karp found optimal path with cost {}", minCost);
        return optimalPath;
    }
}