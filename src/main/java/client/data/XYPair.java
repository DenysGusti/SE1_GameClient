package client.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public record XYPair(int x, int y) {
    private static final Logger logger = LoggerFactory.getLogger(XYPair.class);

    // grid starts from (0, 0)
    public List<XYPair> getAdjacentNeighbors(XYPair gridSize) {
        if (gridSize == null)
            throw new IllegalArgumentException("gridSize must not be null");

        var gridNeighbors = new ArrayList<XYPair>();

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

    // grid starts from (0, 0)
    public List<XYPair> getDiagonalNeighbors(XYPair gridSize) {
        if (gridSize == null)
            throw new IllegalArgumentException("gridSize must not be null");

        var gridNeighbors = new ArrayList<XYPair>();

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

    // grid starts from (0, 0)
    public List<XYPair> getAllNeighbors(XYPair gridSize) {
        if (gridSize == null)
            throw new IllegalArgumentException("gridSize must not be null");

        List<XYPair> gridNeighbors = getAdjacentNeighbors(gridSize);
        gridNeighbors.addAll(getDiagonalNeighbors(gridSize));
        return gridNeighbors;
    }

    // grid starts from (0, 0)
    public List<XYPair> getAllNeighborsWithThis(XYPair gridSize) {
        if (gridSize == null)
            throw new IllegalArgumentException("gridSize must not be null");

        List<XYPair> gridNeighbors = getAllNeighbors(gridSize);
        gridNeighbors.add(this);
        return gridNeighbors;
    }

    // grid starts from (0, 0)
    public boolean isOnBorder(XYPair gridSize) {
        if (gridSize == null)
            throw new IllegalArgumentException("gridSize must not be null");

        return x == 0 || x == gridSize.x() - 1 || y == 0 || y == gridSize.y() - 1;
    }

    // grid starts from (0, 0)
    public boolean isOnCorner(XYPair gridSize) {
        if (gridSize == null)
            throw new IllegalArgumentException("gridSize must not be null");

        return x == 0 && y == 0 || x == 0 && y == gridSize.y() - 1 ||
                x == gridSize.x() - 1 && y == 0 || x == gridSize.x() - 1 && y == gridSize.y() - 1;
    }
}
