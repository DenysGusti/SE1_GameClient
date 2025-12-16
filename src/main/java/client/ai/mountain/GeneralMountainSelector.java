package client.ai.mountain;

import client.ai.graph.FullMapGraph;
import client.data.XYPair;
import client.data.fromserver.FullMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

public class GeneralMountainSelector implements MountainSelector {
    private static final Logger logger = LoggerFactory.getLogger(GeneralMountainSelector.class);
    private static final int EXACT_SELECTOR_MOUNTAINS_THRESHOLD = 7;

    MountainSelector exactMountainSelector;
    MountainSelector heuristicMountainSelector;

    public GeneralMountainSelector(MountainSelector exactMountainSelector, MountainSelector heuristicMountainSelector) {
        if (exactMountainSelector == null)
            throw new IllegalArgumentException("exactMountainSelector is null");
        if (heuristicMountainSelector == null)
            throw new IllegalArgumentException("heuristicMountainSelector is null");

        this.exactMountainSelector = exactMountainSelector;
        this.heuristicMountainSelector = heuristicMountainSelector;
    }

    @Override
    public StepPathMetric computePath(FullMapGraph fullMapGraph, FullMap fullMap, Set<XYPair> unrevealedGrassNodes, List<XYPair> neighborMountains) {
        long startTime = System.nanoTime();
        logger.debug("General started for {} mountains...", neighborMountains.size());

        logger.debug("Exact selector mountains threshold: {}", EXACT_SELECTOR_MOUNTAINS_THRESHOLD);

        StepPathMetric stepPathMetric;

        if (neighborMountains.size() <= EXACT_SELECTOR_MOUNTAINS_THRESHOLD) {
            logger.debug("Using exact strategy");
            stepPathMetric = exactMountainSelector.computePath(fullMapGraph, fullMap, unrevealedGrassNodes, neighborMountains);
        } else {
            logger.debug("Using heuristic strategy");
            stepPathMetric = heuristicMountainSelector.computePath(fullMapGraph, fullMap, unrevealedGrassNodes, neighborMountains);
        }

        double duration = (System.nanoTime() - startTime) / 1_000_000_000.;
        logger.debug("General Mountain Selector finished: time: {}s, expected goal distance: {}",
                duration, stepPathMetric.expectedGoalDistance());
        return stepPathMetric;
    }
}
