package client.halfmaplogic.generation;

import client.data.ETerrain;
import client.data.XYPair;
import client.data.fromclient.HalfMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;

public class HalfMapGenerator {
    private static final Logger logger = LoggerFactory.getLogger(HalfMapGenerator.class);

    private static final XYPair HALF_MAP_SIZE = new XYPair(10, 5);
    private static final int HALF_MAP_NODES = 50;
    private static final int MIN_MOUNTAIN_NODES = 5;
    private static final int MIN_GRASS_NODES = 24;
    private static final int MIN_WATER_NODES = 7;
    private static final int REQUIRED_FORTS = 1;

    private static final Map<XYPair, ETerrain> GRASS_NODES = new HashMap<>(HALF_MAP_NODES);

    static {
        for (int x = 0; x < HALF_MAP_SIZE.x(); ++x)
            for (int y = 0; y < HALF_MAP_SIZE.y(); ++y)
                GRASS_NODES.put(new XYPair(x, y), ETerrain.Grass);
    }

    private final List<XYPair> coordinates = new ArrayList<>(GRASS_NODES.keySet());

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
        int totalMountains = HALF_MAP_NODES - MIN_GRASS_NODES - MIN_WATER_NODES;
        Map<XYPair, ETerrain> nodes = new HashMap<>(HALF_MAP_NODES);

        do {
            nodes.putAll(GRASS_NODES);
            Collections.shuffle(coordinates, randomGenerator);

            int waterPlaced = 0;
            for (XYPair coordinate : coordinates) {
                if (waterPlaced >= MIN_WATER_NODES)
                    break;

                if (coordinate.isOnBorder(HALF_MAP_SIZE)) {
                    nodes.put(coordinate, ETerrain.Water);
                    ++waterPlaced;
                }
            }

            List<XYPair> grassCoordinates = nodes.entrySet().stream()
                    .filter(e -> e.getValue() != ETerrain.Water)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            Collections.shuffle(grassCoordinates, randomGenerator);

            int mountainsPlaced = 0;
            for (XYPair coordinate : grassCoordinates) {
                if (mountainsPlaced >= totalMountains)
                    break;

                if (hasNoMountainNeighborNotOnBorder(nodes, coordinate) || coordinate.isOnBorder(HALF_MAP_SIZE)) {
                    nodes.put(coordinate, ETerrain.Mountain);
                    ++mountainsPlaced;
                }
            }

        } while (!allGrassHaveTwoGrassNeighbors(nodes));

        return nodes;
    }

    private static boolean hasNoMountainNeighborNotOnBorder(Map<XYPair, ETerrain> nodes, XYPair coordinate) {
        return coordinate.getAllNeighbors(HALF_MAP_SIZE).stream()
                .allMatch(neighbor -> nodes.get(neighbor) != ETerrain.Mountain || neighbor.isOnBorder(HALF_MAP_SIZE));
    }

    private static boolean allGrassHaveTwoGrassNeighbors(Map<XYPair, ETerrain> nodes) {
        return nodes.entrySet().stream()
                .filter(e -> e.getValue() == ETerrain.Grass)
                .map(Map.Entry::getKey)
                .allMatch(grass -> grass.getAdjacentNeighbors(HALF_MAP_SIZE).stream()
                        .filter(neighbor -> nodes.get(neighbor) == ETerrain.Grass).count() >= 2);
    }

    private Set<XYPair> placePotentialForts(Map<XYPair, ETerrain> nodes) {
        List<XYPair> grassCoordinates = nodes.entrySet().stream()
                .filter(entry -> entry.getValue() == ETerrain.Grass)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        Collections.shuffle(grassCoordinates, randomGenerator);
        return new HashSet<>(grassCoordinates.subList(0, REQUIRED_FORTS));
    }
}
