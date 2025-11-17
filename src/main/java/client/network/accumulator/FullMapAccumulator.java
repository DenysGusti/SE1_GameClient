package client.network.accumulator;

import client.data.fromserver.FullMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class FullMapAccumulator {
    private static final Logger logger = LoggerFactory.getLogger(FullMapAccumulator.class);

    private final FullMapRevealer fullMapRevealer;

    private FullMap fullMap = null;
    private boolean isTreasureRevealed = false;

    public FullMapAccumulator(FullMapRevealer fullMapRevealer) {
        this.fullMapRevealer = fullMapRevealer;
    }

    public void accumulateFullMap(FullMap newTurnFullMap, boolean hasCollectedTreasure) {
        Objects.requireNonNull(newTurnFullMap, "newTurnFullMap must not be null");
        FullMap mapToAccumulate = fullMapRevealer.revealCoordinatesFromMyPlayer(newTurnFullMap);

        if (!isTreasureRevealed && hasCollectedTreasure) {
            isTreasureRevealed = true;
            mapToAccumulate = fullMapRevealer.revealMyTreasureFromMyPlayer(mapToAccumulate);
        }

        if (fullMap == null)
            fullMap = mapToAccumulate;
        else
            fullMap = fullMapRevealer.combineRevealedNodes(fullMap, mapToAccumulate);
    }

    public FullMap getFullMap() {
        return fullMap;
    }
}
