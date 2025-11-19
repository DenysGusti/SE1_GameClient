package client.modelviewcontroller.view;

import client.data.PlayerInformation;
import client.data.fromserver.EPlayerGameState;
import client.data.fromserver.PlayerState;
import client.modelviewcontroller.model.PlayerModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class PlayerView {
    private static final Logger logger = LoggerFactory.getLogger(PlayerView.class);

    private static final Map<EPlayerGameState, String> playerGameStateEmoji =
            Map.of(
                    EPlayerGameState.MustWait, "😴",
                    EPlayerGameState.MustAct, "🤔",
                    EPlayerGameState.Won, "🥳",
                    EPlayerGameState.Lost, "😵"
            );

    private static final Map<Boolean, String> playerHasCollectedTreasureEmoji =
            Map.of(
                    false, "😐",
                    true, "🤑"
            );

    public PlayerView(PlayerModel playerModel) {
        playerModel.subscribeOnMyPlayerStateUpdated(this::renderMyPlayerState);
        playerModel.subscribeOnEnemyPlayerStateUpdated(this::renderEnemyPlayerState);
    }

    public void renderMyPlayerState(PlayerState playerState) {
        System.out.print("My Player: ");
        renderPlayerState(playerState);
    }

    public void renderEnemyPlayerState(PlayerState playerState) {
        System.out.print("Enemy Player: ");
        renderPlayerState(playerState);
    }

    private void renderPlayerState(PlayerState playerState) {
        renderPlayerInformation(playerState.playerInformation());
        System.out.print(" " + playerHasCollectedTreasureEmoji.get(playerState.hasCollectedTreasure()));
        System.out.println(" " + playerGameStateEmoji.get(playerState.gameState()));
    }

    private void renderPlayerInformation(PlayerInformation playerInformation) {
        System.out.print(playerInformation.firstName() + " " + playerInformation.lastName() + " " + playerInformation.uaccount());
    }
}
