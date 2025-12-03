package client.ai.mountain;

import client.ai.graph.FullMapGraph;
import client.ai.tsp.NodeTraversalStrategy;
import client.data.XYPair;
import client.data.fromserver.FullMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public abstract class MountainSelector {
    private static final Logger logger = LoggerFactory.getLogger(MountainSelector.class);

    protected final NodeTraversalStrategy nodeTraversalStrategy;
    protected final PathOptimizer pathOptimizer;

    public MountainSelector(NodeTraversalStrategy nodeTraversalStrategy, PathOptimizer pathOptimizer) {
        if (nodeTraversalStrategy == null)
            throw new IllegalArgumentException("nodeTraversalStrategy is null");
        if (pathOptimizer == null)
            throw new IllegalArgumentException("pathOptimizer is null");

        this.nodeTraversalStrategy = nodeTraversalStrategy;
        this.pathOptimizer = pathOptimizer;
    }

    private static List<XYPair> getNeighborMountains(FullMap fullMap, Set<XYPair> nodes) {
        return fullMap.nodes().entrySet().stream()
                .filter(entry -> entry.getValue().isMountain())
                .map(Map.Entry::getKey)
                .filter(mountain -> {
                    List<XYPair> neighbors = mountain.getAllNeighbors(fullMap.size());
                    return neighbors.stream().anyMatch(nodes::contains);
                })
                .toList();
    }

    public PathOptimizer.StepPathMetric selectMountainPath(FullMapGraph fullMapGraph, FullMap fullMap,
                                                           Set<XYPair> unrevealedGrassNodes) {
        if (fullMapGraph == null)
            throw new IllegalArgumentException("fullMapGraph is null");
        if (fullMap == null)
            throw new IllegalArgumentException("fullMap is null");
        if (unrevealedGrassNodes == null)
            throw new IllegalArgumentException("unrevealedGrassNodes is null");

        List<XYPair> neighborMountains = getNeighborMountains(fullMap, unrevealedGrassNodes);
        Objects.requireNonNull(neighborMountains, "neighborMountains must not be null");
        logger.debug("Neighbor mountains: {}", neighborMountains);

        return computePath(fullMapGraph, fullMap, unrevealedGrassNodes, neighborMountains);
    }

    protected abstract PathOptimizer.StepPathMetric computePath(FullMapGraph fullMapGraph, FullMap fullMap,
                                                                Set<XYPair> unrevealedGrassNodes, List<XYPair> neighborMountains);
}
