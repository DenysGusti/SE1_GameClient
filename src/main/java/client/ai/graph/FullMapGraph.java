package client.ai.graph;

import client.data.XYPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class FullMapGraph {
    private static final Logger logger = LoggerFactory.getLogger(FullMapGraph.class);

    private final XYPair[] indexToCoordinate;
    private final Map<XYPair, Short> coordinateToIndex;
    private final short[][] distances;
    private final short[][][] next;

    public FullMapGraph(XYPair[] indexToCoordinate, Map<XYPair, Short> coordinateToIndex, short[][] distances, short[][][] next) {
        if (indexToCoordinate == null)
            throw new IllegalArgumentException("indexToCoordinate must not be null");
        if (coordinateToIndex == null)
            throw new IllegalArgumentException("coordinateToIndex must not be null");
        if (distances == null)
            throw new IllegalArgumentException("distances must not be null");
        if (next == null)
            throw new IllegalArgumentException("next must not be null");

        this.indexToCoordinate = indexToCoordinate;
        this.coordinateToIndex = coordinateToIndex;
        this.distances = distances;
        this.next = next;
    }

    public int getDistance(XYPair start, XYPair end) {
        if (start == null)
            throw new IllegalArgumentException("start must not be null");
        if (end == null)
            throw new IllegalArgumentException("end must not be null");

        short startIdx = Objects.requireNonNull(coordinateToIndex.get(start), "startIdx must not be null");
        short endIdx = Objects.requireNonNull(coordinateToIndex.get(end), "endIdx must not be null");

        return distances[startIdx][endIdx];
    }

    public int getDistance(List<XYPair> waypoints) {
        if (waypoints == null)
            throw new IllegalArgumentException("waypoints must not be null");
        if (waypoints.size() <= 1)
            throw new IllegalArgumentException("waypoint must have at least start and end");

        int totalDistance = 0;
        for (int i = 0; i < waypoints.size() - 1; ++i)
            totalDistance += getDistance(waypoints.get(i), waypoints.get(i + 1));

        return totalDistance;
    }

    private record PathState(XYPair coordinate, List<XYPair> path) {
    }

    public List<List<XYPair>> getAllPaths(XYPair start, XYPair end) {
        if (start == null)
            throw new IllegalArgumentException("start must not be null");
        if (end == null)
            throw new IllegalArgumentException("end must not be null");

        List<List<XYPair>> allPaths = new ArrayList<>();

        short endIdx = Objects.requireNonNull(coordinateToIndex.get(end), "endIdx must not be null");

        Queue<PathState> queue = new ArrayDeque<>();
        queue.add(new PathState(start, List.of(start)));

        while (!queue.isEmpty()) {
            PathState entry = queue.remove();
            XYPair current = entry.coordinate();
            List<XYPair> currentPath = entry.path();

            if (current.equals(end)) {
                allPaths.add(currentPath);
                continue;
            }

            short currentIdx = Objects.requireNonNull(coordinateToIndex.get(current), "currentIdx must not be null");
            short[] nextIndices = next[currentIdx][endIdx];

            Set<XYPair> nextCoordinates = new HashSet<>();
            for (short idx : nextIndices)
                nextCoordinates.add(indexToCoordinate[idx]);

            for (XYPair nextCoordinate : nextCoordinates) {
                if (currentPath.contains(nextCoordinate))
                    throw new RuntimeException("Cycling path found!");

                List<XYPair> newPath = new ArrayList<>(currentPath);
                newPath.add(nextCoordinate);

                queue.add(new PathState(nextCoordinate, newPath));
            }
        }

        return allPaths;
    }

    public List<List<XYPair>> getAllPaths(List<XYPair> waypoints) {
        if (waypoints == null)
            throw new IllegalArgumentException("waypoints must not be null");
        if (waypoints.size() <= 1)
            throw new IllegalArgumentException("waypoint must have at least start and end");

        List<List<XYPair>> currentPaths = new ArrayList<>();
        currentPaths.add(new ArrayList<>(List.of(waypoints.getFirst())));

        for (int i = 0; i < waypoints.size() - 1; ++i) {
            XYPair startSegment = waypoints.get(i);
            XYPair endSegment = waypoints.get(i + 1);

            List<List<XYPair>> segmentPaths = getAllPaths(startSegment, endSegment);

            if (segmentPaths.isEmpty())
                throw new RuntimeException("No path found between waypoints " + startSegment + " and " + endSegment);

            List<List<XYPair>> nextPaths = new ArrayList<>();

            for (List<XYPair> existingPath : currentPaths)
                for (List<XYPair> segment : segmentPaths) {
                    List<XYPair> combined = new ArrayList<>(existingPath);
                    // skip the first element to avoid duplicating the start node
                    combined.addAll(segment.subList(1, segment.size()));
                    nextPaths.add(combined);
                }
            currentPaths = nextPaths;
        }

        logger.debug("Found {} total variations for multi-stop path", currentPaths.size());
        for (var path : currentPaths)
            logger.trace("Path: {}", path);
        return currentPaths;
    }
}