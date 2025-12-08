package client.ai.mountain;

import client.ai.graph.FullMapGraph;
import client.data.XYPair;
import client.data.fromserver.FullMap;
import client.data.fromserver.FullMapNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class PathOptimizer {
    private static final Logger logger = LoggerFactory.getLogger(PathOptimizer.class);

    public record StepPathMetric(List<XYPair> path, double expectedGoalDistance) {
        public StepPathMetric {
            if (path == null)
                throw new IllegalArgumentException("path is null");
            if (expectedGoalDistance < 0)
                throw new IllegalArgumentException("expectedGoalDistance is negative");
        }
    }

    // calculate step-path with the lowest expected goal distance
    public StepPathMetric calculateBestStepPath(FullMap fullMap, FullMapGraph fullMapGraph,
                                                Set<XYPair> unrevealedGrassNodes, List<XYPair> traversalPath) {
        if (fullMap == null)
            throw new IllegalArgumentException("fullMap is null");
        if (fullMapGraph == null)
            throw new IllegalArgumentException("fullMapGraph is null");
        if (unrevealedGrassNodes == null)
            throw new IllegalArgumentException("unrevealedGrassNodes is null");
        if (traversalPath == null)
            throw new IllegalArgumentException("traversalPath is null");

        List<List<XYPair>> allPaths = fullMapGraph.getAllPaths(traversalPath);
        Objects.requireNonNull(allPaths, "allPaths is null");

        if (allPaths.isEmpty())
            throw new RuntimeException("No paths could be generated from the traversal path.");

        return allPaths.stream().parallel()
                .map(stepPath -> {
                    double expectedGoalDistance = getExpectedGoalDistance(fullMap, fullMapGraph, unrevealedGrassNodes, stepPath);
                    logger.trace("Expected step-path goal distance: {}", expectedGoalDistance);
                    return new StepPathMetric(stepPath, expectedGoalDistance);
                })
                .min(Comparator.comparingDouble(StepPathMetric::expectedGoalDistance))
                .orElseThrow();
    }

    private static double getExpectedGoalDistance(FullMap fullMap, FullMapGraph fullMapGraph,
                                                  Set<XYPair> unrevealedGrassNodes, List<XYPair> path) {
        if (fullMap == null)
            throw new IllegalArgumentException("fullMap is null");
        if (fullMapGraph == null)
            throw new IllegalArgumentException("fullMapGraph is null");
        if (unrevealedGrassNodes == null)
            throw new IllegalArgumentException("unrevealedGrassNodes is null");
        if (path == null)
            throw new IllegalArgumentException("path is null");

        if (unrevealedGrassNodes.isEmpty())
            return 0.;

        Set<XYPair> remainingGrassNodes = new HashSet<>(unrevealedGrassNodes);
        long sumNodeDistance = 0;
        int currentPathDistance = 0;

        for (int i = 1; i < path.size(); ++i) {
            if (remainingGrassNodes.isEmpty())
                break;

            XYPair prevStep = path.get(i - 1);
            XYPair currentStep = path.get(i);

            FullMapNode node = fullMap.nodes().get(currentStep);
            int stepDistance = fullMapGraph.getDistance(prevStep, currentStep);
            currentPathDistance += stepDistance;

            if (node.isGrass()) {
                if (remainingGrassNodes.remove(currentStep))
                    sumNodeDistance += currentPathDistance;
            } else if (node.isMountain()) {
                for (XYPair neighbor : currentStep.getAllNeighbors(fullMap.size()))
                    if (remainingGrassNodes.remove(neighbor)) {
                        // if across water then a lot
                        int neighborDistance = fullMapGraph.getDistance(currentStep, neighbor);
                        sumNodeDistance += currentPathDistance + neighborDistance;
                    }
            } else
                throw new RuntimeException("Path includes water at " + currentStep);
        }

        if (!remainingGrassNodes.isEmpty())
            throw new RuntimeException("Path didn't cover all targets! Remaining: " + remainingGrassNodes.size());

        return (double) sumNodeDistance / unrevealedGrassNodes.size();
    }
}
