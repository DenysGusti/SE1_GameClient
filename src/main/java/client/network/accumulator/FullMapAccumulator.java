package client.network.accumulator;

import client.data.fromserver.FullMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FullMapAccumulator {
    private static final Logger logger = LoggerFactory.getLogger(FullMapAccumulator.class);

    private final FullMapRevealer fullMapRevealer;

    private FullMap fullMap = null;

    public FullMapAccumulator(FullMapRevealer fullMapRevealer) {
        this.fullMapRevealer = fullMapRevealer;
    }

    public void accumulateFullMap(FullMap newTurnFullMap) {
        if (newTurnFullMap == null)
            throw new IllegalArgumentException("newTurnFullMap is null");

        FullMap mapToAccumulate = fullMapRevealer.revealCoordinatesFromMyPlayer(newTurnFullMap);

        if (fullMap == null) {
            if (newTurnFullMap.isMyTreasureCollected()) {
                mapToAccumulate = mapToAccumulate.withMyTreasurePosition(mapToAccumulate.myPlayerPosition());
                logger.warn("Probably impossible case!");
            }

            fullMap = mapToAccumulate;
            return;
        }

        mapToAccumulate = fullMapRevealer.combineRevealedMyTreasure(fullMap, mapToAccumulate);

        if (!fullMap.isMyTreasureCollected() && newTurnFullMap.isMyTreasureCollected())
            mapToAccumulate = mapToAccumulate.withMyTreasurePosition(mapToAccumulate.myPlayerPosition());

        fullMap = fullMapRevealer.combineRevealedNodes(fullMap, mapToAccumulate);
    }

    public FullMap getFullMap() {
        return fullMap;
    }
}
