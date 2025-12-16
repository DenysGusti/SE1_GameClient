package client.ai.mountain;

import client.ai.graph.FullMapGraph;
import client.data.XYPair;
import client.data.fromserver.FullMap;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public interface MountainSelector {
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

    default StepPathMetric selectMountainPath(FullMapGraph fullMapGraph, FullMap fullMap, Set<XYPair> unrevealedGrassNodes) {
        if (fullMapGraph == null)
            throw new IllegalArgumentException("fullMapGraph is null");
        if (fullMap == null)
            throw new IllegalArgumentException("fullMap is null");
        if (unrevealedGrassNodes == null)
            throw new IllegalArgumentException("unrevealedGrassNodes is null");

        List<XYPair> neighborMountains = getNeighborMountains(fullMap, unrevealedGrassNodes);
        Objects.requireNonNull(neighborMountains, "neighborMountains is null");

        return computePath(fullMapGraph, fullMap, unrevealedGrassNodes, neighborMountains);
    }

    StepPathMetric computePath(FullMapGraph fullMapGraph, FullMap fullMap,
                               Set<XYPair> unrevealedGrassNodes, List<XYPair> neighborMountains);
}
