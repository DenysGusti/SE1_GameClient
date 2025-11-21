package client.ai;

import client.data.ETerrain;
import client.data.XYPair;
import client.data.fromserver.FullMap;
import client.data.fromserver.FullMapNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

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

    private final Map<XYPair, Map<XYPair, Integer>> distance;
    private final Map<XYPair, Map<XYPair, Set<XYPair>>> next;

    public FullMapGraph(FullMap fullMap) {
        if (fullMap == null)
            throw new IllegalArgumentException("fullMap must not be null");

        distance = new HashMap<>();
        next = new HashMap<>();

        Set<XYPair> coordinates = fullMap.nodes().keySet().stream()
                .filter(coordinate -> !fullMap.nodes().get(coordinate).isWater())
                .collect(Collectors.toSet());

        coordinates.forEach(
                (coordinateFrom) -> {
                    Map<XYPair, Integer> innerDistanceMap = new HashMap<>();
                    Map<XYPair, Set<XYPair>> innerNextMap = new HashMap<>();
                    distance.put(coordinateFrom, innerDistanceMap);
                    next.put(coordinateFrom, innerNextMap);

                    coordinates.forEach(
                            (coordinateTo) -> {
                                if (coordinateFrom.equals(coordinateTo)) {
                                    innerDistanceMap.put(coordinateTo, 0);
                                    innerNextMap.put(coordinateTo, Collections.emptySet());
                                } else {
                                    innerDistanceMap.put(coordinateTo, (int) Short.MAX_VALUE);
                                    innerNextMap.put(coordinateTo, new HashSet<>());
                                }
                            }
                    );
                }
        );

        coordinates.forEach((coordinateFrom) -> {
            FullMapNode nodeFrom = fullMap.nodes().get(coordinateFrom);

            coordinateFrom.getAdjacentNeighbors(fullMap.size()).stream()
                    .filter(coordinate -> !fullMap.nodes().get(coordinate).isWater())
                    .forEach((coordinateTo) -> {
                        FullMapNode nodeTo = fullMap.nodes().get(coordinateTo);
                        int cost = getMovementCost(nodeFrom.terrain(), nodeTo.terrain());
                        distance.get(coordinateFrom).put(coordinateTo, cost);
                        next.get(coordinateFrom).get(coordinateTo).add(coordinateTo);
                    });
        });

        for (XYPair coordinateK : coordinates)
            for (XYPair coordinateI : coordinates)
                for (XYPair coordinateJ : coordinates) {
                    int dist_ik = distance.get(coordinateI).get(coordinateK);
                    int dist_kj = distance.get(coordinateK).get(coordinateJ);

                    int newCost = dist_ik + dist_kj;
                    int currentCost = distance.get(coordinateI).get(coordinateJ);

                    if (currentCost > newCost) {
                        distance.get(coordinateI).put(coordinateJ, newCost);
                        next.get(coordinateI).put(coordinateJ, new HashSet<>(next.get(coordinateI).get(coordinateK)));
                    } else if (currentCost == newCost)
                        next.get(coordinateI).get(coordinateJ).addAll(next.get(coordinateI).get(coordinateK));
                }
    }

    public int getDistance(XYPair start, XYPair end) {
        if (start == null)
            throw new IllegalArgumentException("start must not be null");
        if (end == null)
            throw new IllegalArgumentException("end must not be null");

        return distance.get(start).get(end);
    }

    private record QueueEntry(XYPair coordinate, List<XYPair> path) {
    }

    public List<List<XYPair>> getAllPaths(XYPair start, XYPair end) {
        if (start == null)
            throw new IllegalArgumentException("start must not be null");
        if (end == null)
            throw new IllegalArgumentException("end must not be null");

        logger.debug("Starting BFS path search from {} to {}", start, end);

        List<List<XYPair>> allPaths = new ArrayList<>();
        Queue<QueueEntry> queue = new ArrayDeque<>();

        queue.add(new QueueEntry(start, List.of(start)));

        while (!queue.isEmpty()) {
            QueueEntry entry = queue.remove();
            XYPair current = entry.coordinate();
            List<XYPair> currentPath = entry.path();

            logger.trace("Visiting node {} with path {}", current, currentPath);

            if (current.equals(end)) {
                logger.debug("Finished full path: {}", currentPath);
                allPaths.add(currentPath);
                continue;
            }

            Set<XYPair> nextCoordinates = next.get(current).get(end);

            logger.trace("Expanding {} -> next {}", current, nextCoordinates);

            for (XYPair nextCoordinate : nextCoordinates) {
                List<XYPair> newPath = new ArrayList<>(currentPath);
                newPath.add(nextCoordinate);

                logger.trace("Adding to queue: {} via path {}", nextCoordinate, newPath);

                queue.add(new QueueEntry(nextCoordinate, newPath));
            }
        }

        logger.debug("Finished BFS search: total {} paths found from {} to {}", allPaths.size(), start, end);

        return allPaths;
    }
}
