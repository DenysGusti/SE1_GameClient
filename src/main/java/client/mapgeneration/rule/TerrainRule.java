package client.mapgeneration.rule;

import client.data.ETerrain;
import client.data.fromclient.HalfMap;
import client.mapgeneration.validation.IHalfMapValidationRule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class TerrainRule implements IHalfMapValidationRule {
    private static final Logger logger = LoggerFactory.getLogger(TerrainRule.class);

    private static final int HALF_MAP_NODES = 50;
    private static final int MIN_MOUNTAIN_NODES = 5;
    private static final int MIN_GRASS_NODES = 24;
    private static final int MIN_WATER_NODES = 7;

    @Override
    public boolean isValid(HalfMap halfMap) {
        Objects.requireNonNull(halfMap, "halfMap must not be null");

        long mountainCount = halfMap.nodes().values().stream().filter(t -> t == ETerrain.Mountain).count();
        if (mountainCount < MIN_MOUNTAIN_NODES)
            return false;

        long grassCount = halfMap.nodes().values().stream().filter(t -> t == ETerrain.Grass).count();
        if (grassCount < MIN_GRASS_NODES)
            return false;

        long waterCount = halfMap.nodes().values().stream().filter(t -> t == ETerrain.Water).count();
        if (waterCount < MIN_WATER_NODES)
            return false;

        return halfMap.nodes().size() == HALF_MAP_NODES;
    }
}
