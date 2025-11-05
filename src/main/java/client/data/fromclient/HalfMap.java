package client.data.fromclient;

import client.data.ETerrain;
import client.data.XYPair;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public record HalfMap(Map<XYPair, ETerrain> nodes, Set<XYPair> potentialForts) {
    private static final Logger logger = LoggerFactory.getLogger(HalfMap.class);

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
