package client.ai.utilities;

import client.data.ETerrain;
import client.data.XYPair;
import client.data.fromserver.FullMap;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FullMapUtilities {
    private static final XYPair HALF_MAP_SIZE = new XYPair(10, 5);

    public List<XYPair> getUnrevealedGrassNodes(FullMap fullMap, boolean onMySide) {
        if (fullMap == null)
            throw new IllegalArgumentException("fullMap is null");

        return fullMap.nodes().entrySet().stream()
                .filter(e -> e.getValue().terrain() == ETerrain.Grass && !e.getValue().isRevealed())
                .map(Map.Entry::getKey)
                .filter(coordinate -> onMySide == isCoordinateOnMySide(fullMap, coordinate))
                .toList();
    }

    public List<XYPair> getNeighborMountains(FullMap fullMap, List<XYPair> nodes) {
        if (fullMap == null)
            throw new IllegalArgumentException("fullMap is null");
        if (nodes == null)
            throw new IllegalArgumentException("nodes is null");

        return fullMap.nodes().entrySet().stream()
                .filter(entry -> entry.getValue().isMountain())
                .map(Map.Entry::getKey)
                .filter(mountain -> {
                    List<XYPair> neighbors = mountain.getAllNeighbors(fullMap.size());
                    return neighbors.stream().anyMatch(nodes::contains);
                })
                .toList();
    }

    // is coordinate on the same side as my fort
    private static boolean isCoordinateOnMySide(FullMap fullMap, XYPair coordinate) {
        if (fullMap == null)
            throw new IllegalArgumentException("fullMap is null");
        if (coordinate == null)
            throw new IllegalArgumentException("coordinate is null");

        Objects.requireNonNull(fullMap.myFortPosition(), "fullMap.myFortPosition() is null");

        if (fullMap.bottomRightCoordinate().x() >= HALF_MAP_SIZE.x()) {  // wide map, (20, 5)
            boolean isMyFortOnLeftSide = fullMap.myFortPosition().x() < HALF_MAP_SIZE.x();
            boolean isCoordinateOnLeftSide = coordinate.x() < HALF_MAP_SIZE.x();
            return isMyFortOnLeftSide == isCoordinateOnLeftSide;
        } else {  // tall map, (10, 10)
            boolean isFortOnTopSide = fullMap.myFortPosition().y() < HALF_MAP_SIZE.y();
            boolean isCoordinateOnTopSide = coordinate.y() < HALF_MAP_SIZE.y();
            return isFortOnTopSide == isCoordinateOnTopSide;
        }
    }
}
