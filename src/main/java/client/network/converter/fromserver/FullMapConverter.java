package client.network.converter.fromserver;

import client.data.ETerrain;
import client.data.XYPair;
import client.data.fromserver.FullMap;

import messagesbase.messagesfromserver.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class FullMapConverter {
    private static final Logger logger = LoggerFactory.getLogger(FullMapConverter.class);

    private static final Map<messagesbase.messagesfromclient.ETerrain, ETerrain> terrainConverter =
            Map.of(
                    messagesbase.messagesfromclient.ETerrain.Grass, ETerrain.Grass,
                    messagesbase.messagesfromclient.ETerrain.Mountain, ETerrain.Mountain,
                    messagesbase.messagesfromclient.ETerrain.Water, ETerrain.Water
            );

    public FullMap convertFullMap(messagesbase.messagesfromserver.FullMap fullMap) {
        Objects.requireNonNull(fullMap, "fullMap must not be null");

        if (fullMap.isEmpty())
            return FullMap.emptyFullMap();

        Map<XYPair, client.data.fromserver.FullMapNode> nodes = new HashMap<>();

        var size = new XYPair(0, 0);
        XYPair myPlayerPosition = null;
        XYPair enemyPlayerPosition = null;
        XYPair myFortPosition = null;
        XYPair enemyFortPosition = null;
        XYPair myTreasurePosition = null;

        // for loop was much easier to read than streams
        for (var node : fullMap.getMapNodes()) {
            var coordinate = new XYPair(node.getX(), node.getY());
            nodes.put(coordinate, new client.data.fromserver.FullMapNode(terrainConverter.get(node.getTerrain()), false));

            if (node.getPlayerPositionState().representsMyPlayer())
                myPlayerPosition = coordinate;
            if (representsEnemyPlayer(node.getPlayerPositionState()))
                enemyPlayerPosition = coordinate;

            if (node.getFortState() == EFortState.MyFortPresent)
                myFortPosition = coordinate;
            else if (node.getFortState() == EFortState.EnemyFortPresent)
                enemyFortPosition = coordinate;

            if (node.getTreasureState() == ETreasureState.MyTreasureIsPresent)
                myTreasurePosition = coordinate;

            size = new XYPair(Math.max(size.x(), node.getX() + 1), Math.max(size.y(), node.getY() + 1));
        }

        return new FullMap(nodes, size,
                myPlayerPosition, enemyPlayerPosition,
                myFortPosition, enemyFortPosition,
                myTreasurePosition
        );
    }

    private static boolean representsEnemyPlayer(EPlayerPositionState state) {
        Objects.requireNonNull(state, "state must not be null");
        return state == EPlayerPositionState.EnemyPlayerPosition || state == EPlayerPositionState.BothPlayerPosition;
    }
}
