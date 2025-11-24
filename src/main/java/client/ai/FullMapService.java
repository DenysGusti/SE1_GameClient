package client.ai;

import client.data.ETerrain;
import client.data.XYPair;
import client.data.fromserver.FullMap;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class FullMapService {
    private static final XYPair HALF_MAP_SIZE = new XYPair(10, 5);

    public Set<XYPair> getUnrevealedGrassNodesOnMySide(FullMap fullMap) {
        return fullMap.nodes().entrySet().stream()
                .filter(e -> e.getValue().terrain() == ETerrain.Grass && !e.getValue().isRevealed())
                .filter(e -> isOnMySide(fullMap, e.getKey()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    public Set<XYPair> getUnrevealedGrassNodesOnEnemySide(FullMap fullMap) {
        return fullMap.nodes().entrySet().stream()
                .filter(e -> e.getValue().terrain() == ETerrain.Grass && !e.getValue().isRevealed())
                .filter(e -> !isOnMySide(fullMap, e.getKey()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    // is coordinate on the same side as my fort
    private boolean isOnMySide(FullMap fullMap, XYPair coordinate) {
        if (fullMap == null)
            throw new IllegalArgumentException("fullMap must not be null");
        if (coordinate == null)
            throw new IllegalArgumentException("coordinate must not be null");

        Objects.requireNonNull(fullMap.myFortPosition(), "fullMap.myFortPosition() must not be null");

        boolean isWideMap = fullMap.bottomRightCoordinate().x() >= HALF_MAP_SIZE.x();

        if (isWideMap) {  // wide map, (20, 5)
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
