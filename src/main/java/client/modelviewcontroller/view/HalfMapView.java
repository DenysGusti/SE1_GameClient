package client.modelviewcontroller.view;

import client.data.ETerrain;
import client.data.XYPair;
import client.data.fromclient.HalfMap;
import client.modelviewcontroller.cli.MapView;
import client.modelviewcontroller.cli.ScreenBuffer;
import client.modelviewcontroller.observer.Subscriber;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HalfMapView extends MapView implements Subscriber<HalfMap> {
    private static final Logger logger = LoggerFactory.getLogger(HalfMapView.class);

    private static final XYPair HALF_MAP_SIZE = new XYPair(10, 5);

    @Override
    public void update(HalfMap halfMap) {
        if (halfMap == null)
            throw new IllegalArgumentException("halfMap is null");

        logger.info("Visualizing half-map...");

        var bufferDimension = new XYPair(HALF_MAP_SIZE.x() + 2, HALF_MAP_SIZE.y() + 2);
        var screenBuffer = new ScreenBuffer(bufferDimension);

        halfMap.nodes().forEach((coordinate, terrain) -> {
            var offset = new XYPair(coordinate.x() + 1, coordinate.y() + 1);
            String[] tile = stringifyHalfMapTile(halfMap, coordinate);
            screenBuffer.drawTile(offset, tile);
        });

        for (int xIndex = 0; xIndex < HALF_MAP_SIZE.x(); ++xIndex) {
            var offsetTop = new XYPair(xIndex + 1, 0);
            var offsetBottom = new XYPair(xIndex + 1, HALF_MAP_SIZE.y() + 1);

            String[] numberTileLines = stringifyNumberTile(xIndex);
            screenBuffer.drawTile(offsetTop, numberTileLines);
            screenBuffer.drawTile(offsetBottom, numberTileLines);
        }

        for (int yIndex = 0; yIndex < HALF_MAP_SIZE.y(); ++yIndex) {
            var offsetLeft = new XYPair(0, yIndex + 1);
            var offsetRight = new XYPair(HALF_MAP_SIZE.x() + 1, yIndex + 1);

            String[] numberTileLines = stringifyNumberTile(yIndex);
            screenBuffer.drawTile(offsetLeft, numberTileLines);
            screenBuffer.drawTile(offsetRight, numberTileLines);
        }

        String[] cornerTileLines = cornerTile();
        screenBuffer.drawTile(new XYPair(0, 0), cornerTileLines);
        screenBuffer.drawTile(new XYPair(HALF_MAP_SIZE.x() + 1, 0), cornerTileLines);
        screenBuffer.drawTile(new XYPair(0, HALF_MAP_SIZE.y() + 1), cornerTileLines);
        screenBuffer.drawTile(new XYPair(HALF_MAP_SIZE.x() + 1, HALF_MAP_SIZE.y() + 1), cornerTileLines);

        screenBuffer.print();
    }

    private String[] stringifyHalfMapTile(HalfMap halfMap, XYPair coordinate) {
        if (halfMap == null)
            throw new IllegalArgumentException("halfMap is null");
        if (coordinate == null)
            throw new IllegalArgumentException("coordinate is null");

        ETerrain terrain = halfMap.getTerrain(coordinate);

        String terrainEmoji = terrainEmojiConverter.get(terrain);

        String border = halfMap.potentialForts().contains(coordinate) ? myFortEmoji : terrainEmoji;

        return new String[]{
                border + border + border,
                border + terrainEmoji + border,
                border + border + border
        };
    }
}
