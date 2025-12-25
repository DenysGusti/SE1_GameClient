package client.ai.path;

import client.ai.graph.DistanceMatrix;
import client.ai.graph.FullMapGraph;
import client.data.XYPair;

import java.util.*;

public class ExpectedArrivalPathFactory {
    private static final int MAX_GRASS = 38;
    private static final int MAX_MOUNTAINS = 29;
    private static final int[][] HEURISTICS = new int[MAX_GRASS + 1][MAX_MOUNTAINS + 1];

    static {
        for (int grass = 1; grass <= MAX_GRASS; ++grass)
            for (int mountains = 0; mountains <= MAX_MOUNTAINS; ++mountains)
                HEURISTICS[grass][mountains] = calculateExpectedValueSum(grass, mountains);
    }

    private static int calculateExpectedValueSum(int grass, int mountains) {
        int expectedValueSum = 0;
        int currentRelativeDistance = 0;
        int remainingGrass = grass;
        int remainingMountains = mountains;

        while (remainingGrass > 0 && remainingMountains > 0) {
            currentRelativeDistance += 3;

            int adjacentGrass = Math.min(remainingGrass, 3);
            int cornerGrass = Math.min(remainingGrass - adjacentGrass, 4);
            int foundGrass = adjacentGrass + cornerGrass;
            int contribution = foundGrass * currentRelativeDistance + adjacentGrass * 3 + cornerGrass * 5;

            expectedValueSum += contribution;
            remainingGrass -= foundGrass;
            --remainingMountains;
        }

        while (remainingGrass > 0) {
            currentRelativeDistance += 2;
            expectedValueSum += currentRelativeDistance;
            --remainingGrass;
        }

        return expectedValueSum;
    }

    public ExpectedArrivalPathSolver createSolver(XYPair fullMapSize, FullMapGraph fullMapGraph, XYPair start,
                                                  List<XYPair> grass, List<XYPair> mountains) {
        if (fullMapSize == null)
            throw new IllegalArgumentException("fullMapSize is null");
        if (fullMapGraph == null)
            throw new IllegalArgumentException("fullMapGraph is null");
        if (start == null)
            throw new IllegalArgumentException("start is null");
        if (grass == null)
            throw new IllegalArgumentException("grass is null");
        if (mountains == null)
            throw new IllegalArgumentException("mountains is null");
        if (grass.contains(start))
            throw new IllegalArgumentException("grass contains start");

        List<XYPair> allNodes = new ArrayList<>();
        allNodes.add(start);
        allNodes.addAll(grass);
        allNodes.addAll(mountains);

        DistanceMatrix distanceMatrix = fullMapGraph.getDistanceMatrix(allNodes);

        var mountainRevelationMasks = new long[mountains.size()];
        var mountainToGrassDistances = new int[mountains.size()][];

        Map<XYPair, Integer> grassIndexMap = new HashMap<>();
        for (int i = 0; i < grass.size(); ++i)
            grassIndexMap.put(grass.get(i), i);

        for (int mountainIdx = 0; mountainIdx < mountains.size(); ++mountainIdx) {
            long grassMask = 0;
            XYPair mountainCoordinate = mountains.get(mountainIdx);
            List<XYPair> mountainGrassNeighbors = mountainCoordinate.getAllNeighbors(fullMapSize).stream()
                    .filter(grass::contains)
                    .toList();

            Map<Integer, Integer> neighborDistances = new HashMap<>();
            for (XYPair grassNeighbor : mountainGrassNeighbors) {
                int grassIdx = grassIndexMap.get(grassNeighbor);
                grassMask |= 1L << grassIdx;
                neighborDistances.put(grassIdx, fullMapGraph.getDistance(mountainCoordinate, grassNeighbor));
            }

            mountainRevelationMasks[mountainIdx] = grassMask;
            mountainToGrassDistances[mountainIdx] = new int[grass.size()];

            int fromMountainIdx = mountainIdx;
            neighborDistances.forEach((grassIdx, distance) ->
                    mountainToGrassDistances[fromMountainIdx][grassIdx] = distance);
        }

        var grassProximity = new int[allNodes.size()][grass.size()];
        var mountainProximity = new int[allNodes.size()][mountains.size()];

        for (int i = 0; i < allNodes.size(); ++i) {
            final int fromIdx = i;

            var grassIndices = new Integer[grass.size()];
            for (int g = 0; g < grass.size(); ++g)
                grassIndices[g] = g;
            Arrays.sort(grassIndices, Comparator.comparingInt(g -> distanceMatrix.getDistance(fromIdx, 1 + g)));
            for (int g = 0; g < grass.size(); ++g)
                grassProximity[i][g] = grassIndices[g];

            var mountainIndices = new Integer[mountains.size()];
            for (int m = 0; m < mountains.size(); ++m)
                mountainIndices[m] = m;
            Arrays.sort(mountainIndices, Comparator.comparingInt(m -> distanceMatrix.getDistance(fromIdx, 1 + grass.size() + m)));
            for (int m = 0; m < mountains.size(); ++m)
                mountainProximity[i][m] = mountainIndices[m];
        }

        return new ExpectedArrivalPathSolver(HEURISTICS, allNodes, grass.size(), mountains.size(), distanceMatrix,
                mountainRevelationMasks, mountainToGrassDistances, grassProximity, mountainProximity);
    }
}
