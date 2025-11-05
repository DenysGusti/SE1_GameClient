package client.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public record XYPair(int x, int y) {
    private static final Logger logger = LoggerFactory.getLogger(XYPair.class);

    Set<XYPair> getAdjacentNeighbors(XYPair gridSize) {
        Objects.requireNonNull(gridSize, "gridSize must not be null");

        var gridNeighbors = new HashSet<XYPair>();

        if (x > 0)
            gridNeighbors.add(new XYPair(x - 1, y));
        if (x < gridSize.x() - 1)
            gridNeighbors.add(new XYPair(x + 1, y));
        if (y > 0)
            gridNeighbors.add(new XYPair(x, y - 1));
        if (y < gridSize.y() - 1)
            gridNeighbors.add(new XYPair(x, y + 1));

        return gridNeighbors;
    }

    Set<XYPair> getDiagonalNeighbors(XYPair gridSize) {
        Objects.requireNonNull(gridSize, "gridSize must not be null");

        var gridNeighbors = new HashSet<XYPair>();

        if (x > 0 && y > 0)
            gridNeighbors.add(new XYPair(x - 1, y - 1));
        if (x > 0 && y < gridSize.y() - 1)
            gridNeighbors.add(new XYPair(x - 1, y + 1));
        if (x < gridSize.x() - 1 && y > 0)
            gridNeighbors.add(new XYPair(x + 1, y - 1));
        if (x < gridSize.x() - 1 && y < gridSize.y() - 1)
            gridNeighbors.add(new XYPair(x + 1, y + 1));

        return gridNeighbors;
    }

    Set<XYPair> getAllNeighbors(XYPair gridSize) {
        Objects.requireNonNull(gridSize, "gridSize must not be null");

        Set<XYPair> gridNeighbors = getAdjacentNeighbors(gridSize);
        gridNeighbors.addAll(getDiagonalNeighbors(gridSize));
        return gridNeighbors;
    }

    boolean isOnBorder(XYPair gridSize) {
        Objects.requireNonNull(gridSize, "gridSize must not be null");

        return x == 0 || x == gridSize.x() - 1 || y == 0 || y == gridSize.y() - 1;
    }

    boolean isOnCorner(XYPair gridSize) {
        Objects.requireNonNull(gridSize, "gridSize must not be null");

        return x == 0 && y == 0 || x == 0 && y == gridSize.y() - 1 ||
                x == gridSize.x() - 1 && y == 0 || x == gridSize.x() - 1 && y == gridSize.y() - 1;
    }
}
