package client.ai.mountain;

import client.ai.graph.FullMapGraph;
import client.ai.tsp.NodeTraversalStrategy;
import client.data.XYPair;
import client.data.fromserver.FullMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ExhaustiveMountainSelector implements MountainSelector {
    private static final Logger logger = LoggerFactory.getLogger(ExhaustiveMountainSelector.class);

    private final NodeTraversalStrategy nodeTraversalStrategy;
    private final PathOptimizer pathOptimizer;

    public ExhaustiveMountainSelector(NodeTraversalStrategy nodeTraversalStrategy, PathOptimizer pathOptimizer) {
        if (nodeTraversalStrategy == null)
            throw new IllegalArgumentException("nodeTraversalStrategy is null");
        if (pathOptimizer == null)
            throw new IllegalArgumentException("pathOptimizer is null");

        this.nodeTraversalStrategy = nodeTraversalStrategy;
        this.pathOptimizer = pathOptimizer;
    }

    @Override
    public StepPathMetric computePath(FullMapGraph fullMapGraph, FullMap fullMap,
                                      Set<XYPair> unrevealedGrassNodes, List<XYPair> neighborMountains) {
        long startTime = System.nanoTime();
        logger.debug("Exhaustive Mountain Selector started for {} nodes, {} mountains...",
                unrevealedGrassNodes.size(), neighborMountains.size());

        int n = neighborMountains.size();
        int numMountainSubsets = 1 << n;

        logger.debug("Evaluating {} mountain combinations (2^{}) for optimal scouting...", numMountainSubsets, n);

        StepPathMetric bestStepPathMetric = null;
        XYPair myPlayerPosition = fullMap.getOptionalMyPlayerPosition().orElseThrow();
        List<XYPair> bestSelectedMountains = null;

        for (int i = 0; i < numMountainSubsets; ++i) {
            Set<XYPair> currentStrategyNodeTargets = new HashSet<>(unrevealedGrassNodes);
            List<XYPair> selectedMountains = new ArrayList<>();

            for (int mountainBit = 0; mountainBit < n; ++mountainBit)
                if ((i & (1 << mountainBit)) != 0) {
                    XYPair mountain = neighborMountains.get(mountainBit);
                    selectedMountains.add(mountain);
                    List<XYPair> mountainNeighbors = mountain.getAllNeighbors(fullMap.size());
                    mountainNeighbors.forEach(currentStrategyNodeTargets::remove);
                }

            currentStrategyNodeTargets.addAll(selectedMountains);

            if (currentStrategyNodeTargets.isEmpty())
                throw new RuntimeException("Empty targets");

            NodeTraversalStrategy.TraversalResult traversalResult =
                    nodeTraversalStrategy.orderNodes(fullMapGraph, myPlayerPosition, currentStrategyNodeTargets);
            Objects.requireNonNull(traversalResult, "traversalResult is null");

            if (traversalResult.path().isEmpty())
                throw new RuntimeException("Traversal path is empty");

            StepPathMetric currentStepPathMetric =
                    pathOptimizer.calculateBestStepPath(fullMap, fullMapGraph, unrevealedGrassNodes, traversalResult.path());
            Objects.requireNonNull(currentStepPathMetric, "currentStepPathMetric is null");

            if (currentStepPathMetric.path().isEmpty())
                throw new RuntimeException("Step-path is empty");

            if (bestStepPathMetric == null ||
                    currentStepPathMetric.expectedGoalDistance() < bestStepPathMetric.expectedGoalDistance()) {
                logger.trace("Found mountains {}, expected goal distance: {} -> {}",
                        selectedMountains, bestStepPathMetric == null ? "null" : bestStepPathMetric.expectedGoalDistance(),
                        currentStepPathMetric.expectedGoalDistance());

                bestStepPathMetric = currentStepPathMetric;
                bestSelectedMountains = selectedMountains;
            }
        }

        Objects.requireNonNull(bestStepPathMetric, "bestStepPathMetric is null");

        double duration = (System.nanoTime() - startTime) / 1_000_000_000.;
        logger.debug("Exhaustive Mountain Selector finished: time: {}s, selected mountains: {}, expected goal distance: {}",
                duration, bestSelectedMountains, bestStepPathMetric.expectedGoalDistance());

        return bestStepPathMetric;
    }
}
