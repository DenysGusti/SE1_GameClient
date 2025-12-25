package client.ai.path;

import client.ai.graph.DistanceMatrix;
import client.data.XYPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ExpectedArrivalPathSolver {
    private static final Logger logger = LoggerFactory.getLogger(ExpectedArrivalPathSolver.class);

    private static final long TIMEOUT_NS = 4_500_000_000L; // 4.5 seconds in nanoseconds
    private static final long TIME_CHECK_INTERVAL = 0x1FFFF;

    private final int[][] heuristics;
    private final List<XYPair> allNodes;
    private final int grassCount;
    private final int mountainCount;
    private final DistanceMatrix distanceMatrix;
    private final long[] mountainRevelationMasks;
    private final int[][] mountainToGrassDistances;
    private final int[][] grassProximity;
    private final int[][] mountainProximity;

    private long iterations = 0;
    private boolean timeoutReached = false;
    private int minExpectedValueSum = Integer.MAX_VALUE;
    private int[] bestPathIndices = null;
    private long deadline;

    public ExpectedArrivalPathSolver(int[][] heuristics, List<XYPair> allNodes, int grassCount, int mountainCount,
                                     DistanceMatrix distanceMatrix, long[] mountainRevelationMasks,
                                     int[][] mountainToGrassDistances, int[][] grassProximity, int[][] mountainProximity) {
        if (heuristics == null)
            throw new IllegalArgumentException("heuristics is null");
        if (allNodes == null)
            throw new IllegalArgumentException("allNodes is null");
        if (grassCount < 0)
            throw new IllegalArgumentException("grassCount is negative");
        if (mountainCount < 0)
            throw new IllegalArgumentException("mountainCount is negative");
        if (distanceMatrix == null)
            throw new IllegalArgumentException("distanceMatrix is null");
        if (mountainRevelationMasks == null)
            throw new IllegalArgumentException("mountainRevelationMasks is null");
        if (mountainToGrassDistances == null)
            throw new IllegalArgumentException("mountainToGrassDistances is null");
        if (grassProximity == null)
            throw new IllegalArgumentException("grassProximity is null");
        if (mountainProximity == null)
            throw new IllegalArgumentException("mountainProximity is null");

        this.heuristics = heuristics;
        this.allNodes = allNodes;
        this.grassCount = grassCount;
        this.mountainCount = mountainCount;
        this.distanceMatrix = distanceMatrix;
        this.mountainRevelationMasks = mountainRevelationMasks;
        this.mountainToGrassDistances = mountainToGrassDistances;
        this.grassProximity = grassProximity;
        this.mountainProximity = mountainProximity;
    }

    public List<XYPair> getWaypoints() {
        long startTime = System.nanoTime();
        logger.debug("Branch & Bound started for {} grass and {} mountains..., time check interval: {}",
                grassCount, mountainCount, TIME_CHECK_INTERVAL);

        deadline = startTime + TIMEOUT_NS;

        int[] currentPath = new int[1 + grassCount + mountainCount];
        currentPath[0] = 0;
        long initialGrassMask = (1L << grassCount) - 1;

        solveRecursive(1, 0, 0, initialGrassMask, 0L, currentPath);
        if (bestPathIndices == null)
            throw new IllegalStateException("bestPathIndices is null");

        List<XYPair> waypoints = new ArrayList<>(bestPathIndices.length);
        for (int idx : bestPathIndices)
            waypoints.add(allNodes.get(idx));
        logger.trace("Waypoints:\n{}", waypoints);

        double duration = (System.nanoTime() - startTime) / 1_000_000_000.;
        logger.debug("Branch & Bound finished in {}s, iterations: {}, iterations/s: {}, min expected value: {}, timeout reached: {}",
                duration, iterations, iterations / duration, (double) minExpectedValueSum / grassCount, timeoutReached);
        return waypoints;
    }

    private void solveRecursive(int visitedCount, int currentPathDistance, int currentExpectedValueSum,
                                long remainingGrassMask, long mountainVisitedMask, int[] currentPath) {
        ++iterations;

        int remainingGrassCount = Long.bitCount(remainingGrassMask);
        if (remainingGrassCount == 0) {
            if (currentExpectedValueSum < minExpectedValueSum) {
                minExpectedValueSum = currentExpectedValueSum;
                bestPathIndices = Arrays.copyOf(currentPath, visitedCount);
            }
            return;
        }

        if ((iterations & TIME_CHECK_INTERVAL) == 0)
            if (System.nanoTime() > deadline)
                timeoutReached = true;

        if (timeoutReached)
            return;

        int remainingMountainCount = mountainCount - Long.bitCount(mountainVisitedMask);
        int lastVisitedNodeIdx = currentPath[visitedCount - 1];

        int[] sortedMountains = mountainProximity[lastVisitedNodeIdx];
        for (int nextMountain : sortedMountains) {
            if ((mountainVisitedMask & 1L << nextMountain) != 0)
                continue;

            long neighborGrassMask = mountainRevelationMasks[nextMountain] & remainingGrassMask;
            if (neighborGrassMask == 0)
                continue;

            int nextNodeIdx = 1 + grassCount + nextMountain;
            int nextNodeDistance = distanceMatrix.getDistance(lastVisitedNodeIdx, nextNodeIdx);

            int newPathDistance = currentPathDistance + nextNodeDistance;
            int discoveryContribution = getDiscoveryContribution(neighborGrassMask, newPathDistance, nextMountain);
            int newExpectedValueSum = currentExpectedValueSum + discoveryContribution;

            int newRemainingGrassCount = remainingGrassCount - Long.bitCount(neighborGrassMask);
            int newRemainingMountainCount = remainingMountainCount - 1;

            int childHeuristics = getHeuristics(newPathDistance, newRemainingGrassCount, newRemainingMountainCount);
            int newBestExpectedValueSum = newExpectedValueSum + childHeuristics;

            if (newBestExpectedValueSum >= minExpectedValueSum)
                continue;

            long newRemainingGrassMask = remainingGrassMask ^ neighborGrassMask;
            long newMountainVisitedMask = mountainVisitedMask | 1L << nextMountain;

            currentPath[visitedCount] = nextNodeIdx;
            solveRecursive(visitedCount + 1, newPathDistance, newExpectedValueSum,
                    newRemainingGrassMask, newMountainVisitedMask, currentPath);
        }

        int[] sortedGrass = grassProximity[lastVisitedNodeIdx];
        for (int nextGrass : sortedGrass) {
            if ((remainingGrassMask & 1L << nextGrass) == 0)
                continue;

            int nextNodeIdx = 1 + nextGrass;
            int nextNodeDistance = distanceMatrix.getDistance(lastVisitedNodeIdx, nextNodeIdx);

            int newPathDistance = currentPathDistance + nextNodeDistance;
            int newExpectedValueSum = currentExpectedValueSum + newPathDistance;

            int newRemainingGrassCount = remainingGrassCount - 1;

            int childHeuristics = getHeuristics(newPathDistance, newRemainingGrassCount, remainingMountainCount);
            int newBestExpectedValueSum = newExpectedValueSum + childHeuristics;

            if (newBestExpectedValueSum >= minExpectedValueSum)
                break;

            long newRemainingGrassMask = remainingGrassMask ^ 1L << nextGrass;

            currentPath[visitedCount] = nextNodeIdx;
            solveRecursive(visitedCount + 1, newPathDistance, newExpectedValueSum,
                    newRemainingGrassMask, mountainVisitedMask, currentPath);
        }
    }

    // calculate discovery sum for all nodes revealed by this mountain
    private int getDiscoveryContribution(long neighborGrassMask, int newDistance, int nextNode) {
        int neighborGrassCount = Long.bitCount(neighborGrassMask);
        int contribution = newDistance * neighborGrassCount;

        for (long grassMask = neighborGrassMask; grassMask != 0; grassMask &= grassMask - 1) {
            int neighborGrassIdx = Long.numberOfTrailingZeros(grassMask);
            int neighborGrassDistance = mountainToGrassDistances[nextNode][neighborGrassIdx];
            // path to mountain + path from mountain to neighbor grass
            contribution += neighborGrassDistance;
        }
        return contribution;
    }

    private int getHeuristics(int currentPathDistance, int remainingGrassCount, int remainingMountainCount) {
        return heuristics[remainingGrassCount][remainingMountainCount] + remainingGrassCount * currentPathDistance;
    }
}