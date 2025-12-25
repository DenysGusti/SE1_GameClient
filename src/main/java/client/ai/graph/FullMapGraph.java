package client.ai.graph;

import client.ai.exception.PathException;
import client.data.XYPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class FullMapGraph {
    private static final Logger logger = LoggerFactory.getLogger(FullMapGraph.class);

    private final XYPair[] indexToCoordinate;
    private final Map<XYPair, Integer> coordinateToIndex;
    private final byte[][] distances;
    private final byte[][] next;

    public FullMapGraph(XYPair[] indexToCoordinate, Map<XYPair, Integer> coordinateToIndex,
                        byte[][] distances, byte[][] next) {
        if (indexToCoordinate == null)
            throw new IllegalArgumentException("indexToCoordinate is null");
        if (coordinateToIndex == null)
            throw new IllegalArgumentException("coordinateToIndex is null");
        if (distances == null)
            throw new IllegalArgumentException("distances is null");
        if (next == null)
            throw new IllegalArgumentException("next is null");

        this.indexToCoordinate = indexToCoordinate;
        this.coordinateToIndex = coordinateToIndex;
        this.distances = distances;
        this.next = next;
    }

    private void accessGuard(XYPair start, XYPair end) {
        if (start == null)
            throw new IllegalArgumentException("start is null");
        if (end == null)
            throw new IllegalArgumentException("end is null");

        if (!coordinateToIndex.containsKey(start))
            throw new NoSuchElementException("Coordinate " + start + " not found");
        if (!coordinateToIndex.containsKey(end))
            throw new NoSuchElementException("Coordinate " + end + " not found");
    }

    public int getDistance(XYPair start, XYPair end) {
        accessGuard(start, end);

        int startIdx = coordinateToIndex.get(start);
        int endIdx = coordinateToIndex.get(end);

        return Byte.toUnsignedInt(distances[startIdx][endIdx]);
    }

    public DistanceMatrix getDistanceMatrix(List<XYPair> nodes) {
        if (nodes == null)
            throw new IllegalArgumentException("nodes is null");

        int n = nodes.size();

        var dist = new byte[n * n];
        for (int i = 0; i < n; ++i)
            for (int j = 0; j < n; ++j) {
                int distance = getDistance(nodes.get(i), nodes.get(j));
                dist[i * n + j] = (byte) distance;
            }
        return new DistanceMatrix(dist, n);
    }

    public List<XYPair> getStepPathBetweenCoordinates(XYPair start, XYPair end) {
        accessGuard(start, end);

        int currentIdx = coordinateToIndex.get(start);
        int endIdx = coordinateToIndex.get(end);

        List<XYPair> stepPath = new ArrayList<>();
        stepPath.add(start);

        while (currentIdx != endIdx) {
            int nextIdx = Byte.toUnsignedInt(next[currentIdx][endIdx]);
            if (nextIdx >= indexToCoordinate.length)
                throw new PathException("No path exists between " + start + " and " + end);

            stepPath.add(indexToCoordinate[nextIdx]);
            if (stepPath.size() > indexToCoordinate.length)
                throw new PathException("Infinite loop detected in path data");

            currentIdx = nextIdx;
        }

        return stepPath;
    }

    public List<XYPair> getStepPathBetweenWaypoints(List<XYPair> waypoints) {
        if (waypoints == null)
            throw new IllegalArgumentException("waypoints is null");
        if (waypoints.size() <= 1)
            throw new IllegalArgumentException("waypoint must have at least start and end");

        List<XYPair> fullPath = new ArrayList<>();
        fullPath.add(waypoints.getFirst());

        for (int i = 0; i < waypoints.size() - 1; ++i) {
            XYPair startSegment = waypoints.get(i);
            XYPair endSegment = waypoints.get(i + 1);

            List<XYPair> segment = getStepPathBetweenCoordinates(startSegment, endSegment);
            // skip the first element to avoid duplicating the start node
            fullPath.addAll(segment.subList(1, segment.size()));
        }

        return fullPath;
    }
}