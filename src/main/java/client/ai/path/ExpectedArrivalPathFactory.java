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
        generateHeuristics();
    }

    private static void generateHeuristics() {
        for (int grass = 1; grass <= MAX_GRASS; ++grass)
            for (int mountains = 0; mountains <= MAX_MOUNTAINS; ++mountains)
                HEURISTICS[grass][mountains] = calculateBestCase(grass, mountains);
    }

    private static int calculateBestCase(int grass, int mountains) {
        int totalExtraSum = 0;
        int currentRelativeDist = 0;
        int remainingGrass = grass;
        int remainingMountains = mountains;

        while (remainingGrass > 0 && remainingMountains > 0) {
            currentRelativeDist += 3;

            int adjacent = Math.min(remainingGrass, 3);
            int corner = Math.min(remainingGrass - adjacent, 4);
            int found = adjacent + corner;

            totalExtraSum += found * currentRelativeDist + adjacent * 3 + corner * 5;

            remainingGrass -= found;
            --remainingMountains;
        }

        while (remainingGrass > 0) {
            currentRelativeDist += 2;
            totalExtraSum += currentRelativeDist;
            --remainingGrass;
        }

        return totalExtraSum;
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

        long[] mountainRevelationMasks = new long[mountains.size()];
        int[][] mountainToGrassDistances = new int[mountains.size()][];

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

        return new ExpectedArrivalPathSolver(HEURISTICS, allNodes, grass.size(), mountains.size(), distanceMatrix,
                mountainRevelationMasks, mountainToGrassDistances);
    }
}
