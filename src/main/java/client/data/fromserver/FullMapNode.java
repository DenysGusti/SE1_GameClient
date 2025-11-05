package client.data.fromserver;

import client.data.ETerrain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public record FullMapNode(ETerrain terrain, boolean isRevealed) {
    private static final Logger logger = LoggerFactory.getLogger(FullMapNode.class);

    public FullMapNode {
        Objects.requireNonNull(terrain, "Terrain must not be null");
    }
}
