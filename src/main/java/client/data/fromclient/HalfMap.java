package client.data.fromclient;

import client.data.ETerrain;
import client.data.XYPair;

import java.util.*;

public record HalfMap(Map<XYPair, ETerrain> nodes, Set<XYPair> potentialForts) {
    public HalfMap(Map<XYPair, ETerrain> nodes, Set<XYPair> potentialForts) {
        Objects.requireNonNull(nodes, "nodes map must not be null");
        Objects.requireNonNull(potentialForts, "potentialForts set must not be null");

        this.nodes = new HashMap<>(nodes);
        this.potentialForts = new HashSet<>(potentialForts);
    }

    @Override
    public Map<XYPair, ETerrain> nodes() {
        return Collections.unmodifiableMap(nodes);
    }

    @Override
    public Set<XYPair> potentialForts() {
        return Collections.unmodifiableSet(potentialForts);
    }
}
