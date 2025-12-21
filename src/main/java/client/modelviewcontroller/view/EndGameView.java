package client.modelviewcontroller.view;

import client.data.fromserver.EPlayerGameState;
import client.modelviewcontroller.observer.Subscriber;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class EndGameView implements Subscriber<EPlayerGameState> {
    private static final Logger logger = LoggerFactory.getLogger(EndGameView.class);

    private static final Map<EPlayerGameState, String> playerGameStateEmojiConverter = Map.of(
            EPlayerGameState.Won, "🥳 VICTORY!",
            EPlayerGameState.Lost, "😭  DEFEAT!"
    );

    @Override
    public void update(EPlayerGameState playerGameState) {
        if (playerGameState == null)
            throw new IllegalArgumentException("playerGameState is null");

        String message = playerGameStateEmojiConverter.get(playerGameState);
        String buffer = "\n" + "=".repeat(40) +
                "\n             GAME FINISHED\n              " + message +
                "\n" + "=".repeat(40) + "\n";
        System.out.println(buffer);
    }
}