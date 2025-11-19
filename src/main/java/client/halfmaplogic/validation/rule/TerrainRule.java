package client.halfmaplogic.validation.rule;

import client.data.ETerrain;
import client.data.fromclient.HalfMap;
import client.halfmaplogic.validation.Notification;

import client.halfmaplogic.validation.exception.TerrainRuleException;
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
    public void validate(HalfMap halfMap, Notification notification) {
        Objects.requireNonNull(halfMap, "halfMap must not be null");

        if (halfMap.nodes().size() != HALF_MAP_NODES)
            notification.addError( new TerrainRuleException("TerrainRule: Map must have exactly 50 nodes, but found " + halfMap.nodes().size()));

        long mountainCount = halfMap.nodes().values().stream().filter(t -> t == ETerrain.Mountain).count();
        if (mountainCount < MIN_MOUNTAIN_NODES)
            notification.addError(new TerrainRuleException("TerrainRule: Not enough mountains. Found " + mountainCount));

        long grassCount = halfMap.nodes().values().stream().filter(t -> t == ETerrain.Grass).count();
        if (grassCount < MIN_GRASS_NODES)
            notification.addError(new TerrainRuleException("TerrainRule: Not enough grass. Found " + grassCount));

        long waterCount = halfMap.nodes().values().stream().filter(t -> t == ETerrain.Water).count();
        if (waterCount < MIN_WATER_NODES)
            notification.addError(new TerrainRuleException("TerrainRule: Not enough water. Found " + waterCount));
    }
}
