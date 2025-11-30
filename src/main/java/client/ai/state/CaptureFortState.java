package client.ai.state;

import client.ai.AIPlayer;
import client.data.XYPair;
import client.data.fromserver.FullMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

public class CaptureFortState extends AIState {
    private static final Logger logger = LoggerFactory.getLogger(CaptureFortState.class);

    public CaptureFortState(AIPlayer aiPlayer) {
        super(Objects.requireNonNull(aiPlayer, "aiPlayer must not be null"));
    }

    @Override
    public void handleFullMapUpdate(FullMap fullMap) {
        if (fullMap == null)
            throw new IllegalArgumentException("fullMap must not be null");

        if (aiPlayer.hasMoves())
            return;

        XYPair enemyFort = fullMap.getOptionalEnemyFortPosition().orElseThrow();
        logger.info("Plotting final trajectory to Enemy Fort at {}", enemyFort);

        List<XYPair> path = aiPlayer.moveToTarget(enemyFort);
        if (path.isEmpty())
            throw new RuntimeException("path is empty");

        aiPlayer.setPlannedPath(path.subList(1, path.size()));
    }
}