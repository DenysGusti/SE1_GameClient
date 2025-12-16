package client.ai.mountain;

import client.ai.graph.FullMapGraph;
import client.ai.tsp.NodeTraversalStrategy;
import client.data.XYPair;
import client.data.fromserver.FullMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class GreedyMountainSelector implements MountainSelector {
    private static final Logger logger = LoggerFactory.getLogger(GreedyMountainSelector.class);

    private final NodeTraversalStrategy nodeTraversalStrategy;
    private final PathOptimizer pathOptimizer;

    public GreedyMountainSelector(NodeTraversalStrategy nodeTraversalStrategy, PathOptimizer pathOptimizer) {
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
        logger.debug("Greedy Mountain Selector started for {} nodes, {} mountains...",
                unrevealedGrassNodes.size(), neighborMountains.size());

        List<XYPair> currentSelectedMountains = new ArrayList<>();
        Set<XYPair> currentStrategyTargets = new HashSet<>(unrevealedGrassNodes);
        XYPair myPlayerPosition = fullMap.getOptionalMyPlayerPosition().orElseThrow();

        NodeTraversalStrategy.TraversalResult traversalResult =
                nodeTraversalStrategy.orderNodes(fullMapGraph, myPlayerPosition, currentStrategyTargets);

        StepPathMetric bestStepPathMetric =
                pathOptimizer.calculateBestStepPath(fullMap, fullMapGraph, currentStrategyTargets, traversalResult.path());

        logger.debug("Baseline (no mountains) expected distance: {}", bestStepPathMetric.expectedGoalDistance());

        List<XYPair> availableMountains = new ArrayList<>(neighborMountains);

        int iteration = 0;

        for (boolean improvementMade = true; improvementMade && !availableMountains.isEmpty(); ++iteration) {
            improvementMade = false;
            XYPair bestCandidateMountain = null;
            StepPathMetric bestCandidateStepPathMetric = null;

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

                StepPathMetric currentStepPathMetric = pathOptimizer.calculateBestStepPath(
                        fullMap, fullMapGraph, unrevealedGrassNodes, currentTraversalResult.path()
                );
                Objects.requireNonNull(currentStepPathMetric, "currentStepPathMetric not be null");

                if (currentStepPathMetric.expectedGoalDistance() < bestStepPathMetric.expectedGoalDistance())
                    if (bestCandidateStepPathMetric == null ||
                            currentStepPathMetric.expectedGoalDistance() < bestCandidateStepPathMetric.expectedGoalDistance()) {
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

        Objects.requireNonNull(bestStepPathMetric, "bestStepPathMetric is null");

        double duration = (System.nanoTime() - startTime) / 1_000_000_000.;
        logger.debug("Greedy Mountain Selector finished: time: {}s, selected mountains: {}, expected goal distance: {}",
                duration, currentSelectedMountains, bestStepPathMetric.expectedGoalDistance());
        return bestStepPathMetric;
    }
}