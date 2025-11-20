package client.ai;

import client.data.ETerrain;
import client.data.XYPair;
import client.data.fromserver.FullMap;
import client.data.fromserver.FullMapNode;

import java.util.*;
import java.util.stream.Collectors;

public class FullMapGraph {
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
        Objects.requireNonNull(fullMap, "fullMap must not be null");

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
        Objects.requireNonNull(start, "start must not be null");
        Objects.requireNonNull(end, "end must not be null");
        return distance.get(start).get(end);
    }

    public List<List<XYPair>> getAllPaths(XYPair start, XYPair end) {
        Objects.requireNonNull(start, "start must not be null");
        Objects.requireNonNull(end, "end must not be null");

        List<List<XYPair>> allPaths = new ArrayList<>();
        reconstructPaths(start, end, List.of(start), allPaths);

        return allPaths;
    }

    private void reconstructPaths(XYPair current, XYPair end, List<XYPair> currentPath, List<List<XYPair>> allPaths) {
        if (current.equals(end)) {
            allPaths.add(new ArrayList<>(currentPath));
            return;
        }

        Set<XYPair> nextCoordinates = next.get(current).get(end);
        if (nextCoordinates.isEmpty())
            return;

        for (XYPair nextCoordinate : nextCoordinates) {
            if (currentPath.contains(nextCoordinate)) {
                throw new RuntimeException("???");
            }

            List<XYPair> nextPath = new ArrayList<>(currentPath);
            nextPath.add(nextCoordinate);

            reconstructPaths(nextCoordinate, end, nextPath, allPaths);
        }
    }
}
