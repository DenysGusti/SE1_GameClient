package client.modelviewcontroller.view;

import client.data.PlayerInformation;
import client.data.fromserver.EPlayerGameState;
import client.data.fromserver.PlayerState;
import client.modelviewcontroller.model.PlayerModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;

public class PlayerView {
    private static final Logger logger = LoggerFactory.getLogger(PlayerView.class);

    private static final Map<EPlayerGameState, String> playerGameStateEmojiConverter =
            Map.of(
                    EPlayerGameState.MustWait, "⏳",
                    EPlayerGameState.MustAct, "🤔",
                    EPlayerGameState.Won, "🥳",
                    EPlayerGameState.Lost, "😭"
            );

    private static final Map<Boolean, String> playerHasCollectedTreasureEmojiConverter =
            Map.of(
                    false, "😐",
                    true, "🤑"
            );

    private static final int PLAYER_INFO_WIDTH = 80;

    public void renderMyPlayerState(PlayerState playerState) {
        Objects.requireNonNull(playerState, "playerState must not be null");
        System.out.println("My Player");
        renderPlayerState(playerState);
    }

    public void renderEnemyPlayerState(PlayerState playerState) {
        Objects.requireNonNull(playerState, "playerState must not be null");
        System.out.println("Enemy Player");
        renderPlayerState(playerState);
    }

    private void renderPlayerState(PlayerState playerState) {
        Objects.requireNonNull(playerState, "playerState must not be null");
        renderPlayerInformation(playerState.playerInformation());
        System.out.print(" " + playerHasCollectedTreasureEmojiConverter.get(playerState.hasCollectedTreasure()));
        System.out.println(" " + playerGameStateEmojiConverter.get(playerState.gameState()));
    }

    private void renderPlayerInformation(PlayerInformation playerInformation) {
        Objects.requireNonNull(playerInformation, "playerInformation must not be null");

        String info = playerInformation.firstName() + " "
                + playerInformation.lastName() + " "
                + playerInformation.uaccount();

        System.out.printf("%-" + PLAYER_INFO_WIDTH + "s", info);
    }
}
