package client.data.fromserver;

import client.data.XYPair;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public record FullMap(Map<XYPair, FullMapNode> nodes, XYPair topLeftCoordinate, XYPair bottomRightCoordinate,
                      XYPair myPlayerPosition, XYPair enemyPlayerPosition,
                      XYPair myFortPosition, XYPair enemyFortPosition,
                      XYPair myTreasurePosition, boolean isMyTreasureCollected) {
    private static final Logger logger = LoggerFactory.getLogger(FullMap.class);

    public static FullMap emptyFullMap() {
        return new FullMap(Map.of(), null, null, null, null, null, null, null, false);
    }

    public FullMap(Map<XYPair, FullMapNode> nodes, XYPair topLeftCoordinate, XYPair bottomRightCoordinate,
                   XYPair myPlayerPosition, XYPair enemyPlayerPosition,
                   XYPair myFortPosition, XYPair enemyFortPosition,
                   XYPair myTreasurePosition, boolean isMyTreasureCollected) {
        if (nodes == null)
            throw new IllegalArgumentException("nodes must not be null");

        this.nodes = Map.copyOf(nodes);
        this.topLeftCoordinate = topLeftCoordinate;
        this.bottomRightCoordinate = bottomRightCoordinate;
        this.myPlayerPosition = myPlayerPosition;
        this.enemyPlayerPosition = enemyPlayerPosition;
        this.myFortPosition = myFortPosition;
        this.enemyFortPosition = enemyFortPosition;
        this.myTreasurePosition = myTreasurePosition;
        this.isMyTreasureCollected = isMyTreasureCollected;
    }

    public FullMap withNodes(Map<XYPair, FullMapNode> newNodes) {
        if (newNodes == null)
            throw new IllegalArgumentException("newNodes must not be null");

        return new FullMap(newNodes, topLeftCoordinate, bottomRightCoordinate,
                myPlayerPosition, enemyPlayerPosition,
                myFortPosition, enemyFortPosition,
                myTreasurePosition, isMyTreasureCollected);
    }

    public FullMap withMyPlayerPosition(XYPair myPlayerPosition) {
        return new FullMap(nodes, topLeftCoordinate, bottomRightCoordinate,
                myPlayerPosition, enemyPlayerPosition,
                myFortPosition, enemyFortPosition,
                myTreasurePosition, isMyTreasureCollected);
    }

    public FullMap withMyTreasurePosition(XYPair newMyTreasurePosition) {
        return new FullMap(nodes, topLeftCoordinate, bottomRightCoordinate,
                myPlayerPosition, enemyPlayerPosition,
                myFortPosition, enemyFortPosition,
                newMyTreasurePosition, isMyTreasureCollected);
    }

    public FullMap withMyFortPosition(XYPair newMyFortPosition) {
        return new FullMap(nodes, topLeftCoordinate, bottomRightCoordinate,
                myPlayerPosition, enemyPlayerPosition,
                newMyFortPosition, enemyFortPosition,
                myTreasurePosition, isMyTreasureCollected);
    }

    public XYPair size() {
        if (nodes.isEmpty())
            return new XYPair(0, 0);

        if (bottomRightCoordinate == null)
            throw new IllegalStateException("bottomRightCoordinate must not be null");
        if (topLeftCoordinate == null)
            throw new IllegalStateException("topLeftCoordinate must not be null");

        return new XYPair(bottomRightCoordinate.x() - topLeftCoordinate.x() + 1,
                bottomRightCoordinate.y() - topLeftCoordinate.y() + 1);
    }

    public Optional<XYPair> getOptionalTopLeftCoordinate() {
        return Optional.ofNullable(topLeftCoordinate);
    }

    public Optional<XYPair> getOptionalBottomRightCoordinate() {
        return Optional.ofNullable(bottomRightCoordinate);
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

    public Optional<XYPair> getOptionalMyTreasurePosition() {
        return Optional.ofNullable(myTreasurePosition);
    }
}
