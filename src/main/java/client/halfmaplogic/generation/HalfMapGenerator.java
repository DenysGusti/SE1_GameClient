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
        int totalMountains = MIN_MOUNTAIN_NODES + 7;
        Map<XYPair, ETerrain> nodes = new HashMap<>(HALF_MAP_NODES);

        while (true) {
            nodes.clear();
            for (int x = 0; x < HALF_MAP_SIZE.x(); ++x)
                for (int y = 0; y < HALF_MAP_SIZE.y(); ++y)
                    nodes.put(new XYPair(x, y), ETerrain.Grass);

            List<XYPair> coordinates = new ArrayList<>(nodes.keySet());
            Collections.shuffle(coordinates, randomGenerator);

            int mountainsPlaced = 0;
            for (XYPair coordinate : coordinates) {
                if (mountainsPlaced >= totalMountains)
                    break;

                boolean hasMountainNeighbor = coordinate.getAllNeighbors(HALF_MAP_SIZE).stream()
                        .anyMatch(neighbor -> nodes.get(neighbor) == ETerrain.Mountain);

                if (!hasMountainNeighbor) {
                    nodes.put(coordinate, ETerrain.Mountain);
                    ++mountainsPlaced;
                }
            }

            if (mountainsPlaced >= totalMountains)
                break;
        }

        List<XYPair> grassCoordinates = nodes.entrySet().stream()
                .filter(e -> e.getValue() == ETerrain.Grass)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        Collections.shuffle(grassCoordinates, randomGenerator);

        for (int i = 0; i < MIN_WATER_NODES; ++i)
            nodes.put(grassCoordinates.get(i), ETerrain.Water);

        return nodes;
    }

    private Set<XYPair> placePotentialForts(Map<XYPair, ETerrain> nodes) {
        if (nodes == null)
            throw new IllegalArgumentException("nodes is null");

        List<XYPair> grassTiles = nodes.entrySet().stream()
                .filter(entry -> entry.getValue() == ETerrain.Grass)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        Collections.shuffle(grassTiles, randomGenerator);
        return new HashSet<>(grassTiles.subList(0, REQUIRED_FORTS));
    }
}
