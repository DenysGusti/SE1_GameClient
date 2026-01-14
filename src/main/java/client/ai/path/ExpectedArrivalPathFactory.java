package client.ai.path;

import client.ai.graph.DistanceMatrix;
import client.ai.graph.FullMapGraph;
import client.data.XYPair;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ExpectedArrivalPathFactory {
    private static final Logger logger = LoggerFactory.getLogger(ExpectedArrivalPathFactory.class);

    private static final int MAX_TILES_SINCE_LAST_MOUNTAIN = 2;
    private static final int MAX_GRASS = 38;
    private static final int MAX_MOUNTAINS = 5;
    private static final int[][][] HEURISTICS = new int[MAX_TILES_SINCE_LAST_MOUNTAIN + 1][MAX_GRASS + 1][MAX_MOUNTAINS + 1];

    private record HeuristicResult(int cost, String path) {
    }

    static {
        for (int previousMountainTiles = 0; previousMountainTiles < HEURISTICS.length; ++previousMountainTiles)
            for (int grass = 0; grass < HEURISTICS[0].length; ++grass)
                for (int mountains = 0; mountains < HEURISTICS[0][0].length; ++mountains) {
                    HeuristicResult minDistance = calculateExpectedValueSum(previousMountainTiles, 0, 0, grass, mountains, previousMountainTiles == 0 ? "m" : "g");
                    HEURISTICS[previousMountainTiles][grass][mountains] = minDistance.cost();
                }
    }

    private static HeuristicResult calculateExpectedValueSum(int tilesSinceLastMountain, int currentRowStep, int currentDistanceSum,
                                                             int remainingGrass, int remainingMountains, String currentPath) {
        if (remainingGrass == 0)
            return new HeuristicResult(currentDistanceSum, currentPath);

        int stepGrass = tilesSinceLastMountain == 0 ? 3 : 2;

        HeuristicResult pickGrass = tilesSinceLastMountain == 0 ?
                calculateExpectedValueSum(1, currentRowStep + stepGrass,
                        currentDistanceSum, remainingGrass, remainingMountains, currentPath + "g") :
                calculateExpectedValueSum(tilesSinceLastMountain + 1, currentRowStep + stepGrass,
                        currentDistanceSum + currentRowStep + stepGrass,
                        remainingGrass - 1, remainingMountains, currentPath + "G");

        if (remainingMountains == 0)
            return pickGrass;

        int stepMountain = tilesSinceLastMountain == 0 ? 4 : 3;
        int maxCornerGrass = tilesSinceLastMountain <= 1 ? 2 : 4;
        int maxAdjacentGrass = tilesSinceLastMountain == 0 ? 1 : 3;

        int adjacentGrass = Math.min(remainingGrass, maxAdjacentGrass);
        int cornerGrass = Math.min(remainingGrass - adjacentGrass, maxCornerGrass);
        int foundGrass = adjacentGrass + cornerGrass;
        int contribution = foundGrass * (currentRowStep + stepMountain) + adjacentGrass * 3 + cornerGrass * 5;

        HeuristicResult pickMountain = calculateExpectedValueSum(1, currentRowStep + stepMountain + 3,
                currentDistanceSum + contribution, remainingGrass - foundGrass,
                remainingMountains - 1, currentPath + (adjacentGrass > 0 ? "Mg" : "M"));

        if (pickGrass.cost() <= pickMountain.cost())
            return pickGrass;
        else
            return pickMountain;
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

        Map<XYPair, Integer> grassIndexMap = new HashMap<>();
        for (int i = 0; i < grass.size(); ++i)
            grassIndexMap.put(grass.get(i), i);

        for (int mountainIdx = 0; mountainIdx < mountains.size(); ++mountainIdx) {
            long grassMask = 0;
            XYPair mountainCoordinate = mountains.get(mountainIdx);
            List<XYPair> mountainGrassNeighbors = mountainCoordinate.getAllNeighbors(fullMapSize).stream()
                    .filter(grass::contains)
                    .toList();

            for (XYPair grassNeighbor : mountainGrassNeighbors) {
                int grassIdx = grassIndexMap.get(grassNeighbor);
                grassMask |= 1L << grassIdx;
            }

            mountainRevelationMasks[mountainIdx] = grassMask;
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
                mountainRevelationMasks, grassProximity, mountainProximity);
    }
}
