package client.mapgeneration.rule;

import client.data.ETerrain;
import client.data.XYPair;
import client.data.fromclient.HalfMap;

import client.mapgeneration.validation.IHalfMapValidationRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ConnectivityRule implements IHalfMapValidationRule {
    private static final Logger logger = LoggerFactory.getLogger(ConnectivityRule.class);

    private static final XYPair HALF_MAP_SIZE = new XYPair(10, 5);

    @Override
    public boolean isValid(HalfMap halfMap) {
        XYPair startNode = halfMap.nodes().entrySet().stream()
                .filter(e -> e.getValue() != ETerrain.Water)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);

        if (startNode == null)
            return false;

        Set<XYPair> visited = new HashSet<>();
        visited.add(startNode);

        Queue<XYPair> toVisit = new ArrayDeque<>();
        toVisit.add(startNode);

        while (!toVisit.isEmpty()) {
            XYPair current = toVisit.poll();

            for (XYPair neighbor : current.getAdjacentNeighbors(HALF_MAP_SIZE))
                if (!visited.contains(neighbor)) {
                    ETerrain terrain = halfMap.nodes().get(neighbor);
                    if (terrain != ETerrain.Water) {
                        visited.add(neighbor);
                        toVisit.add(neighbor);
                    }
                }
        }

        long totalWalkableNodes = halfMap.nodes().values().stream()
                .filter(t -> t != ETerrain.Water)
                .count();

        return visited.size() == totalWalkableNodes;
    }
}
