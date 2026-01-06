package client.ai.utilities;

import client.data.XYPair;
import client.data.fromserver.FullMapNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class FullMapUtilities {
    private static final Logger logger = LoggerFactory.getLogger(FullMapUtilities.class);
    private static final XYPair HALF_MAP_SIZE = new XYPair(10, 5);

    private final boolean isMyFortOnTopOrLeftSide;

    public FullMapUtilities(XYPair myFortPosition) {
        if (myFortPosition == null)
            throw new IllegalArgumentException("myFortPosition is null");

        isMyFortOnTopOrLeftSide = myFortPosition.x() < HALF_MAP_SIZE.x() && myFortPosition.y() < HALF_MAP_SIZE.y();
    }

    public List<XYPair> getUnrevealedGrass(Map<XYPair, FullMapNode> fullMapNodes, boolean onMySide) {
        if (fullMapNodes == null)
            throw new IllegalArgumentException("fullMapNodes is null");

        return fullMapNodes.entrySet().stream()
                .filter(e -> e.getValue().isGrass() && !e.getValue().isRevealed())
                .map(Map.Entry::getKey)
                .filter(coordinate -> onMySide == isCoordinateOnMySide(coordinate))
                .toList();
    }

    public List<XYPair> getMountains(Map<XYPair, FullMapNode> fullMapNodes, boolean onMySide) {
        if (fullMapNodes == null)
            throw new IllegalArgumentException("fullMapNodes is null");

        return fullMapNodes.entrySet().stream()
                .filter(e -> e.getValue().isMountain())
                .map(Map.Entry::getKey)
                .filter(coordinate -> onMySide == isCoordinateOnMySide(coordinate))
                .toList();
    }

    public List<XYPair> getNeighbors(XYPair fullMapSize, List<XYPair> potentialNeighbors, List<XYPair> nodes) {
        if (fullMapSize == null)
            throw new IllegalArgumentException("fullMapSize is null");
        if (potentialNeighbors == null)
            throw new IllegalArgumentException("potentialNeighbors is null");
        if (nodes == null)
            throw new IllegalArgumentException("nodes is null");

        return potentialNeighbors.stream()
                .filter(neighbor -> {
                    List<XYPair> neighbors = neighbor.getAllNeighbors(fullMapSize);
                    return neighbors.stream().anyMatch(nodes::contains);
                })
                .toList();
    }

    // is coordinate on the same side as my fort
    private boolean isCoordinateOnMySide(XYPair coordinate) {
        if (coordinate == null)
            throw new IllegalArgumentException("coordinate is null");

        return isMyFortOnTopOrLeftSide == isCoordinateOnTopOrLeftSide(coordinate);
    }

    private static boolean isCoordinateOnTopOrLeftSide(XYPair coordinate) {
        if (coordinate == null)
            throw new IllegalArgumentException("coordinate is null");

        return coordinate.x() < HALF_MAP_SIZE.x() && coordinate.y() < HALF_MAP_SIZE.y();
    }
}
