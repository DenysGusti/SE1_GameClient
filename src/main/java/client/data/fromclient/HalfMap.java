package client.data.fromclient;

import client.data.ETerrain;
import client.data.XYPair;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public record HalfMap(Map<XYPair, ETerrain> nodes, Set<XYPair> potentialForts) {
    private static final Logger logger = LoggerFactory.getLogger(HalfMap.class);

    public HalfMap(Map<XYPair, ETerrain> nodes, Set<XYPair> potentialForts) {
        if (nodes == null)
            throw new IllegalArgumentException("nodes is null");
        if (potentialForts == null)
            throw new IllegalArgumentException("potentialForts is null");

        this.nodes = Map.copyOf(nodes);
        this.potentialForts = Set.copyOf(potentialForts);
    }
}
