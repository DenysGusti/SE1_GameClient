package client.modelviewcontroller.view;

import client.data.PlayerInformation;
import client.data.fromserver.EPlayerGameState;
import client.data.fromserver.PlayerState;
import client.modelviewcontroller.observer.Subscriber;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class PlayerView implements Subscriber<PlayerState> {
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

    private final String header;

    public PlayerView(String header) {
        if (header == null)
            throw new IllegalArgumentException("header is null");

        this.header = header;
    }

    @Override
    public void update(PlayerState playerState) {
        if (playerState == null)
            throw new IllegalArgumentException("playerState is null");

        System.out.printf("%-" + HEADER_WIDTH + "s", header + ":");

        renderPlayerInformation(playerState.playerInformation());

        System.out.print(" " + playerHasCollectedTreasureEmojiConverter.get(playerState.hasCollectedTreasure()));
        System.out.println(" " + playerGameStateEmojiConverter.get(playerState.gameState()));
    }

    private void renderPlayerInformation(PlayerInformation playerInformation) {
        if (playerInformation == null)
            throw new IllegalArgumentException("playerInformation is null");

        String info = playerInformation.firstName() + " "
                + playerInformation.lastName() + " "
                + playerInformation.uAccount();

        System.out.printf("%-" + PLAYER_INFO_WIDTH + "s", info);
    }
}