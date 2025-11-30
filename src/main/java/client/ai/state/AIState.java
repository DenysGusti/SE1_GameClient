package client.ai.state;

import client.ai.AIPlayer;
import client.data.fromserver.FullMap;

public abstract class AIState {
    AIPlayer aiPlayer;

    public AIState(AIPlayer aiPlayer) {
        if (aiPlayer == null)
            throw new IllegalArgumentException("aiPlayer cannot be null");

        this.aiPlayer = aiPlayer;
    }

    public abstract void handleFullMapUpdate(FullMap fullMap);
}