package client.data.fromclient;

import client.data.ETerrain;
import client.data.XYPair;

import java.util.*;

public record HalfMap(Map<XYPair, ETerrain> nodes, Set<XYPair> potentialForts) {
    public HalfMap(Map<XYPair, ETerrain> nodes, Set<XYPair> potentialForts) {
        if (nodes == null)
            throw new IllegalArgumentException("nodes is null");
        if (potentialForts == null)
            throw new IllegalArgumentException("potentialForts is null");

        this.nodes = Map.copyOf(nodes);
        this.potentialForts = Set.copyOf(potentialForts);
    }

    public boolean isWater(XYPair coordinate) {
        if (coordinate == null)
            throw new IllegalArgumentException("coordinate is null");

        return nodes.get(coordinate) == ETerrain.Water;
    }

    public ETerrain getTerrain(XYPair coordinate) {
        if (coordinate == null)
            throw new IllegalArgumentException("coordinate is null");

        return nodes.get(coordinate);
    }
}
