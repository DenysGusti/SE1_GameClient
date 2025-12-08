package client.ai.graph;

import client.data.ETerrain;
import client.data.XYPair;
import client.data.fromserver.FullMap;
import client.data.fromserver.FullMapNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class FullMapGraphFactory {
    private static final Logger logger = LoggerFactory.getLogger(FullMapGraphFactory.class);

    private static final Map<ETerrain, Short> terrainMovementCost = Map.of(
            ETerrain.Grass, (short) 1,
            ETerrain.Mountain, (short) 2
    );

    private static short getMovementCost(ETerrain from, ETerrain to) {
        int cost = terrainMovementCost.get(from) + terrainMovementCost.get(to);
        if (cost >= Short.MAX_VALUE)
            throw new RuntimeException("Movement Cost Overflow!");

        return (short) cost;
    }

    @SuppressWarnings("unchecked")
    public FullMapGraph createGraph(FullMap fullMap) {
        if (fullMap == null)
            throw new IllegalArgumentException("fullMap is null");

        long startTime = System.nanoTime();

        List<XYPair> validNodes = fullMap.nodes().keySet().stream()
                .filter(coordinate -> !fullMap.nodes().get(coordinate).isWater())
                .toList();

        if (validNodes.size() >= Short.MAX_VALUE)
            throw new IllegalArgumentException("Too many valid nodes!");

        short n = (short) validNodes.size();

        XYPair[] indexToCoordinate = validNodes.toArray(new XYPair[0]);
        Map<XYPair, Short> coordinateToIndex = new HashMap<>(n);
        for (short i = 0; i < n; i++)
            coordinateToIndex.put(indexToCoordinate[i], i);

        short[][] distances = new short[n][n];
        var tempNext = (Set<Short>[][]) new Set[n][n];

        for (short i = 0; i < n; ++i)
            for (short j = 0; j < n; ++j) {
                if (i == j) {
                    distances[i][j] = 0;
                    tempNext[i][j] = Collections.emptySet();
                } else {
                    distances[i][j] = Short.MAX_VALUE;
                    tempNext[i][j] = new HashSet<>();
                }
            }

        for (short i = 0; i < n; ++i) {
            XYPair coordinateFrom = indexToCoordinate[i];
            FullMapNode nodeFrom = fullMap.nodes().get(coordinateFrom);

            List<XYPair> traversableAdjacentNeighbors = coordinateFrom.getAdjacentNeighbors(fullMap.size()).stream()
                    .filter(coordinateToIndex::containsKey)
                    .toList();

            for (XYPair coordinateTo : traversableAdjacentNeighbors) {
                short j = coordinateToIndex.get(coordinateTo);
                FullMapNode nodeTo = fullMap.nodes().get(coordinateTo);

                distances[i][j] = getMovementCost(nodeFrom.terrain(), nodeTo.terrain());
                tempNext[i][j].add(j);
            }
        }

        // Floyd-Warshall Algorithm O(n^3)
        for (short k = 0; k < n; ++k) {
            for (short i = 0; i < n; ++i) {
                if (distances[i][k] == Short.MAX_VALUE)
                    continue;

                for (short j = 0; j < n; ++j) {
                    if (distances[k][j] == Short.MAX_VALUE)
                        continue;

                    int newCostTmp = distances[i][k] + distances[k][j];
                    if (newCostTmp >= Short.MAX_VALUE)
                        throw new RuntimeException("Cost Overflow!");
                    var newDistance = (short) newCostTmp;

                    if (distances[i][j] > newDistance) {
                        distances[i][j] = newDistance;
                        tempNext[i][j].clear();
                        tempNext[i][j].addAll(tempNext[i][k]);
                    } else if (distances[i][j] == newDistance)
                        tempNext[i][j].addAll(tempNext[i][k]);
                }
            }
        }

        var next = new short[n][n][];
        for (short i = 0; i < n; ++i)
            for (short j = 0; j < n; ++j) {
                Set<Short> nextIndices = tempNext[i][j];
                var arr = new short[nextIndices.size()];
                short idx = 0;
                for (short val : nextIndices)
                    arr[idx++] = val;
                next[i][j] = arr;
            }

        long endTime = System.nanoTime();
        double duration = (endTime - startTime) / 1_000_000_000.;
        logger.debug("Graph constructed in {}s, nodes: {}", duration, n);

        return new FullMapGraph(indexToCoordinate, coordinateToIndex, distances, next);
    }
}