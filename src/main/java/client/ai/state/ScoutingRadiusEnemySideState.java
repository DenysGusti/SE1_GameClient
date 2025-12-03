package client.ai.state;

import client.ai.AIPlayer;
import client.data.XYPair;
import client.data.fromserver.FullMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

public class ScoutingRadiusEnemySideState extends AIState {
    private static final Logger logger = LoggerFactory.getLogger(ScoutingRadiusEnemySideState.class);

    public ScoutingRadiusEnemySideState(AIPlayer aiPlayer) {
        super(Objects.requireNonNull(aiPlayer, "aiPlayer must not be null"));
    }

    @Override
    public void handleFullMapUpdate(FullMap fullMap) {
        if (fullMap == null)
            throw new IllegalArgumentException("fullMap must not be null");

        if (fullMap.getOptionalEnemyFortPosition().isPresent()) {
            logger.info("Enemy Fort spotted during radius search! Switching to CaptureFortState.");
            var aiState = new CaptureFortState(aiPlayer);
            aiPlayer.setAIState(aiState);
            aiState.handleFullMapUpdate(fullMap);
            return;
        }

        if (aiPlayer.hasMoves())
            return;

        logger.debug("Generating step-path for scouting radius enemy side.");

        List<XYPair> path = aiPlayer.getStepPathForScouting(fullMap, false);
        if (path.isEmpty())
            throw new RuntimeException("path is empty");

        aiPlayer.setPlannedPath(path.subList(1, path.size()));
    }
}