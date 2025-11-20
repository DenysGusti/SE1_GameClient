package client.modelviewcontroller.view;

import client.data.ETerrain;
import client.data.XYPair;
import client.data.fromclient.HalfMap;
import client.data.fromserver.FullMap;
import client.data.fromserver.FullMapNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class MapView {
    private static final Logger logger = LoggerFactory.getLogger(MapView.class);

    private static final Map<ETerrain, String> terrainEmojiConverter =
            Map.of(
                    ETerrain.Grass, "🌿",
                    ETerrain.Mountain, "🏔️",
                    ETerrain.Water, "🌊"
            );
    private static final String isRevealedEmoji = "🚩";

    private static final String myFortEmoji = "🏰";
    private static final String enemyFortEmoji = "🏯";
    private static final String myTreasureBackgroundEmoji = "🪙";

    private static final String myPlayerEmoji = "😇";
    private static final String enemyPlayerEmoji = "😈";
    private static final String bothPlayersEmoji = "⚔️";
    private static final String myTreasureEmoji = "💰";

    private static final String[] numbers = new String[]{"0️⃣", "1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣", "7️⃣", "8️⃣", "9️⃣"};
    private static final String numberBackground = "⭐";

    private static final int EXPECTED_EMOJI_WIDTH = 4;
    private static final int TILE_SIZE = 3;
    // 3 * 4x1 (max emoji size)
    private static final XYPair TILE_DIMENSIONS = new XYPair(EXPECTED_EMOJI_WIDTH * TILE_SIZE, TILE_SIZE);

    public void renderFullMap(FullMap fullMap) {
        logger.debug(fullMap.toString());
        Objects.requireNonNull(fullMap, "fullMap must not be null");
        XYPair size = fullMap.size();
        if (fullMap.nodes().size() == 50) {
            logger.warn(fullMap.topLeftCoordinate().toString());
            logger.warn(fullMap.bottomRightCoordinate().toString());
            logger.warn(fullMap.size().toString());
        }

        StringBuilder[] screenBuffer = new StringBuilder[TILE_DIMENSIONS.y() * (size.y() + 2)];
        String blankRow = " ".repeat(TILE_DIMENSIONS.x() * (size.x() + 2));
        for (int i = 0; i < screenBuffer.length; i++)
            screenBuffer[i] = new StringBuilder(blankRow);

        fullMap.nodes().forEach((coordinate, node) -> {
            var offset = new XYPair((coordinate.x() + 1) * TILE_DIMENSIONS.x(), (coordinate.y() + 1) * TILE_DIMENSIONS.y());

            String[] tile = stringifyFullMapTile(fullMap, coordinate);
            for (int i = 0; i < tile.length; ++i)
                screenBuffer[offset.y() + i].replace(offset.x(), offset.x() + tile[i].length(), tile[i]);
        });
        for (int xCoordinate = 0; xCoordinate < size.x(); ++xCoordinate) {
            var offsetTop = new XYPair((xCoordinate + 1) * TILE_DIMENSIONS.x(), 0);
            var offsetBottom = new XYPair((xCoordinate + 1) * TILE_DIMENSIONS.x(), (size.y() + 1) * TILE_DIMENSIONS.y());

            String[] tile = stringifyNumberTile(xCoordinate);
            for (int i = 0; i < tile.length; ++i) {
                screenBuffer[offsetTop.y() + i].replace(offsetTop.x(), offsetTop.x() + tile[i].length(), tile[i]);
                screenBuffer[offsetBottom.y() + i].replace(offsetBottom.x(), offsetBottom.x() + tile[i].length(), tile[i]);
            }
        }
        for (int yCoordinate = 0; yCoordinate < size.y(); ++yCoordinate) {
            var offsetLeft = new XYPair(0, (yCoordinate + 1) * TILE_DIMENSIONS.y());
            var offsetRight = new XYPair((size.x() + 1) * TILE_DIMENSIONS.x(), (yCoordinate + 1) * TILE_DIMENSIONS.y());

            String[] tile = stringifyNumberTile(yCoordinate);
            for (int i = 0; i < tile.length; ++i) {
                screenBuffer[offsetLeft.y() + i].replace(offsetLeft.x(), offsetLeft.x() + tile[i].length(), tile[i]);
                screenBuffer[offsetRight.y() + i].replace(offsetRight.x(), offsetRight.x() + tile[i].length(), tile[i]);
            }
        }
        {
            var offset = new XYPair(0, 0);
            String[] tile = cornerTile();
            for (int i = 0; i < tile.length; ++i)
                screenBuffer[offset.y() + i].replace(offset.x(), offset.x() + tile[i].length(), tile[i]);
        }
        {
            var offset = new XYPair((size.x() + 1) * TILE_DIMENSIONS.x(), 0);
            String[] tile = cornerTile();
            for (int i = 0; i < tile.length; ++i)
                screenBuffer[offset.y() + i].replace(offset.x(), offset.x() + tile[i].length(), tile[i]);
        }
        {
            var offset = new XYPair(0, (size.y() + 1) * TILE_DIMENSIONS.y());
            String[] tile = cornerTile();
            for (int i = 0; i < tile.length; ++i)
                screenBuffer[offset.y() + i].replace(offset.x(), offset.x() + tile[i].length(), tile[i]);
        }
        {
            var offset = new XYPair((size.x() + 1) * TILE_DIMENSIONS.x(), (size.y() + 1) * TILE_DIMENSIONS.y());
            String[] tile = cornerTile();
            for (int i = 0; i < tile.length; ++i)
                screenBuffer[offset.y() + i].replace(offset.x(), offset.x() + tile[i].length(), tile[i]);
        }
        System.out.println(String.join("\n", screenBuffer).replaceAll(" ", ""));
    }

    private String[] stringifyHalfMapTile(HalfMap halfMap, XYPair coordinate) {
        Objects.requireNonNull(halfMap, "halfMap must not be null");
        Objects.requireNonNull(coordinate, "coordinate must not be null");

        ETerrain terrain = halfMap.nodes().get(coordinate);

        String terrainEmoji = terrainEmojiConverter.get(terrain);

        String side = terrainEmoji;
        if (halfMap.potentialForts().contains(coordinate))
            side = myFortEmoji;

        return new String[]{
                terrainEmoji + side + terrainEmoji,
                side + terrainEmoji + side,
                terrainEmoji + side + terrainEmoji
        };
    }

    private String[] stringifyNumberTile(int number) {
        int digitsValue = number % 10;
        int tensValue = (number / 10) % 10;

        return new String[]{
                numberBackground + numberBackground + numberBackground,
                numberBackground + numbers[tensValue] + numbers[digitsValue],
                numberBackground + numberBackground + numberBackground
        };
    }

    private String[] cornerTile() {
        return new String[]{
                numberBackground + numberBackground + numberBackground,
                numberBackground + numberBackground + numberBackground,
                numberBackground + numberBackground + numberBackground
        };
    }

    private String[] stringifyFullMapTile(FullMap fullMap, XYPair coordinate) {
        Objects.requireNonNull(fullMap, "fullMap must not be null");
        Objects.requireNonNull(coordinate, "coordinate must not be null");

        FullMapNode fullMapNode = fullMap.nodes().get(coordinate);
        Objects.requireNonNull(fullMap, "fullMapNode must not be null");

        String terrainEmoji = terrainEmojiConverter.get(fullMapNode.terrain());
        String corner = fullMapNode.isRevealed() ? isRevealedEmoji : terrainEmoji;

        String side = terrainEmoji;
        if (fullMap.getOptionalMyFortPosition().filter(coordinate::equals).isPresent())
            side = myFortEmoji;
        else if (fullMap.getOptionalEnemyFortPosition().filter(coordinate::equals).isPresent())
            side = enemyFortEmoji;
        else if (fullMap.getOptionalMyTreasurePosition().filter(coordinate::equals).isPresent())
            side = myTreasureBackgroundEmoji;

        String center = terrainEmoji;
        if (fullMap.getOptionalMyPlayerPosition().filter(coordinate::equals).isPresent()) {
            if (fullMap.getOptionalEnemyPlayerPosition().filter(coordinate::equals).isPresent())
                center = bothPlayersEmoji;
            else
                center = myPlayerEmoji;
        } else if (fullMap.getOptionalEnemyPlayerPosition().filter(coordinate::equals).isPresent())
            center = enemyPlayerEmoji;
        else if (fullMap.getOptionalMyTreasurePosition().filter(coordinate::equals).isPresent()) {
            if (!fullMap.isMyTreasureCollected())
                center = myTreasureEmoji;
        }
        return new String[]{
                corner + side + corner,
                side + center + side,
                corner + side + corner
        };
    }
}
