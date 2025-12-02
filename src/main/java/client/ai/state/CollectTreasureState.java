package client.ai.state;

import client.ai.AIPlayer;
import client.data.XYPair;
import client.data.fromserver.FullMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

public class CollectTreasureState extends AIState {
    private static final Logger logger = LoggerFactory.getLogger(CollectTreasureState.class);

    public CollectTreasureState(AIPlayer aiPlayer) {
        super(Objects.requireNonNull(aiPlayer, "aiPlayer must not be null"));
    }

    @Override
    public void handleFullMapUpdate(FullMap fullMap) {
        if (fullMap == null)
            throw new IllegalArgumentException("fullMap must not be null");

        if (fullMap.isMyTreasureCollected()) {
            logger.info("Treasure successfully collected.");

            AIState aiState;
            if (fullMap.getOptionalEnemyFortPosition().isPresent()) {
                logger.warn("Rare case: Enemy Fort location is known -> Switching to CaptureFortState.");
                aiState = new CaptureFortState(aiPlayer);
            } else {
                logger.info("Enemy Fort location unknown -> Switching to ScoutingFullEnemySideState.");
                aiState = new ScoutingFullEnemySideState(aiPlayer);
            }

            aiPlayer.setAIState(aiState);
            aiState.handleFullMapUpdate(fullMap);
            return;
        }

        if (aiPlayer.hasMoves())
            return;

        XYPair myTreasurePosition = fullMap.getOptionalMyTreasurePosition().orElseThrow();
        logger.debug("Calculating step-path to my treasure at {}", myTreasurePosition);

        List<XYPair> stepPathToGoal = aiPlayer.getStepPathToGoal(myTreasurePosition);
        if (stepPathToGoal.isEmpty())
            throw new RuntimeException("stepPathToGoal is empty");

        aiPlayer.setPlannedPath(stepPathToGoal.subList(1, stepPathToGoal.size()));
    }
}