package client.halfmaplogic.generation;

import client.data.ETerrain;
import client.data.XYPair;
import client.data.fromclient.HalfMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.random.RandomGenerator;

public class HalfMapGenerator {
    private static final Logger logger = LoggerFactory.getLogger(HalfMapGenerator.class);

    private static final XYPair HALF_MAP_SIZE = new XYPair(10, 5);
    private static final int HALF_MAP_NODES = 50;
    private static final int MIN_MOUNTAIN_NODES = 5;
    private static final int MIN_GRASS_NODES = 24;
    private static final int MIN_WATER_NODES = 7;
    private static final int REQUIRED_FORTS = 1;

    private final RandomGenerator randomGenerator;

    public HalfMapGenerator(RandomGenerator randomGenerator) {
        if (randomGenerator == null)
            throw new IllegalArgumentException("randomGenerator is null");

        this.randomGenerator = randomGenerator;
    }

    public HalfMap generateHalfMap() {
        Map<XYPair, ETerrain> nodes = createRandomTerrain();
        Set<XYPair> potentialForts = placePotentialForts(nodes);
        return new HalfMap(nodes, potentialForts);
    }

    private Map<XYPair, ETerrain> createRandomTerrain() {
        Map<XYPair, ETerrain> nodes = new HashMap<>(HALF_MAP_NODES);

        nodes.put(new XYPair(0, 0), ETerrain.Water);
        nodes.put(new XYPair(1, 0), ETerrain.Water);
        nodes.put(new XYPair(2, 0), ETerrain.Mountain);
        nodes.put(new XYPair(3, 0), ETerrain.Mountain);
        nodes.put(new XYPair(4, 0), ETerrain.Mountain);
        nodes.put(new XYPair(5, 0), ETerrain.Mountain);
        nodes.put(new XYPair(6, 0), ETerrain.Mountain);
        nodes.put(new XYPair(7, 0), ETerrain.Mountain);
        nodes.put(new XYPair(8, 0), ETerrain.Water);
        nodes.put(new XYPair(9, 0), ETerrain.Water);

        nodes.put(new XYPair(0, 1), ETerrain.Grass);
        nodes.put(new XYPair(1, 1), ETerrain.Grass);
        nodes.put(new XYPair(2, 1), ETerrain.Grass);
        nodes.put(new XYPair(3, 1), ETerrain.Grass);
        nodes.put(new XYPair(4, 1), ETerrain.Grass);
        nodes.put(new XYPair(5, 1), ETerrain.Grass);
        nodes.put(new XYPair(6, 1), ETerrain.Grass);
        nodes.put(new XYPair(7, 1), ETerrain.Grass);
        nodes.put(new XYPair(8, 1), ETerrain.Grass);
        nodes.put(new XYPair(9, 1), ETerrain.Mountain);

        nodes.put(new XYPair(0, 2), ETerrain.Grass);
        nodes.put(new XYPair(1, 2), ETerrain.Mountain);
        nodes.put(new XYPair(2, 2), ETerrain.Grass);
        nodes.put(new XYPair(3, 2), ETerrain.Grass);
        nodes.put(new XYPair(4, 2), ETerrain.Mountain);
        nodes.put(new XYPair(5, 2), ETerrain.Grass);
        nodes.put(new XYPair(6, 2), ETerrain.Grass);
        nodes.put(new XYPair(7, 2), ETerrain.Mountain);
        nodes.put(new XYPair(8, 2), ETerrain.Grass);
        nodes.put(new XYPair(9, 2), ETerrain.Mountain);

        nodes.put(new XYPair(0, 3), ETerrain.Grass);
        nodes.put(new XYPair(1, 3), ETerrain.Grass);
        nodes.put(new XYPair(2, 3), ETerrain.Grass);
        nodes.put(new XYPair(3, 3), ETerrain.Grass);
        nodes.put(new XYPair(4, 3), ETerrain.Grass);
        nodes.put(new XYPair(5, 3), ETerrain.Grass);
        nodes.put(new XYPair(6, 3), ETerrain.Grass);
        nodes.put(new XYPair(7, 3), ETerrain.Grass);
        nodes.put(new XYPair(8, 3), ETerrain.Grass);
        nodes.put(new XYPair(9, 3), ETerrain.Mountain);

        nodes.put(new XYPair(0, 4), ETerrain.Water);
        nodes.put(new XYPair(1, 4), ETerrain.Water);
        nodes.put(new XYPair(2, 4), ETerrain.Mountain);
        nodes.put(new XYPair(3, 4), ETerrain.Mountain);
        nodes.put(new XYPair(4, 4), ETerrain.Mountain);
        nodes.put(new XYPair(5, 4), ETerrain.Mountain);
        nodes.put(new XYPair(6, 4), ETerrain.Mountain);
        nodes.put(new XYPair(7, 4), ETerrain.Mountain);
        nodes.put(new XYPair(8, 4), ETerrain.Water);
        nodes.put(new XYPair(9, 4), ETerrain.Water);

        return nodes;
    }

    private Set<XYPair> placePotentialForts(Map<XYPair, ETerrain> nodes) {
        if (nodes == null)
            throw new IllegalArgumentException("nodes is null");

        List<XYPair> potentialForts = new ArrayList<>();

        potentialForts.add(new XYPair(0, 2));
        potentialForts.add(new XYPair(1, 1));
        potentialForts.add(new XYPair(1, 3));
        potentialForts.add(new XYPair(2, 2));
        potentialForts.add(new XYPair(7, 1));
        potentialForts.add(new XYPair(7, 3));
        potentialForts.add(new XYPair(6, 2));
        potentialForts.add(new XYPair(8, 2));

        Collections.shuffle(potentialForts, randomGenerator);
        return new HashSet<>(potentialForts.subList(0, REQUIRED_FORTS));
    }
}
