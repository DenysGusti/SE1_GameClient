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
    private static final int INF = 255;

    private static final Map<ETerrain, Byte> terrainMovementCost = Map.of(
            ETerrain.Grass, (byte) 1,
            ETerrain.Mountain, (byte) 2
    );

    private static byte getMovementDistance(ETerrain from, ETerrain to) {
        int fromDistance = Byte.toUnsignedInt(terrainMovementCost.get(from));
        int toDistance = Byte.toUnsignedInt(terrainMovementCost.get(to));
        int cost = fromDistance + toDistance;
        if (cost >= INF)
            throw new RuntimeException("Movement Cost Overflow!");

        return (byte) cost;
    }

    @SuppressWarnings("unchecked")
    public FullMapGraph createGraph(FullMap fullMap) {
        if (fullMap == null)
            throw new IllegalArgumentException("fullMap is null");

        long startTime = System.nanoTime();

        Map<XYPair, FullMapNode> nodes = fullMap.nodes();

        List<XYPair> validNodes = nodes.keySet().stream()
                .filter(coordinate -> !nodes.get(coordinate).isWater())
                .toList();

        if (validNodes.size() >= INF)
            throw new IllegalArgumentException("Too many valid nodes!");

        int n = validNodes.size();

        XYPair[] indexToCoordinate = validNodes.toArray(new XYPair[0]);
        Map<XYPair, Integer> coordinateToIndex = new HashMap<>(n);
        for (int i = 0; i < n; ++i)
            coordinateToIndex.put(indexToCoordinate[i], i);

        var distances = new byte[n][n];
        var tempNext = (Set<Byte>[][]) new Set[n][n];

        for (int i = 0; i < n; ++i)
            for (int j = 0; j < n; ++j) {
                if (i == j) {
                    distances[i][j] = 0;
                    tempNext[i][j] = Collections.emptySet();
                } else {
                    distances[i][j] = (byte) INF;
                    tempNext[i][j] = new HashSet<>();
                }
            }

        for (int i = 0; i < n; ++i) {
            XYPair coordinateFrom = indexToCoordinate[i];
            FullMapNode nodeFrom = nodes.get(coordinateFrom);

            List<XYPair> traversableAdjacentNeighbors = coordinateFrom.getAdjacentNeighbors(fullMap.size()).stream()
                    .filter(coordinateToIndex::containsKey)
                    .toList();

            for (XYPair coordinateTo : traversableAdjacentNeighbors) {
                int j = coordinateToIndex.get(coordinateTo);
                FullMapNode nodeTo = nodes.get(coordinateTo);

                distances[i][j] = getMovementDistance(nodeFrom.terrain(), nodeTo.terrain());
                tempNext[i][j].add((byte) j);
            }
        }

        // Floyd-Warshall Algorithm O(n^3)
        for (int k = 0; k < n; ++k) {
            for (int i = 0; i < n; ++i) {
                int distance_ik = Byte.toUnsignedInt(distances[i][k]);
                if (distance_ik == INF)
                    continue;

                for (int j = 0; j < n; ++j) {
                    int distance_kj = Byte.toUnsignedInt(distances[k][j]);
                    if (distance_kj == INF)
                        continue;

                    int newDistance = distance_ik + distance_kj;
                    if (newDistance >= INF)
                        throw new RuntimeException("Cost Overflow!");

                    int currentDistance = Byte.toUnsignedInt(distances[i][j]);
                    if (currentDistance > newDistance) {
                        distances[i][j] = (byte) newDistance;
                        tempNext[i][j].clear();
                        tempNext[i][j].addAll(tempNext[i][k]);
                    } else if (currentDistance == newDistance)
                        tempNext[i][j].addAll(tempNext[i][k]);
                }
            }
        }

        var next = new byte[n][n][];
        for (int i = 0; i < n; ++i)
            for (int j = 0; j < n; ++j) {
                Set<Byte> nextIndices = tempNext[i][j];
                var arr = new byte[nextIndices.size()];
                int idx = 0;
                for (byte val : nextIndices)
                    arr[idx++] = val;
                next[i][j] = arr;
            }

        double duration = (System.nanoTime() - startTime) / 1_000_000_000.;
        logger.debug("Graph constructed in {}s, nodes: {}", duration, n);

        return new FullMapGraph(indexToCoordinate, coordinateToIndex, distances, next);
    }
}