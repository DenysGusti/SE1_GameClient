package client.ai.mountain;

import client.data.XYPair;

import java.util.List;

public record StepPathMetric(List<XYPair> path, double expectedGoalDistance) {
    public StepPathMetric {
        if (path == null)
            throw new IllegalArgumentException("path is null");
        if (expectedGoalDistance < 0)
            throw new IllegalArgumentException("expectedGoalDistance is negative");
    }
}