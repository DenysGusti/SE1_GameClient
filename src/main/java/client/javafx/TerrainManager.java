package client.javafx;

import client.data.XYPair;
import client.data.fromserver.FullMap;
import client.data.fromserver.FullMapNode;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.shape.MeshView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class TerrainManager {
    private static final Logger logger = LoggerFactory.getLogger(TerrainManager.class);

    private static final int FULL_MAP_SIZE = 100;
    private static final double BLOCK_CENTER = 0.5;
    private static final double Y_BASE = 0;
    private static final double Y_SURFACE = -1;
    private static final double Y_MOUNTAIN = -2;
    private static final double Y_SNOW_CAP = -3;

    private final Group worldRoot;
    private final Group waterGroup = new Group();
    private final Map<XYPair, MeshView> coordinateTopBlocks = new HashMap<>();
    private final Assets assets;

    public TerrainManager(Group worldRoot, Assets assets) {
        if (worldRoot == null)
            throw new IllegalArgumentException("worldRoot is null");
        if (assets == null)
            throw new IllegalArgumentException("fullMapAssets is null");

        this.worldRoot = worldRoot;
        this.assets = assets;
        this.worldRoot.getChildren().add(waterGroup);
    }

    public void renderTerrain(FullMap fullMap, Point3D cameraPosition) {
        if (fullMap == null)
            throw new IllegalArgumentException("fullMap is null");
        if (cameraPosition == null)
            throw new IllegalArgumentException("cameraPosition is null");

        if (coordinateTopBlocks.size() < FULL_MAP_SIZE) {
            fullMap.nodes().forEach((coordinate, node) -> {
                if (!coordinateTopBlocks.containsKey(coordinate)) {
                    MeshView topBlock = createPillar(coordinate, node);
                    coordinateTopBlocks.put(coordinate, topBlock);
                }
            });
            sortWaterByCameraDistance(cameraPosition);
            waterGroup.toFront();
        }

        fullMap.nodes().forEach(this::updateRevealedNode);
    }

    private void updateRevealedNode(XYPair coordinate, FullMapNode fullMapNode) {
        if (coordinate == null)
            throw new IllegalArgumentException("coordinate is null");
        if (fullMapNode == null)
            throw new IllegalArgumentException("fullMapNode is null");
        if (!coordinateTopBlocks.containsKey(coordinate))
            throw new NoSuchElementException("Could not find top block for coordinate: " + coordinate);

        if (!fullMapNode.isRevealed())
            return;

        switch (fullMapNode.terrain()) {
            case Mountain -> coordinateTopBlocks.get(coordinate).setVisible(false);
            case Grass -> setTopBlockBlockStyle(coordinate, "block_15-16", "dirt_path");
            case Water -> setTopBlockBlockStyle(coordinate, "block_16-16", "ice");
        }
    }

    public void setTopBlockBlockStyle(XYPair coordinate, String meshName, String materialName) {
        if (coordinate == null)
            throw new IllegalArgumentException("coordinate is null");
        if (meshName == null)
            throw new IllegalArgumentException("meshName is null");
        if (materialName == null)
            throw new IllegalArgumentException("materialName is null");
        if (!coordinateTopBlocks.containsKey(coordinate))
            throw new NoSuchElementException("Could not find top block for coordinate: " + coordinate);

        MeshView topBlock = Objects.requireNonNull(coordinateTopBlocks.get(coordinate), "coordinateTopBlocks.get(coordinate) is null");
        topBlock.setMesh(assets.getMesh(meshName));
        topBlock.setMaterial(assets.getMaterial(materialName));
    }

    // returns top block
    private MeshView createPillar(XYPair coordinate, FullMapNode fullMapNode) {
        if (coordinate == null)
            throw new IllegalArgumentException("coordinate is null");
        if (fullMapNode == null)
            throw new IllegalArgumentException("fullMapNode is null");

        MeshView baseBlock = assets.createMeshView("block_16-16", "stone", coordinate, Y_BASE);
        worldRoot.getChildren().add(baseBlock);

        return switch (fullMapNode.terrain()) {
            case Grass -> {
                MeshView grassBlock = assets.createMeshView("block_16-16", "grass", coordinate, Y_SURFACE);
                worldRoot.getChildren().add(grassBlock);
                yield grassBlock;
            }
            case Water -> {
                MeshView waterBlock = assets.createMeshView("block_14-16", "water", coordinate, Y_SURFACE);
                waterGroup.getChildren().add(waterBlock);
                yield waterBlock;
            }
            case Mountain -> {
                worldRoot.getChildren().add(assets.createMeshView("block_16-16", "stone", coordinate, Y_SURFACE));
                worldRoot.getChildren().add(assets.createMeshView("block_16-16", "stone", coordinate, Y_MOUNTAIN));

                MeshView snowBlock = assets.createMeshView("block_02-16", "snow", coordinate, Y_SNOW_CAP);
                worldRoot.getChildren().add(snowBlock);
                yield snowBlock;
            }
        };
    }

    public void sortWaterByCameraDistance(Point3D cameraPosition) {
        if (cameraPosition == null)
            throw new IllegalArgumentException("cameraPosition is null");

        List<Node> sorted = new ArrayList<>(waterGroup.getChildren());
        sorted.sort(Comparator.comparingDouble(node -> {
            double dx = node.getTranslateX() + BLOCK_CENTER - cameraPosition.getX();
            double dy = node.getTranslateY() + BLOCK_CENTER - cameraPosition.getY();
            double dz = node.getTranslateZ() + BLOCK_CENTER - cameraPosition.getZ();
            return dx * dx + dy * dy + dz * dz;
        }));
        waterGroup.getChildren().setAll(sorted);
    }
}