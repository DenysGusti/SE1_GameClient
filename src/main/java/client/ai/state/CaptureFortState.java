package client.ai.state;

import client.ai.AIPlayer;
import client.ai.exception.PathException;
import client.data.XYPair;
import client.data.fromserver.FullMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

public class CaptureFortState extends AIState {
    private static final Logger logger = LoggerFactory.getLogger(CaptureFortState.class);

    public CaptureFortState(AIPlayer aiPlayer) {
        super(Objects.requireNonNull(aiPlayer, "aiPlayer is null"));
    }

    @Override
    public void handleFullMapUpdate(FullMap fullMap) {
        if (fullMap == null)
            throw new IllegalArgumentException("fullMap is null");

        if (aiPlayer.hasMoves())
            return;

        XYPair enemyFort = fullMap.getOptionalEnemyFortPosition().orElseThrow();
        logger.info("Calculating step-path to enemy fort at {}", enemyFort);

        List<XYPair> stepPathToGoal = aiPlayer.getStepPathToTarget(enemyFort);
        if (stepPathToGoal.isEmpty())
            throw new PathException("stepPathToGoal is empty");

        aiPlayer.setPlannedStepPath(stepPathToGoal.subList(1, stepPathToGoal.size()));
    }
}