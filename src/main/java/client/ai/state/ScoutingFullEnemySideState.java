package client.ai.state;

import client.ai.AIPlayer;
import client.data.XYPair;
import client.data.fromserver.FullMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Set;

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

        logger.debug("Generating traversal path for full enemy side.");
        Set<XYPair> unrevealedGrassNodes = aiPlayer.collectUnrevealedGrassNodes(fullMap, false);
        if (unrevealedGrassNodes.isEmpty())
            throw new RuntimeException("unrevealedGrassNodes is empty");

        List<XYPair> path = aiPlayer.traverseUnrevealedGrassNodes(unrevealedGrassNodes);
        if (path.isEmpty())
            throw new RuntimeException("path is empty");

        aiPlayer.setPlannedPath(path.subList(1, path.size()));
    }
}