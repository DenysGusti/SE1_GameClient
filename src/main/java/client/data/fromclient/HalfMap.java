package client.data.fromclient;

import client.data.ETerrain;
import client.data.XYPair;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public record HalfMap(Map<XYPair, ETerrain> nodes, Set<XYPair> potentialForts) {
    private static final Logger logger = LoggerFactory.getLogger(HalfMap.class);

    public HalfMap(Map<XYPair, ETerrain> nodes, Set<XYPair> potentialForts) {
        this.nodes = Map.copyOf(Objects.requireNonNull(nodes, "nodes map must not be null"));
        this.potentialForts = Set.copyOf(Objects.requireNonNull(potentialForts, "potentialForts set must not be null"));
    }
}
