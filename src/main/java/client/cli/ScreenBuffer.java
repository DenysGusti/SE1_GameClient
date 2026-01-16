package client.cli;

import client.data.XYPair;

public class ScreenBuffer {
    private static final int EXPECTED_EMOJI_WIDTH = 4;
    private static final int TILE_SIZE = 3;
    // 3 * 4x1 (max emoji size)
    private static final XYPair TILE_DIMENSIONS = new XYPair(EXPECTED_EMOJI_WIDTH * TILE_SIZE, TILE_SIZE);

    private final StringBuilder[] rows;

    public ScreenBuffer(XYPair bufferDimensions) {
        if (bufferDimensions == null)
            throw new IllegalArgumentException("bufferDimensions is null");

        rows = new StringBuilder[bufferDimensions.y() * TILE_DIMENSIONS.y()];
        String blankRow = " ".repeat(bufferDimensions.x() * TILE_DIMENSIONS.x());

        for (int i = 0; i < rows.length; ++i)
            rows[i] = new StringBuilder(blankRow);
    }

    public void drawTile(XYPair tileOffset, String[] tileLines) {
        if (tileOffset == null)
            throw new IllegalArgumentException("tileOffset is null");
        if (tileLines == null)
            throw new IllegalArgumentException("tileLines is null");

        var scaledTileOffset = new XYPair(
                tileOffset.x() * TILE_DIMENSIONS.x(),
                tileOffset.y() * TILE_DIMENSIONS.y()
        );

        for (int i = 0; i < tileLines.length; ++i)
            rows[scaledTileOffset.y() + i]
                    .replace(scaledTileOffset.x(), scaledTileOffset.x() + tileLines[i].length(), tileLines[i]);
    }

    public void print() {
        System.out.println(String.join("\n", rows).replaceAll(" ", ""));
    }
}
