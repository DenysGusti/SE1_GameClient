package client.data.fromserver;

import client.data.ETerrain;

import java.util.Objects;

public record FullMapNode(ETerrain terrain, boolean isRevealed) {
    public FullMapNode {
        Objects.requireNonNull(terrain, "Terrain must not be null");
    }
}
