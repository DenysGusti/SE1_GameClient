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
    private static final String fogOfWarEmoji = "☁️";

    private static final String myFortEmoji = "🏰";
    private static final String enemyFortEmoji = "🏯";
    private static final String myTreasureBackgroundEmoji = "🪙";

    private static final String myPlayerEmoji = "😇";
    private static final String enemyPlayerEmoji = "😈";
    private static final String bothPlayersEmoji = "⚔️";
    private static final String myTreasureEmoji = "💰";

    private static final String[] numbers = new String[]{"0️⃣", "1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣", "7️⃣", "8️⃣", "9️⃣"};
    private static final String numberBackground = "▪️";

    public void renderFullMap(FullMap fullMap) {
        if (fullMap == null)
            throw new IllegalArgumentException("fullMap is null");

        XYPair size = fullMap.size();
        XYPair topLeft = fullMap.getOptionalTopLeftCoordinate().orElse(new XYPair(0, 0));
        if (fullMap.nodes().size() == 50) {
            logger.warn(fullMap.topLeftCoordinate().toString());
            logger.warn(fullMap.bottomRightCoordinate().toString());
            logger.warn(fullMap.size().toString());
        }

        var bufferDimension = new XYPair(size.x() + 2, size.y() + 2);
        var screenBuffer = new ScreenBuffer(bufferDimension);

        fullMap.nodes().forEach((coordinate, node) -> {
            var offset = new XYPair(coordinate.x() - topLeft.x() + 1, coordinate.y() - topLeft.y() + 1);
            String[] tile = stringifyFullMapTile(fullMap, coordinate);
            screenBuffer.drawTile(offset, tile);
        });

        for (int xIndex = 0; xIndex < size.x(); ++xIndex) {
            var offsetTop = new XYPair(xIndex + 1, 0);
            var offsetBottom = new XYPair(xIndex + 1, size.y() + 1);

            String[] numberTileLines = stringifyNumberTile(xIndex + topLeft.x());
            screenBuffer.drawTile(offsetTop, numberTileLines);
            screenBuffer.drawTile(offsetBottom, numberTileLines);
        }

        for (int yIndex = 0; yIndex < size.y(); ++yIndex) {
            var offsetLeft = new XYPair(0, yIndex + 1);
            var offsetRight = new XYPair(size.x() + 1, yIndex + 1);

            String[] numberTileLines = stringifyNumberTile(yIndex + topLeft.y());
            screenBuffer.drawTile(offsetLeft, numberTileLines);
            screenBuffer.drawTile(offsetRight, numberTileLines);
        }

        String[] cornerTileLines = cornerTile();
        screenBuffer.drawTile(new XYPair(0, 0), cornerTileLines);
        screenBuffer.drawTile(new XYPair(size.x() + 1, 0), cornerTileLines);
        screenBuffer.drawTile(new XYPair(0, size.y() + 1), cornerTileLines);
        screenBuffer.drawTile(new XYPair(size.x() + 1, size.y() + 1), cornerTileLines);

        screenBuffer.print();
    }

    private String[] stringifyHalfMapTile(HalfMap halfMap, XYPair coordinate) {
        if (halfMap == null)
            throw new IllegalArgumentException("halfMap is null");
        if (coordinate == null)
            throw new IllegalArgumentException("coordinate is null");

        ETerrain terrain = halfMap.nodes().get(coordinate);

        String terrainEmoji = terrainEmojiConverter.get(terrain);

        String border = halfMap.potentialForts().contains(coordinate) ? terrainEmoji : myFortEmoji;

        return new String[]{
                border + border + border,
                border + terrainEmoji + border,
                border + border + border
        };
    }

    private String[] stringifyNumberTile(int number) {
        int digitsValue = number % 10;
        int tensValue = (number / 10) % 10;
        String centerContent = tensValue != 0 ? numbers[tensValue] + numbers[digitsValue] : numbers[digitsValue] + numberBackground;

        return new String[]{
                numberBackground + numberBackground + numberBackground,
                numberBackground + centerContent,
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
        if (fullMap == null)
            throw new IllegalArgumentException("fullMap is null");
        if (coordinate == null)
            throw new IllegalArgumentException("coordinate is null");

        FullMapNode fullMapNode = fullMap.nodes().get(coordinate);
        Objects.requireNonNull(fullMapNode, "fullMapNode is null");

        String terrainEmoji = terrainEmojiConverter.get(fullMapNode.terrain());

        String border = terrainEmoji;
        if (fullMap.getOptionalMyFortPosition().filter(coordinate::equals).isPresent())
            border = myFortEmoji;
        else if (fullMap.getOptionalEnemyFortPosition().filter(coordinate::equals).isPresent())
            border = enemyFortEmoji;
        else if (fullMap.getOptionalMyTreasurePosition().filter(coordinate::equals).isPresent())
            border = myTreasureBackgroundEmoji;

        String center = fullMapNode.isRevealed() ? terrainEmoji : fogOfWarEmoji;
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
                border + border + border,
                border + center + border,
                border + border + border
        };
    }
}