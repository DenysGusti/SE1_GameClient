package client.cli;

import client.data.ETerrain;

import java.util.*;

public abstract class MapView {
    protected static final Map<ETerrain, String> terrainEmojiConverter =
            Map.of(
                    ETerrain.Grass, "🌿",
                    ETerrain.Mountain, "🏔️",
                    ETerrain.Water, "🌊"
            );

    protected static final String myFortEmoji = "🏰";

    private static final String[] numbers = new String[]{"0️⃣", "1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣", "7️⃣", "8️⃣", "9️⃣"};
    private static final String numberBackground = "▪️";

    protected String[] stringifyNumberTile(int number) {
        int digitsValue = number % 10;
        int tensValue = (number / 10) % 10;
        String centerContent = tensValue != 0 ? numbers[tensValue] + numbers[digitsValue] : numbers[digitsValue] + numberBackground;

        return new String[]{
                numberBackground + numberBackground + numberBackground,
                numberBackground + centerContent,
                numberBackground + numberBackground + numberBackground
        };
    }

    protected String[] cornerTile() {
        return new String[]{
                numberBackground + numberBackground + numberBackground,
                numberBackground + numberBackground + numberBackground,
                numberBackground + numberBackground + numberBackground
        };
    }
}