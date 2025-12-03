package client.ai.mountain;

import client.ai.graph.FullMapGraph;
import client.ai.tsp.NodeTraversalStrategy;
import client.data.XYPair;
import client.data.fromserver.FullMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ExhaustiveMountainSelector extends MountainSelector {
    private static final Logger logger = LoggerFactory.getLogger(ExhaustiveMountainSelector.class);

    public ExhaustiveMountainSelector(NodeTraversalStrategy nodeTraversalStrategy, PathOptimizer pathOptimizer) {
        super(Objects.requireNonNull(nodeTraversalStrategy, "nodeTraversalStrategy must not be null"),
                Objects.requireNonNull(pathOptimizer, "pathOptimizer must not be null"));
    }

    @Override
    protected PathOptimizer.StepPathMetric computePath(FullMapGraph fullMapGraph, FullMap fullMap, Set<XYPair> unrevealedGrassNodes, List<XYPair> neighborMountains) {
        long startTime = System.nanoTime();
        logger.debug("Exhaustive Mountain Selector started for {} nodes, {} mountains...",
                unrevealedGrassNodes.size(), neighborMountains.size());

        int n = neighborMountains.size();
        int numMountainSubsets = 1 << n;

        logger.debug("Evaluating {} mountain combinations (2^{}) for optimal scouting...", numMountainSubsets, n);

        PathOptimizer.StepPathMetric bestStepPathMetric = null;
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
            Objects.requireNonNull(traversalResult, "traversalResult must not be null");

            if (traversalResult.path().isEmpty())
                throw new RuntimeException("Traversal path is empty");

            PathOptimizer.StepPathMetric currentStepPathMetric =
                    pathOptimizer.calculateBestStepPath(fullMap, fullMapGraph, unrevealedGrassNodes, traversalResult.path());
            Objects.requireNonNull(currentStepPathMetric, "currentStepPathMetric must not be null");

            if (currentStepPathMetric.path().isEmpty())
                throw new RuntimeException("Step-path is empty");

            logger.trace("Step-path expected goal distance: {}", currentStepPathMetric.expectedGoalDistance());

            if (bestStepPathMetric == null ||
                    currentStepPathMetric.expectedGoalDistance() < bestStepPathMetric.expectedGoalDistance()) {
                logger.trace("Found mountains {}, expected goal distance: {} -> {}",
                        selectedMountains, bestStepPathMetric == null ? "null" : bestStepPathMetric.expectedGoalDistance(),
                        currentStepPathMetric.expectedGoalDistance());

                bestStepPathMetric = currentStepPathMetric;
                bestSelectedMountains = selectedMountains;
            }
        }

        Objects.requireNonNull(bestStepPathMetric, "bestStepPathMetric must not be null");

        logger.debug("Selected mountains: {}", bestSelectedMountains);
        logger.debug("Step-path: {}", bestStepPathMetric.path());

        long endTime = System.nanoTime();
        double duration = (endTime - startTime) / 1_000_000_000.;
        logger.debug("Exhaustive Mountain Selector finished: time: {}s, expected goal distance: {}",
                duration, bestStepPathMetric.expectedGoalDistance());

        return bestStepPathMetric;
    }
}
