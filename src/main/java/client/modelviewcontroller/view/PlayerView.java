package client.modelviewcontroller.view;

import client.data.PlayerInformation;
import client.data.fromserver.EPlayerGameState;
import client.data.fromserver.PlayerState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

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
    private static final int HEADER_WIDTH = 15;

    public void renderMyPlayerState(PlayerState playerState) {
        if (playerState == null)
            throw new IllegalArgumentException("playerState must not be null");

        renderPlayerState("My Player", playerState);
    }

    public void renderEnemyPlayerState(PlayerState playerState) {
        if (playerState == null)
            throw new IllegalArgumentException("playerState must not be null");

        renderPlayerState("Enemy Player", playerState);
    }

    private void renderPlayerState(String header, PlayerState playerState) {
        if (header == null)
            throw new IllegalArgumentException("header must not be null");
        if (playerState == null)
            throw new IllegalArgumentException("playerState must not be null");

        System.out.printf("%-" + HEADER_WIDTH + "s", header + ":");

        renderPlayerInformation(playerState.playerInformation());

        System.out.print(" " + playerHasCollectedTreasureEmojiConverter.get(playerState.hasCollectedTreasure()));
        System.out.println(" " + playerGameStateEmojiConverter.get(playerState.gameState()));
    }

    private void renderPlayerInformation(PlayerInformation playerInformation) {
        if (playerInformation == null)
            throw new IllegalArgumentException("playerInformation must not be null");

        String info = playerInformation.firstName() + " "
                + playerInformation.lastName() + " "
                + playerInformation.uaccount();

        System.out.printf("%-" + PLAYER_INFO_WIDTH + "s", info);
    }
}