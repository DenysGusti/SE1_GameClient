package client.ai.mountain;

import client.ai.graph.FullMapGraph;
import client.ai.tsp.NodeTraversalStrategy;
import client.data.XYPair;
import client.data.fromserver.FullMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class GreedyMountainSelector extends MountainSelector {
    private static final Logger logger = LoggerFactory.getLogger(GreedyMountainSelector.class);

    public GreedyMountainSelector(NodeTraversalStrategy nodeTraversalStrategy, PathOptimizer pathOptimizer) {
        super(Objects.requireNonNull(nodeTraversalStrategy, "nodeTraversalStrategy must not be null"),
                Objects.requireNonNull(pathOptimizer, "pathOptimizer must not be null"));
    }

    @Override
    protected PathOptimizer.StepPathMetric computePath(FullMapGraph fullMapGraph, FullMap fullMap,
                                                       Set<XYPair> unrevealedGrassNodes, List<XYPair> neighborMountains) {
        long startTime = System.nanoTime();
        logger.debug("Greedy Mountain Picker started for {} nodes, {} mountains...",
                unrevealedGrassNodes.size(), neighborMountains.size());

        List<XYPair> currentSelectedMountains = new ArrayList<>();
        Set<XYPair> currentStrategyTargets = new HashSet<>(unrevealedGrassNodes);
        XYPair myPlayerPosition = fullMap.getOptionalMyPlayerPosition().orElseThrow();

        NodeTraversalStrategy.TraversalResult traversalResult =
                nodeTraversalStrategy.orderNodes(fullMapGraph, myPlayerPosition, currentStrategyTargets);

        PathOptimizer.StepPathMetric bestStepPathMetric = pathOptimizer.calculateBestStepPath(
                fullMap, fullMapGraph, currentStrategyTargets, traversalResult.path()
        );

        logger.debug("Baseline (no mountains) expected distance: {}", bestStepPathMetric.expectedGoalDistance());

        List<XYPair> availableMountains = new ArrayList<>(neighborMountains);

        int iteration = 0;

        for (boolean improvementMade = true; improvementMade && !availableMountains.isEmpty(); ++iteration) {
            improvementMade = false;
            XYPair bestCandidateMountain = null;
            PathOptimizer.StepPathMetric bestCandidateStepPathMetric = null;

            for (XYPair currentCandidateMountain : availableMountains) {
                Set<XYPair> trialNodes = new HashSet<>(unrevealedGrassNodes);

                List<XYPair> trialSelectedMountains = new ArrayList<>(currentSelectedMountains);
                trialSelectedMountains.add(currentCandidateMountain);
                for (XYPair mountain : trialSelectedMountains)
                    mountain.getAllNeighbors(fullMap.size()).forEach(trialNodes::remove);

                trialNodes.addAll(trialSelectedMountains);

                NodeTraversalStrategy.TraversalResult currentTraversalResult =
                        nodeTraversalStrategy.orderNodes(fullMapGraph, myPlayerPosition, trialNodes);
                Objects.requireNonNull(currentTraversalResult, "currentTraversalResult not be null");

                PathOptimizer.StepPathMetric currentStepPathMetric = pathOptimizer.calculateBestStepPath(
                        fullMap, fullMapGraph, unrevealedGrassNodes, currentTraversalResult.path()
                );
                Objects.requireNonNull(currentStepPathMetric, "currentStepPathMetric not be null");

                logger.trace("Analyzed mountain {} at iteration {}, expected goal distance: {} -> {}",
                        currentCandidateMountain, iteration, bestCandidateStepPathMetric == null ? "null" : bestCandidateStepPathMetric.expectedGoalDistance(),
                        currentStepPathMetric.expectedGoalDistance());

                if (currentStepPathMetric.expectedGoalDistance() < bestStepPathMetric.expectedGoalDistance())
                    if (bestCandidateStepPathMetric == null ||
                            currentStepPathMetric.expectedGoalDistance() < bestCandidateStepPathMetric.expectedGoalDistance()) {
                        logger.trace("Found mountain {} at iteration {}, expected goal distance: {} -> {}",
                                currentCandidateMountain, iteration, bestCandidateStepPathMetric == null ? "null" : bestCandidateStepPathMetric.expectedGoalDistance(),
                                currentStepPathMetric.expectedGoalDistance());

                        bestCandidateMountain = currentCandidateMountain;
                        bestCandidateStepPathMetric = currentStepPathMetric;
                    }
            }

            if (bestCandidateMountain != null) {
                logger.trace("Added mountain {} at iteration {}, expected goal distance: {} -> {}",
                        bestCandidateMountain, iteration, bestStepPathMetric.expectedGoalDistance(),
                        bestCandidateStepPathMetric.expectedGoalDistance());

                currentSelectedMountains.add(bestCandidateMountain);
                availableMountains.remove(bestCandidateMountain);
                bestStepPathMetric = bestCandidateStepPathMetric;
                improvementMade = true;
            }
        }

        Objects.requireNonNull(bestStepPathMetric, "bestStepPathMetric must not be null");

        logger.debug("Selected mountains: {}", currentSelectedMountains);
        logger.debug("Step-path: {}", bestStepPathMetric.path());

        long endTime = System.nanoTime();
        double duration = (endTime - startTime) / 1_000_000_000.;
        logger.debug("Greedy Mountain Picker finished: iterations: {}, time: {}s, expected goal distance: {}",
                iteration, duration, bestStepPathMetric.expectedGoalDistance());
        return bestStepPathMetric;
    }
}