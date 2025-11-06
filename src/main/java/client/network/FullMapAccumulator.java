package client.network;

import client.data.fromserver.FullMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class FullMapAccumulator {
    private static final Logger logger = LoggerFactory.getLogger(FullMapAccumulator.class);

    private FullMap fullMap = null;
    private boolean isTreasureRevealed = false;

    public void accumulateFullMap(FullMap newTurnFullMap, boolean hasCollectedTreasure) {
        FullMap newFullMap = Objects.requireNonNull(newTurnFullMap, "newTurnFullMap must not be null");

        if (!this.isTreasureRevealed && hasCollectedTreasure) {
            newFullMap = newTurnFullMap.withRevealedMyTreasureFromMyPlayer();
            this.isTreasureRevealed = true;
        }

        this.fullMap = newFullMap.withCombinedRevealedNodesFromOtherFullMap(this.fullMap);
    }

    public FullMap getFullMap() {
        return fullMap;
    }
}
