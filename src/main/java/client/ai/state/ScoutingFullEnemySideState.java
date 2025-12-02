package client.ai.state;

import client.ai.AIPlayer;
import client.data.XYPair;
import client.data.fromserver.FullMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

public class ScoutingFullEnemySideState extends AIState {
    private static final Logger logger = LoggerFactory.getLogger(ScoutingFullEnemySideState.class);

    public ScoutingFullEnemySideState(AIPlayer aiPlayer) {
        super(Objects.requireNonNull(aiPlayer, "aiPlayer must not be null"));
    }

    @Override
    public void handleFullMapUpdate(FullMap fullMap) {
        if (fullMap == null)
            throw new IllegalArgumentException("fullMap must not be null");

        if (fullMap.getOptionalEnemyFortPosition().isPresent()) {
            logger.warn("Rare case: Enemy Fort spotted! Switching to CaptureFortState.");
            var aiState = new CaptureFortState(aiPlayer);
            aiPlayer.setAIState(aiState);
            aiState.handleFullMapUpdate(fullMap);
            return;
        }

        if (aiPlayer.isFirstValidEnemyPlayerPositionIdentified()) {
            logger.info("Enemy start position identified. Switching to optimized ScoutingRadiusEnemySideState.");
            var aiState = new ScoutingRadiusEnemySideState(aiPlayer);
            aiPlayer.setAIState(aiState);
            aiState.handleFullMapUpdate(fullMap);
            return;
        } else
            logger.warn("Watch out for same direction after handling! Non-atomic move.");

        if (aiPlayer.hasMoves())
            return;

        logger.debug("Generating step-path for scouting full enemy side.");

        List<XYPair> stepPathForScouting = aiPlayer.getStepPathForScouting(fullMap, false);
        if (stepPathForScouting.isEmpty())
            throw new RuntimeException("stepPathForScouting is empty");

        aiPlayer.setPlannedPath(stepPathForScouting.subList(1, stepPathForScouting.size()));
    }
}