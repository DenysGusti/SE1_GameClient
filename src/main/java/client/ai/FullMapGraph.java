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

    private static final Map<ETerrain, Short> terrainMovementCost =
            Map.of(
                    ETerrain.Grass, (short) 1,
                    ETerrain.Mountain, (short) 2
            );

    private static short getMovementCost(ETerrain from, ETerrain to) {
        return (short) (terrainMovementCost.get(from) + terrainMovementCost.get(to));
    }

    private final XYPair[] indexToCoordinate;
    private final Map<XYPair, Short> coordinateToIndex;

    private final short[][] distances;
    private final short[][][] next;

    @SuppressWarnings("unchecked")
    public FullMapGraph(FullMap fullMap) {
        if (fullMap == null)
            throw new IllegalArgumentException("fullMap must not be null");

        List<XYPair> validNodes = fullMap.nodes().keySet().stream()
                .filter(coordinate -> !fullMap.nodes().get(coordinate).isWater())
                .toList();
        int n = validNodes.size();

        indexToCoordinate = validNodes.toArray(new XYPair[0]);
        coordinateToIndex = new HashMap<>(n);
        for (short i = 0; i < n; i++)
            coordinateToIndex.put(indexToCoordinate[i], i);

        distances = new short[n][n];
        next = new short[n][n][];
        var tempNext = (Set<Short>[][]) new Set[n][n];

        for (short i = 0; i < n; ++i) {
            for (short j = 0; j < n; ++j) {
                if (i == j) {
                    distances[i][j] = 0;
                    tempNext[i][j] = Collections.emptySet();
                } else {
                    distances[i][j] = Short.MAX_VALUE;
                    tempNext[i][j] = new HashSet<>();
                }
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

        for (short k = 0; k < n; ++k)
            for (short i = 0; i < n; ++i) {
                if (distances[i][k] == Short.MAX_VALUE)
                    continue;

                for (short j = 0; j < n; ++j) {
                    if (distances[k][j] == Short.MAX_VALUE)
                        continue;

                    short currentCost = distances[i][j];
                    int newCost = distances[i][k] + distances[k][j];
                    if (newCost >= Short.MAX_VALUE)
                        throw new RuntimeException("Cost Overflow!");

                    if (currentCost > newCost) {
                        distances[i][j] = (short) newCost;
                        tempNext[i][j].clear();
                        tempNext[i][j].addAll(tempNext[i][k]);
                    } else if (currentCost == newCost)
                        tempNext[i][j].addAll(tempNext[i][k]);
                }
            }

        for (short i = 0; i < n; ++i)
            for (short j = 0; j < n; ++j) {
                Set<Short> nextIndices = tempNext[i][j];
                short[] arr = new short[nextIndices.size()];
                short idx = 0;
                for (short val : nextIndices)
                    arr[idx++] = val;
                next[i][j] = arr;
            }

        short maxDistance = 0;

        for (short[] distanceFrom : distances)
            for (short distance : distanceFrom) {
                if (distance == Short.MAX_VALUE)
                    throw new RuntimeException("Uninitialized Cost!");

                if (distance > maxDistance)
                    maxDistance = distance;
            }

        logger.debug("Max Distance: {}", maxDistance);
    }

    public int getDistance(XYPair start, XYPair end) {
        if (start == null)
            throw new IllegalArgumentException("start must not be null");
        if (end == null)
            throw new IllegalArgumentException("end must not be null");

        short startIdx = coordinateToIndex.get(start);
        short endIdx = coordinateToIndex.get(end);

        return distances[startIdx][endIdx];
    }

    public int getDistance(List<XYPair> waypoints) {
        if (waypoints == null)
            throw new IllegalArgumentException("waypoints must not be null");
        if (waypoints.size() <= 1)
            throw new IllegalArgumentException("waypoint must have at least start and end");

        int totalDistance = 0;
        for (int i = 0; i < waypoints.size() - 1; ++i)
            totalDistance += getDistance(waypoints.get(i), waypoints.get(i + 1));

        return totalDistance;
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

        short endIdx = coordinateToIndex.get(end);

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

            short currentIdx = coordinateToIndex.get(current);
            short[] nextIndices = next[currentIdx][endIdx];

            Set<XYPair> nextCoordinates = new HashSet<>();
            for (short idx : nextIndices)
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

    public List<List<XYPair>> getAllPaths(List<XYPair> waypoints) {
        if (waypoints == null)
            throw new IllegalArgumentException("waypoints must not be null");
        if (waypoints.size() <= 1)
            throw new IllegalArgumentException("waypoint must have at least start and end");

        List<List<XYPair>> currentPaths = new ArrayList<>();
        currentPaths.add(new ArrayList<>(List.of(waypoints.getFirst())));

        for (int i = 0; i < waypoints.size() - 1; ++i) {
            XYPair startSegment = waypoints.get(i);
            XYPair endSegment = waypoints.get(i + 1);

            List<List<XYPair>> segmentPaths = getAllPaths(startSegment, endSegment);

            if (segmentPaths.isEmpty())
                throw new RuntimeException("No path found between waypoints " + startSegment + " and " + endSegment);

            List<List<XYPair>> nextPaths = new ArrayList<>();

            for (List<XYPair> existingPath : currentPaths)
                for (List<XYPair> segment : segmentPaths) {
                    List<XYPair> combined = new ArrayList<>(existingPath);
                    // skip the first element to avoid duplicating the start node
                    combined.addAll(segment.subList(1, segment.size()));
                    nextPaths.add(combined);
                }
            currentPaths = nextPaths;
        }

        logger.debug("Found {} total variations for multi-stop path", currentPaths.size());
        return currentPaths;
    }
}