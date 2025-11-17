package client.mapgeneration;

import client.data.ETerrain;
import client.data.XYPair;
import client.data.fromclient.HalfMap;
import client.mapgeneration.validation.HalfMapValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class HalfMapGenerator {
    private static final Logger logger = LoggerFactory.getLogger(HalfMapGenerator.class);

    private static final XYPair HALF_MAP_SIZE = new XYPair(10, 5);
    private static final int HALF_MAP_NODES = 50;
    private static final int MIN_MOUNTAIN_NODES = 5;
    private static final int MIN_GRASS_NODES = 24;
    private static final int MIN_WATER_NODES = 7;
    private static final int REQUIRED_FORTS = 1;

    private final Random random;
    private final HalfMapValidator halfMapValidator;

    public HalfMapGenerator(Random random, HalfMapValidator halfMapValidator) {
        this.random = Objects.requireNonNull(random, "random must not be null");
        this.halfMapValidator = Objects.requireNonNull(halfMapValidator, "halfMapValidator must not be null");
    }

    public HalfMap generateHalfMap() {
        for (int attempt = 0; attempt < 100; ++attempt) {
            Map<XYPair, ETerrain> nodes = createRandomTerrain();
            Set<XYPair> potentialForts = placePotentialForts(nodes);
            HalfMap halfMap = new HalfMap(nodes, potentialForts);

            if (halfMapValidator.isValid(halfMap)) {
                logger.info("Generated a valid map in {} attempts.", attempt);
                return halfMap;
            }
        }

        logger.error("Failed to generate a valid map after 100 attempts!");
        throw new RuntimeException("Map generation failed. Check rules.");
    }

    private Map<XYPair, ETerrain> createRandomTerrain() {
        List<ETerrain> terrainPool = new ArrayList<>();

        terrainPool.addAll(Collections.nCopies(MIN_MOUNTAIN_NODES, ETerrain.Mountain));
        terrainPool.addAll(Collections.nCopies(MIN_GRASS_NODES, ETerrain.Grass));
        terrainPool.addAll(Collections.nCopies(MIN_WATER_NODES, ETerrain.Water));

        ETerrain[] allTerrains = {ETerrain.Grass, ETerrain.Water, ETerrain.Mountain};
        int remaining = HALF_MAP_NODES - terrainPool.size();
        for (int i = 0; i < remaining; ++i)
            terrainPool.add(allTerrains[random.nextInt(allTerrains.length)]);

        Collections.shuffle(terrainPool, random);
        Map<XYPair, ETerrain> nodes = new HashMap<>();
        int index = 0;
        for (int x = 0; x < HALF_MAP_SIZE.x(); x++)
            for (int y = 0; y < HALF_MAP_SIZE.y(); y++)
                nodes.put(new XYPair(x, y), terrainPool.get(index++));

        return nodes;
    }

    private Set<XYPair> placePotentialForts(Map<XYPair, ETerrain> nodes) {
        List<XYPair> grassTiles = nodes.entrySet().stream()
                .filter(entry -> entry.getValue() == ETerrain.Grass)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        Collections.shuffle(grassTiles, random);
        return new HashSet<>(grassTiles.subList(0, REQUIRED_FORTS));
    }
}
