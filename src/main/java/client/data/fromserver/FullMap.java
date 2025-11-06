package client.data.fromserver;

import client.data.XYPair;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record FullMap(Map<XYPair, FullMapNode> nodes, XYPair size,
                      XYPair myPlayerPosition, XYPair enemyPlayerPosition,
                      XYPair myFortPosition, XYPair enemyFortPosition,
                      XYPair myTreasurePosition) {
    private static final Logger logger = LoggerFactory.getLogger(FullMap.class);
    private static final XYPair HALF_MAP_SIZE = new XYPair(10, 5);

    public static FullMap emptyFullMap() {
        return new FullMap(Map.of(), new XYPair(0, 0), null, null, null, null, null);
    }

    public FullMap(Map<XYPair, FullMapNode> nodes, XYPair size,
                   XYPair myPlayerPosition, XYPair enemyPlayerPosition,
                   XYPair myFortPosition, XYPair enemyFortPosition,
                   XYPair myTreasurePosition) {
        this.nodes = Map.copyOf(Objects.requireNonNull(nodes, "nodes must not be null"));
        this.size = Objects.requireNonNull(size, "size must not be null");
        this.myPlayerPosition = myPlayerPosition;
        this.enemyPlayerPosition = enemyPlayerPosition;
        this.myFortPosition = myFortPosition;
        this.enemyFortPosition = enemyFortPosition;
        this.myTreasurePosition = myTreasurePosition;
    }

    public FullMap withRevealedCoordinatesFromMyPlayer() {
        Set<XYPair> coordinatesToReveal = getCoordinatesToRevealFromMyPlayer();
        Map<XYPair, FullMapNode> newNodes = new HashMap<>(this.nodes);

        coordinatesToReveal.forEach(coordinate -> newNodes.replace(
                coordinate, newNodes.get(coordinate).withIsRevealed(coordinatesToReveal.contains(coordinate))
        ));

        return new FullMap(newNodes, this.size,
                this.myPlayerPosition, this.enemyPlayerPosition,
                this.myFortPosition, this.enemyFortPosition,
                this.myTreasurePosition);
    }

    public FullMap withRevealedMyTreasureFromMyPlayer() {
        return new FullMap(nodes, this.size,
                this.myPlayerPosition, this.enemyPlayerPosition,
                this.myFortPosition, this.enemyFortPosition,
                this.myPlayerPosition);
    }

    public FullMap withCombinedRevealedNodesFromOtherFullMap(FullMap otherFullMap) {
        Objects.requireNonNull(otherFullMap, "otherFullMap must not be null");
        Map<XYPair, FullMapNode> newNodes = new HashMap<>(this.nodes);

        otherFullMap.nodes().forEach((coordinate, node) -> {
            boolean isRevealed = this.nodes.get(coordinate).isRevealed() || node.isRevealed();
            newNodes.replace(coordinate, newNodes.get(coordinate).withIsRevealed(isRevealed));
        });

        return new FullMap(newNodes, this.size,
                this.myPlayerPosition, this.enemyPlayerPosition,
                this.myFortPosition, this.enemyFortPosition,
                this.myPlayerPosition);
    }

    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    public boolean isOnMySide(XYPair coordinate) {
        Objects.requireNonNull(coordinate, "coordinate must not be null");
        Objects.requireNonNull(myFortPosition, "myFortPosition must not be null");

        boolean isMyFortInUpperLeft = myFortPosition.x() < HALF_MAP_SIZE.x() && myFortPosition.y() < HALF_MAP_SIZE.y();
        boolean isCoordinateInUpperLeft = coordinate.x() < HALF_MAP_SIZE.x() && coordinate.y() < HALF_MAP_SIZE.y();
        return isMyFortInUpperLeft == isCoordinateInUpperLeft;
    }

    public boolean isOnEnemySide(XYPair coordinate) {
        return !isOnMySide(coordinate);
    }

    public Optional<XYPair> getOptionalMyPlayerPosition() {
        return Optional.ofNullable(myPlayerPosition);
    }

    public Optional<XYPair> getOptionalEnemyPlayerPosition() {
        return Optional.ofNullable(enemyPlayerPosition);
    }

    public Optional<XYPair> getOptionalMyFortPosition() {
        return Optional.ofNullable(myFortPosition);
    }

    public Optional<XYPair> getOptionalEnemyFortPosition() {
        return Optional.ofNullable(enemyFortPosition);
    }

    Optional<XYPair> getOptionalMyTreasurePosition() {
        return Optional.ofNullable(myTreasurePosition);
    }

    private Set<XYPair> getCoordinatesToRevealFromMyPlayer() {
        return getOptionalMyPlayerPosition()
                .map(coordinate -> {
                    if (nodes.get(coordinate).isMountain())
                        return coordinate.getAllNeighborsWithThis(size).stream();
                    else
                        return Stream.of(coordinate);
                })
                .orElse(Stream.empty())
                .collect(Collectors.toSet());
    }
}
