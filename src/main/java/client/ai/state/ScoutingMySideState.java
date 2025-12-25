package client.ai.state;

import client.ai.AIPlayer;
import client.ai.exception.PathException;
import client.data.XYPair;
import client.data.fromserver.FullMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

public class ScoutingMySideState extends AIState {
    private static final Logger logger = LoggerFactory.getLogger(ScoutingMySideState.class);

    public ScoutingMySideState(AIPlayer aiPlayer) {
        super(Objects.requireNonNull(aiPlayer, "aiPlayer is null"));
    }

    @Override
    public void handleFullMapUpdate(FullMap fullMap) {
        if (fullMap == null) throw new IllegalArgumentException("fullMap is null");

        if (fullMap.getOptionalMyTreasurePosition().isPresent()) {
            logger.info("My Treasure discovered at {}! Switching to CollectTreasureState.", fullMap.getOptionalMyTreasurePosition().get());
            AIState aiState = new CollectTreasureState(aiPlayer);
            aiPlayer.setAIState(aiState);
            aiState.handleFullMapUpdate(fullMap);
            return;
        }

        if (aiPlayer.hasMoves())
            return;

        logger.debug("Generating step-path for scouting full my side.");

        List<XYPair> stepPathForScouting = aiPlayer.getStepPathForScouting(fullMap, true);
        if (stepPathForScouting.isEmpty())
            throw new PathException("stepPathForScouting is empty");

        aiPlayer.setPlannedStepPath(stepPathForScouting.subList(1, stepPathForScouting.size()));
    }
}