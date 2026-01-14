package client.ai.path;

import client.ai.exception.HeuristicConsistencyException;
import client.ai.exception.PathException;
import client.ai.graph.DistanceMatrix;
import client.data.XYPair;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ExpectedArrivalPathSolver {
    private static final Logger logger = LoggerFactory.getLogger(ExpectedArrivalPathSolver.class);

    private static final long TIMEOUT_NS = 4_500_000_000L; // 4.5 seconds in nanoseconds
    private static final long TIME_CHECK_INTERVAL = 0xFFFFF;

    private final int[][][] heuristics;
    private final List<XYPair> allNodes;
    private final int grassCount;
    private final int mountainCount;
    private final DistanceMatrix distanceMatrix;
    private final long[] mountainRevelationMasks;
    private final int[][] grassProximity;
    private final int[][] mountainProximity;

    private long iterations = 0;
    private boolean timeoutReached = false;
    private int minExpectedValueSum = Integer.MAX_VALUE;
    private int[] bestPathIndices = null;
    private long deadline;

    public ExpectedArrivalPathSolver(int[][][] heuristics, List<XYPair> allNodes, int grassCount, int mountainCount,
                                     DistanceMatrix distanceMatrix, long[] mountainRevelationMasks,
                                     int[][] grassProximity, int[][] mountainProximity) {
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
        this.grassProximity = grassProximity;
        this.mountainProximity = mountainProximity;
    }

    public void solve() {
        long startTime = System.nanoTime();
        logger.debug("Branch & Bound started for {} grass and {} mountains..., time check interval: {}",
                grassCount, mountainCount, TIME_CHECK_INTERVAL);

        deadline = startTime + TIMEOUT_NS;

        int[] currentPath = new int[1 + grassCount + mountainCount];
        currentPath[0] = 0;
        long initialGrassMask = (1L << grassCount) - 1;

        solveRecursive(1, 0, 0, initialGrassMask, 0L, currentPath, 2);

        double duration = (System.nanoTime() - startTime) / 1_000_000_000.;
        logger.debug("Branch & Bound finished in {}s, iterations: {}, iterations/s: {}, min expected value: {}, timeout reached: {}",
                duration, iterations, iterations / duration, (double) minExpectedValueSum / grassCount, timeoutReached);
    }

    public List<XYPair> getWaypoints() {
        if (bestPathIndices == null)
            throw new IllegalStateException("bestPathIndices is null");

        List<XYPair> waypoints = new ArrayList<>(bestPathIndices.length);
        for (int idx : bestPathIndices)
            waypoints.add(allNodes.get(idx));
        logger.trace("Waypoints:\n{}", waypoints);
        return waypoints;
    }

    private void solveRecursive(int visitedCount, int currentPathDistance, int currentExpectedValueSum,
                                long remainingGrassMask, long mountainVisitedMask, int[] currentPath, int tilesSinceLastMountain) {
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
        for (int mountain : sortedMountains) {
            if ((mountainVisitedMask & 1L << mountain) != 0)
                continue;

            long neighborGrassMask = mountainRevelationMasks[mountain] & remainingGrassMask;
            if (neighborGrassMask == 0)
                continue;

            int mountainIdx = 1 + grassCount + mountain;
            int mountainDistance = distanceMatrix.getDistance(lastVisitedNodeIdx, mountainIdx);

            if (!canVisitMountain(neighborGrassMask, lastVisitedNodeIdx, mountainIdx))
                continue;

            int newPathDistance = currentPathDistance + mountainDistance;
            int neighborGrassCount = Long.bitCount(neighborGrassMask);
            int newRemainingGrassCount = remainingGrassCount - neighborGrassCount;
            int newRemainingMountainCount = remainingMountainCount - 1;

            int discoveryContributionWeight = newPathDistance * neighborGrassCount;
            int relativeDiscoveryContribution = getRelativeDiscoveryContribution(neighborGrassMask, mountainIdx);
            int fullDiscoveryContribution = discoveryContributionWeight + relativeDiscoveryContribution;
            int newExpectedValueSum = currentExpectedValueSum + fullDiscoveryContribution;

            int parentTableWeight = currentPathDistance * remainingGrassCount;
            int parentRelativeTable = getRelativeTableHeuristics(tilesSinceLastMountain, remainingGrassCount, remainingMountainCount);
            int parentFullTable = parentTableWeight + parentRelativeTable;
            int parentHeuristics = currentExpectedValueSum + parentFullTable;

            int childTableWeight = newPathDistance * newRemainingGrassCount;
            int childRelativeTable = getRelativeTableHeuristics(0, newRemainingGrassCount, newRemainingMountainCount);
            int childFullTable = childTableWeight + childRelativeTable;
            int childHeuristics = newExpectedValueSum + childFullTable;

            if (childHeuristics < parentHeuristics) {
                logger.error("Consistency violation!");
                logger.trace("parentHeuristics: {}, childHeuristics: {}, minExpectedValueSum: {}", parentHeuristics, childHeuristics, minExpectedValueSum);

                logger.trace("mountain: {}", allNodes.get(mountainIdx));
                List<XYPair> waypoints = new ArrayList<>(currentPath.length);
                for (int i = 0; i < visitedCount; ++i)
                    waypoints.add(allNodes.get(currentPath[i]));
                logger.trace("waypoints: {}", waypoints);

                logger.trace("pathDistance: {} -> {} (+{})", currentPathDistance, newPathDistance, mountainDistance);
                logger.trace("remainingGrassCount: {} -> {} (-{})", remainingGrassCount, newRemainingGrassCount, neighborGrassCount);
                logger.trace("remainingMountainCount: {} -> {} (-1)", remainingMountainCount, newRemainingMountainCount);

                logger.trace("discoveryContributionWeight ({}) = newPathDistance ({}) * neighborGrassCount ({})",
                        discoveryContributionWeight, newPathDistance, neighborGrassCount);
                logger.trace("fullDiscoveryContribution ({}) = discoveryContributionWeight ({}) + relativeDiscoveryContribution ({})",
                        fullDiscoveryContribution, discoveryContributionWeight, relativeDiscoveryContribution);
                logger.trace("expectedValueSum: {} -> {} (+{})", currentExpectedValueSum, newExpectedValueSum, fullDiscoveryContribution);

                logger.trace("parentTableWeight ({}) = currentPathDistance ({}) * remainingGrassCount ({})",
                        parentTableWeight, currentPathDistance, remainingGrassCount);
                logger.trace("parentFullTable ({}) = parentTableWeight ({}) + parentRelativeTable (h[t={},g={},m={}] = {})",
                        parentFullTable, parentTableWeight, tilesSinceLastMountain, remainingGrassCount, remainingMountainCount, parentRelativeTable);
                logger.trace("parentHeuristics ({}) = currentExpectedValueSum ({}) + parentFullTable ({})",
                        parentHeuristics, currentExpectedValueSum, parentFullTable);

                logger.trace("childTableWeight ({}) = newPathDistance ({}) * newRemainingGrassCount ({})",
                        childTableWeight, newPathDistance, newRemainingGrassCount);
                logger.trace("childFullTable ({}) = childTableWeight ({}) + childRelativeTable (h[t=0,g={},m={}] = {})",
                        childFullTable, childTableWeight, newRemainingGrassCount, newRemainingMountainCount, childRelativeTable);
                logger.trace("childHeuristics ({}) = newExpectedValueSum ({}) + childFullTable ({})\n",
                        childHeuristics, newExpectedValueSum, childFullTable);

                /*
                The problem is that the optimal solution for table[t=0,g=6,m=1..n] is 58 mMgGGG, but the new mountain
                can't be second if there are no other mountains around the current mountain, which is very hard to
                detect, so this is the only exception from consistency, the heuristics in this case is 60.
                 */
                if (tilesSinceLastMountain != 0 && childRelativeTable != 58)
                    throw new HeuristicConsistencyException("Consistency violation!");
                else
                    childHeuristics += 2;
            }

            if (childHeuristics >= minExpectedValueSum)
                continue;

            long newRemainingGrassMask = remainingGrassMask ^ neighborGrassMask;
            long newMountainVisitedMask = mountainVisitedMask | 1L << mountain;

            currentPath[visitedCount] = mountainIdx;
            solveRecursive(visitedCount + 1, newPathDistance, newExpectedValueSum,
                    newRemainingGrassMask, newMountainVisitedMask, currentPath, 0);
        }

        int[] sortedGrass = grassProximity[lastVisitedNodeIdx];
        for (int grass : sortedGrass) {
            if ((remainingGrassMask & 1L << grass) == 0)
                continue;

            int grassIdx = 1 + grass;
            int grassDistance = distanceMatrix.getDistance(lastVisitedNodeIdx, grassIdx);

            int newPathDistance = currentPathDistance + grassDistance;
            int newExpectedValueSum = currentExpectedValueSum + newPathDistance;

            int newRemainingGrassCount = remainingGrassCount - 1;

            int newTilesSinceLastMountain = grassDistance == 2 ? tilesSinceLastMountain + 1 : 2;

            int childTableWeight = newPathDistance * newRemainingGrassCount;
            int childRelativeTable = getRelativeTableHeuristics(newTilesSinceLastMountain, newRemainingGrassCount, remainingMountainCount);
            int childFullTable = childTableWeight + childRelativeTable;
            int childHeuristics = newExpectedValueSum + childFullTable;

            if (childHeuristics >= minExpectedValueSum)
                break;

            long newRemainingGrassMask = remainingGrassMask ^ 1L << grass;

            currentPath[visitedCount] = grassIdx;
            solveRecursive(visitedCount + 1, newPathDistance, newExpectedValueSum,
                    newRemainingGrassMask, mountainVisitedMask, currentPath, newTilesSinceLastMountain);
        }
    }

    private boolean canVisitMountain(long neighborGrassMask, int lastVisitedNodeIdx, int mountainIdx) {
        int lastNodeMountainDistance = distanceMatrix.getDistance(lastVisitedNodeIdx, mountainIdx);

        for (long grassMask = neighborGrassMask; grassMask != 0; grassMask &= grassMask - 1) {
            int neighborGrassIdx = 1 + Long.numberOfTrailingZeros(grassMask);

            int lastNodeNeighborGrassDistance = distanceMatrix.getDistance(lastVisitedNodeIdx, neighborGrassIdx);
            int neighborGrassMountainDistance = distanceMatrix.getDistance(neighborGrassIdx, mountainIdx);

            if (lastNodeNeighborGrassDistance + neighborGrassMountainDistance == lastNodeMountainDistance)
                return false;
        }

        return true;
    }

    private int getRelativeDiscoveryContribution(long neighborGrassMask, int mountainIdx) {
        int adjacentCount = 0;
        int contribution = 0;

        for (long grassMask = neighborGrassMask; grassMask != 0; grassMask &= grassMask - 1) {
            int neighborGrassIdx = 1 + Long.numberOfTrailingZeros(grassMask);

            int neighborGrassMountainDistance = distanceMatrix.getDistance(neighborGrassIdx, mountainIdx);
            contribution += neighborGrassMountainDistance;

            if (neighborGrassMountainDistance == 3)
                ++adjacentCount;
        }

        if (adjacentCount == 4)
            throw new PathException("No entry point to mountain");

        return contribution;
    }

    private int getRelativeTableHeuristics(int tilesBetweenPreviousMountainAndCurrentTile,
                                           int remainingGrassCount, int remainingMountainCount) {
        int idx0 = Math.min(tilesBetweenPreviousMountainAndCurrentTile, heuristics.length - 1);
        int idx2 = Math.min(remainingMountainCount, heuristics[0][0].length - 1);
        return heuristics[idx0][remainingGrassCount][idx2];
    }
}