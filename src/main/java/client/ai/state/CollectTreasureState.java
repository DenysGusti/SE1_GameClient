package client.ai.state;

import client.ai.AIPlayer;
import client.data.XYPair;
import client.data.fromserver.FullMap;

import java.util.List;
import java.util.Objects;

public class CollectTreasureState extends AIState {
    public CollectTreasureState(AIPlayer aiPlayer) {
        super(Objects.requireNonNull(aiPlayer, "aiPlayer must not be null"));
    }

    @Override
    public void handleFullMapUpdate(FullMap fullMap) {
        if (fullMap == null)
            throw new IllegalArgumentException("fullMap must not be null");

        if (fullMap.isMyTreasureCollected()) {
            AIState aiState;
            if (fullMap.getOptionalEnemyFortPosition().isPresent())
                aiState = new CaptureFortState(aiPlayer);
            else
                aiState = new ScoutingFullEnemySideState(aiPlayer);

            aiPlayer.setAIState(aiState);
            aiState.handleFullMapUpdate(fullMap);
            return;
        }

        if (aiPlayer.hasMoves())
            return;

        List<XYPair> path = aiPlayer.moveToTarget(fullMap.getOptionalMyTreasurePosition().orElseThrow());
        if (path.isEmpty())
            throw new RuntimeException("path is empty");

        aiPlayer.setPlannedPath(path.subList(1, path.size()));
    }
}