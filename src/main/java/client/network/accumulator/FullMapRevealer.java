package client.network.accumulator;

import client.data.XYPair;
import client.data.fromserver.FullMap;
import client.data.fromserver.FullMapNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FullMapRevealer {
    private static final Logger logger = LoggerFactory.getLogger(FullMapRevealer.class);

    public FullMap revealCoordinatesFromMyPlayer(FullMap fullMap) {
        Objects.requireNonNull(fullMap, "fullMap must not be null");

        Set<XYPair> coordinatesToReveal = getCoordinatesToRevealFromMyPlayer(fullMap);
        Map<XYPair, FullMapNode> newNodes = new HashMap<>(fullMap.nodes());

        coordinatesToReveal.forEach(coordinate -> newNodes.replace(
                coordinate, newNodes.get(coordinate).withIsRevealed(coordinatesToReveal.contains(coordinate))
        ));

        return fullMap.withNodes(newNodes);
    }

    public FullMap revealMyTreasureFromMyPlayer(FullMap fullMap) {
        Objects.requireNonNull(fullMap, "fullMap must not be null");
        return fullMap.withMyTreasurePosition(fullMap.myPlayerPosition());
    }

    // other fields are from lhs
    public FullMap combineRevealedNodes(FullMap lhs, FullMap rhs) {
        Objects.requireNonNull(lhs, "lhs must not be null");
        Objects.requireNonNull(rhs, "rhs must not be null");

        Map<XYPair, FullMapNode> newNodes = new HashMap<>();

        lhs.nodes().forEach((coordinate, lhsNode) -> {
            boolean isRevealed = lhsNode.isRevealed() || rhs.nodes().get(coordinate).isRevealed();
            newNodes.put(coordinate, lhsNode.withIsRevealed(isRevealed));
        });

        return lhs.withNodes(newNodes);
    }

    private Set<XYPair> getCoordinatesToRevealFromMyPlayer(FullMap fullMap) {
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
