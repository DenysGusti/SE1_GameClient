package client.network.fromclient;

import client.data.ETerrain;
import client.data.PlayerInformation;
import client.data.UniquePlayerIdentifier;
import client.data.XYPair;
import client.data.fromclient.EMove;
import client.data.fromclient.HalfMap;
import messagesbase.messagesfromclient.PlayerHalfMap;
import messagesbase.messagesfromclient.PlayerHalfMapNode;
import messagesbase.messagesfromclient.PlayerMove;
import messagesbase.messagesfromclient.PlayerRegistration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class FromClientConverter {
    private static final Logger logger = LoggerFactory.getLogger(FromClientConverter.class);
    private static final Map<EMove, messagesbase.messagesfromclient.EMove> moveConverter =
            Map.of(
                    EMove.Up, messagesbase.messagesfromclient.EMove.Up,
                    EMove.Left, messagesbase.messagesfromclient.EMove.Left,
                    EMove.Right, messagesbase.messagesfromclient.EMove.Right,
                    EMove.Down, messagesbase.messagesfromclient.EMove.Down
            );
    private static final Map<ETerrain, messagesbase.messagesfromclient.ETerrain> terrainConverter =
            Map.of(
                    ETerrain.Grass, messagesbase.messagesfromclient.ETerrain.Grass,
                    ETerrain.Mountain, messagesbase.messagesfromclient.ETerrain.Mountain,
                    ETerrain.Water, messagesbase.messagesfromclient.ETerrain.Water
            );

    public PlayerRegistration convertPlayerInformation(PlayerInformation playerInformation) {
        if (playerInformation == null)
            throw new IllegalArgumentException("playerInformation must not be null");

        return new PlayerRegistration(
                playerInformation.firstName(),
                playerInformation.lastName(),
                playerInformation.uaccount()
        );
    }

    public PlayerHalfMap convertHalfMap(UniquePlayerIdentifier uniquePlayerIdentifier, HalfMap halfMap) {
        if (uniquePlayerIdentifier == null)
            throw new IllegalArgumentException("uniquePlayerIdentifier must not be null");
        if (halfMap == null)
            throw new IllegalArgumentException("halfMap must not be null");

        var nodes = new ArrayList<PlayerHalfMapNode>();
        Set<XYPair> potentialForts = halfMap.potentialForts();

        halfMap.nodes().forEach((coordinate, terrain) -> {
            boolean isMyFort = potentialForts.contains(coordinate);
            nodes.add(new PlayerHalfMapNode(coordinate.x(), coordinate.y(), isMyFort, terrainConverter.get(terrain)));
        });

        return new PlayerHalfMap(uniquePlayerIdentifier.uniquePlayerID(), nodes);
    }

    public PlayerMove convertMove(UniquePlayerIdentifier uniquePlayerIdentifier, EMove move) {
        if (uniquePlayerIdentifier == null)
            throw new IllegalArgumentException("uniquePlayerIdentifier must not be null");
        if (move == null)
            throw new IllegalArgumentException("move must not be null");

        return PlayerMove.of(uniquePlayerIdentifier.uniquePlayerID(), moveConverter.get(move));
    }
}