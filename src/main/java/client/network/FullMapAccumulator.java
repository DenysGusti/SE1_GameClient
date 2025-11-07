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
        FullMap mapToAccumulate = Objects.requireNonNull(newTurnFullMap, "newTurnFullMap must not be null");

        if (!this.isTreasureRevealed && hasCollectedTreasure) {
            mapToAccumulate = newTurnFullMap.withRevealedMyTreasureFromMyPlayer();
            this.isTreasureRevealed = true;
        }

        if (this.fullMap == null)
            this.fullMap = mapToAccumulate;
        else
            this.fullMap = this.fullMap.withCombinedRevealedNodesFromOtherFullMap(mapToAccumulate);
    }

    public FullMap getFullMap() {
        return fullMap;
    }
}
