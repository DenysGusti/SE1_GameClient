package client.ai.state;

import client.ai.AIPlayer;
import client.data.XYPair;
import client.data.fromserver.FullMap;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ScoutingRadiusEnemySideState extends AIState {
    public ScoutingRadiusEnemySideState(AIPlayer aiPlayer) {
        super(Objects.requireNonNull(aiPlayer, "aiPlayer must not be null"));
    }

    @Override
    public void handleFullMapUpdate(FullMap fullMap) {
        if (fullMap == null)
            throw new IllegalArgumentException("fullMap must not be null");

        if (fullMap.getOptionalEnemyFortPosition().isPresent()) {
            var aiState = new CaptureFortState(aiPlayer);
            aiPlayer.setAIState(aiState);
            aiState.handleFullMapUpdate(fullMap);
            return;
        }

        if (aiPlayer.hasMoves())
            return;

        Set<XYPair> unrevealedGrassNodes = aiPlayer.collectUnrevealedGrassNodes(fullMap, false);
        if (unrevealedGrassNodes.isEmpty())
            throw new RuntimeException("unrevealedGrassNodes is empty");

        List<XYPair> path = aiPlayer.traverseUnrevealedGrassNodes(unrevealedGrassNodes);
        if (path.isEmpty())
            throw new RuntimeException("path is empty");

        aiPlayer.setPlannedPath(path.subList(1, path.size()));
    }
}