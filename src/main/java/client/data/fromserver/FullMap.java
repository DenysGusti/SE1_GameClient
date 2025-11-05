package client.data.fromserver;

import client.data.XYPair;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public record FullMap(Map<XYPair, FullMapNode> nodes, XYPair size,
                      XYPair myPlayerPosition, XYPair enemyPlayerPosition,
                      XYPair myFortPosition, XYPair enemyFortPosition,
                      XYPair treasurePosition) {
    private static final Logger logger = LoggerFactory.getLogger(FullMap.class);

    private static final XYPair HALF_MAP_SIZE = new XYPair(10, 5);

    public FullMap(Map<XYPair, FullMapNode> nodes, XYPair size,
                   XYPair myPlayerPosition, XYPair enemyPlayerPosition,
                   XYPair myFortPosition, XYPair enemyFortPosition,
                   XYPair treasurePosition) {
        Objects.requireNonNull(nodes, "nodes must not be null");
        Objects.requireNonNull(size, "size must not be null");

        this.nodes = new HashMap<>(nodes);

        this.size = size;
        this.myPlayerPosition = myPlayerPosition;
        this.enemyPlayerPosition = enemyPlayerPosition;
        this.myFortPosition = myFortPosition;
        this.enemyFortPosition = enemyFortPosition;
        this.treasurePosition = treasurePosition;
    }

    @Override
    public Map<XYPair, FullMapNode> nodes() {
        return Collections.unmodifiableMap(nodes);
    }

    boolean isOnMySide(XYPair coordinate) {
        Objects.requireNonNull(coordinate, "coordinate must not be null");
        Objects.requireNonNull(myFortPosition, "myFortPosition must not be null");

        boolean isMyFortInUpperLeft = myFortPosition.x() < HALF_MAP_SIZE.x() && myFortPosition.y() < HALF_MAP_SIZE.y();
        boolean isCoordinateInUpperLeft = coordinate.x() < HALF_MAP_SIZE.x() && coordinate.y() < HALF_MAP_SIZE.y();
        return isMyFortInUpperLeft == isCoordinateInUpperLeft;
    }

    boolean isOnEnemySide(XYPair coordinate) {
        return !isOnMySide(coordinate);
    }

    Optional<XYPair> getOptionalMyPlayerPosition() {
        return Optional.ofNullable(myPlayerPosition);
    }

    Optional<XYPair> getOptionalEnemyPlayerPosition() {
        return Optional.ofNullable(enemyPlayerPosition);
    }

    Optional<XYPair> getOptionalMyFortPosition() {
        return Optional.ofNullable(myFortPosition);
    }

    Optional<XYPair> getOptionalEnemyFortPosition() {
        return Optional.ofNullable(enemyFortPosition);
    }

    Optional<XYPair> getOptionalTreasurePosition() {
        return Optional.ofNullable(treasurePosition);
    }
}
