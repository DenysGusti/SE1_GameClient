package client.ai;

import client.data.ETerrain;
import client.data.XYPair;
import client.data.fromserver.FullMap;
import client.data.fromserver.FullMapNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class FullMapGraph {
    private static final Logger logger = LoggerFactory.getLogger(FullMapGraph.class);

    private static final Map<ETerrain, Integer> terrainMovementCost =
            Map.of(
                    ETerrain.Grass, 1,
                    ETerrain.Mountain, 2
            );

    private static int getMovementCost(ETerrain from, ETerrain to) {
        return terrainMovementCost.get(from) + terrainMovementCost.get(to);
    }

    private final XYPair[] indexToCoordinate;
    private final Map<XYPair, Integer> coordinateToIndex;

    private final int[][] distances;
    private final Set<Integer>[][] next;

    public FullMapGraph(FullMap fullMap) {
        if (fullMap == null)
            throw new IllegalArgumentException("fullMap must not be null");

        List<XYPair> validNodes = fullMap.nodes().keySet().stream()
                .filter(coordinate -> !fullMap.nodes().get(coordinate).isWater())
                .toList();
        int n = validNodes.size();

        indexToCoordinate = validNodes.toArray(new XYPair[0]);
        coordinateToIndex = new HashMap<>(n);
        for (int i = 0; i < n; i++)
            coordinateToIndex.put(indexToCoordinate[i], i);

        this.distances = new int[n][n];
        this.next = (Set<Integer>[][]) new Set[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    distances[i][j] = 0;
                    next[i][j] = Collections.emptySet();
                } else {
                    distances[i][j] = Short.MAX_VALUE;
                    next[i][j] = new HashSet<>();
                }
            }
        }

        for (int i = 0; i < n; i++) {
            XYPair coordinateFrom = indexToCoordinate[i];
            FullMapNode nodeFrom = fullMap.nodes().get(coordinateFrom);

            List<XYPair> traversableAdjacentNeighbors = coordinateFrom.getAdjacentNeighbors(fullMap.size()).stream()
                    .filter(coordinateToIndex::containsKey)
                    .toList();

            for (XYPair coordinateTo : traversableAdjacentNeighbors) {
                int j = coordinateToIndex.get(coordinateTo);
                FullMapNode nodeTo = fullMap.nodes().get(coordinateTo);

                distances[i][j] = getMovementCost(nodeFrom.terrain(), nodeTo.terrain());
                next[i][j].add(j);
            }
        }

        for (int k = 0; k < n; k++)
            for (int i = 0; i < n; i++) {
                if (distances[i][k] == Short.MAX_VALUE)
                    continue;

                for (int j = 0; j < n; j++) {
                    if (distances[k][j] == Short.MAX_VALUE)
                        continue;

                    int currentCost = distances[i][j];
                    int newCost = distances[i][k] + distances[k][j];

                    if (currentCost > newCost) {
                        distances[i][j] = newCost;
                        next[i][j].clear();
                        next[i][j].addAll(next[i][k]);
                    } else if (currentCost == newCost)
                        next[i][j].addAll(next[i][k]);
                }
            }
    }

    public int getDistance(XYPair start, XYPair end) {
        if (start == null)
            throw new IllegalArgumentException("start must not be null");
        if (end == null)
            throw new IllegalArgumentException("end must not be null");

        int startIdx = coordinateToIndex.get(start);
        int endIdx = coordinateToIndex.get(end);

        return distances[startIdx][endIdx];
    }

    private record PathState(XYPair coordinate, List<XYPair> path) {
    }

    public List<List<XYPair>> getAllPaths(XYPair start, XYPair end) {
        if (start == null)
            throw new IllegalArgumentException("start must not be null");
        if (end == null)
            throw new IllegalArgumentException("end must not be null");

        logger.debug("Starting BFS path search from {} to {}", start, end);

        List<List<XYPair>> allPaths = new ArrayList<>();

        int endIdx = coordinateToIndex.get(end);

        Queue<PathState> queue = new ArrayDeque<>();
        queue.add(new PathState(start, List.of(start)));

        while (!queue.isEmpty()) {
            PathState entry = queue.remove();
            XYPair current = entry.coordinate();
            List<XYPair> currentPath = entry.path();

            logger.trace("Visiting node {} with path {}", current, currentPath);

            if (current.equals(end)) {
                logger.debug("Finished full path: {}", currentPath);
                allPaths.add(currentPath);
                continue;
            }

            int currentIdx = coordinateToIndex.get(current);
            Set<Integer> nextIndices = next[currentIdx][endIdx];

            Set<XYPair> nextCoordinates = new HashSet<>();
            for (int idx : nextIndices)
                nextCoordinates.add(indexToCoordinate[idx]);

            logger.trace("Expanding {} -> next {}", current, nextCoordinates);

            for (XYPair nextCoordinate : nextCoordinates) {
                if (currentPath.contains(nextCoordinate))
                    throw new RuntimeException("Cycling path found!");

                List<XYPair> newPath = new ArrayList<>(currentPath);
                newPath.add(nextCoordinate);

                logger.trace("Adding to queue: {} via path {}", nextCoordinate, newPath);
                queue.add(new PathState(nextCoordinate, newPath));
            }
        }

        logger.debug("Finished BFS search: total {} paths found from {} to {}", allPaths.size(), start, end);

        return allPaths;
    }
}