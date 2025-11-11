package client.ai;

import client.data.XYPair;
import client.data.fromserver.FullMap;

import java.util.Objects;

public class FullMapService {
    private static final XYPair HALF_MAP_SIZE = new XYPair(10, 5);

    // is coordinate on the same side as my fort
    public boolean isOnMySide(FullMap fullMap, XYPair coordinate) {
        Objects.requireNonNull(fullMap, "fullMap must not be null");
        Objects.requireNonNull(fullMap.myFortPosition(), "fullMap.myFortPosition() must not be null");
        Objects.requireNonNull(coordinate, "coordinate must not be null");

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

    public boolean isOnEnemySide(FullMap fullMap, XYPair coordinate) {
        return !isOnMySide(fullMap, coordinate);
    }
}
