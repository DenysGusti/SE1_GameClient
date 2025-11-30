package client.network.accumulator;

import client.data.XYPair;
import client.data.fromserver.FullMap;
import client.data.fromserver.FullMapNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FullMapRevealer {
    private static final Logger logger = LoggerFactory.getLogger(FullMapRevealer.class);

    public FullMap revealCoordinatesFromMyPlayer(FullMap fullMap) {
        if (fullMap == null)
            throw new IllegalArgumentException("fullMap must not be null");

        Set<XYPair> coordinatesToReveal = getCoordinatesToRevealFromMyPlayer(fullMap);
        Map<XYPair, FullMapNode> newNodes = new HashMap<>(fullMap.nodes());

        coordinatesToReveal.forEach(coordinate -> newNodes.replace(
                coordinate, newNodes.get(coordinate).withIsRevealed(coordinatesToReveal.contains(coordinate))
        ));

        return fullMap.withNodes(newNodes);
    }

    public FullMap combineRevealedMyTreasure(FullMap oldFullMap, FullMap newFullMap) {
        if (oldFullMap == null)
            throw new IllegalArgumentException("oldFullMap must not be null");
        if (newFullMap == null)
            throw new IllegalArgumentException("newFullMap must not be null");

        XYPair myTreasurePosition = oldFullMap.getOptionalMyTreasurePosition().orElse(newFullMap.myTreasurePosition());

        // other fields are from new full map
        return newFullMap.withMyTreasurePosition(myTreasurePosition);
    }

    public FullMap combineRevealedNodes(FullMap oldFullMap, FullMap newFullMap) {
        if (oldFullMap == null)
            throw new IllegalArgumentException("oldFullMap must not be null");
        if (newFullMap == null)
            throw new IllegalArgumentException("newFullMap must not be null");

        Map<XYPair, FullMapNode> newNodes = new HashMap<>(newFullMap.nodes());

        // old full map can be smaller
        oldFullMap.nodes().forEach((coordinate, oldNode) -> {
            boolean isRevealed = oldNode.isRevealed() || newFullMap.nodes().get(coordinate).isRevealed();
            newNodes.replace(coordinate, oldNode.withIsRevealed(isRevealed));
        });

        // other fields are from new full map
        return newFullMap.withNodes(newNodes);
    }

    private Set<XYPair> getCoordinatesToRevealFromMyPlayer(FullMap fullMap) {
        if (fullMap == null)
            throw new IllegalArgumentException("fullMap must not be null");

        return fullMap.getOptionalMyPlayerPosition()
                .map(coordinate -> {
                    if (fullMap.nodes().get(coordinate).isMountain())
                        return coordinate.getAllNeighborsWithThis(fullMap.size()).stream();
                    else
                        return Stream.of(coordinate);
                })
                .orElse(Stream.empty())
                .collect(Collectors.toSet());
    }
}
