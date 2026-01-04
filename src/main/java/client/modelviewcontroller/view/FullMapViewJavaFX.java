package client.modelviewcontroller.view;

import client.data.XYPair;
import client.data.fromserver.FullMap;
import client.data.fromserver.FullMapNode;
import client.modelviewcontroller.javafx.CameraMovementDetector;
import client.modelviewcontroller.observer.Subscriber;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class FullMapViewJavaFX implements Subscriber<FullMap> {
    private static final Logger logger = LoggerFactory.getLogger(FullMapViewJavaFX.class);

    private static final PhongMaterial WATER_MATERIAL = new PhongMaterial();

    private final Map<String, PhongMaterial> materials = new HashMap<>();
    private final Image[] waterTextures;
    private final Map<String, TriangleMesh> meshes;

    private final Group worldRoot;
    private final Group waterGroup = new Group();
    private final Map<XYPair, MeshView> coordinateTopBlocks = new HashMap<>();

    private final CameraMovementDetector cameraMovementDetector;

    private MeshView myPlayerModel = null;
    private MeshView enemyPlayerModel = null;
    private Node myTreasureModel = null;

    public FullMapViewJavaFX(Group worldRoot, CameraMovementDetector cameraMovementDetector,
                             Map<String, Image> textures, Image[] waterTextures, Map<String, TriangleMesh> meshes) {
        if (worldRoot == null)
            throw new IllegalArgumentException("worldRoot is null");
        if (cameraMovementDetector == null)
            throw new IllegalArgumentException("cameraMovementDetector is null");
        if (textures == null)
            throw new IllegalArgumentException("textures is null");
        if (waterTextures == null)
            throw new IllegalArgumentException("waterTextures is null");
        if (meshes == null)
            throw new IllegalArgumentException("meshes is null");

        this.worldRoot = worldRoot;
        this.cameraMovementDetector = cameraMovementDetector;
        this.waterTextures = waterTextures;
        this.meshes = meshes;

        textures.forEach((textureName, image) -> {
            var phongMaterial = new PhongMaterial();
            Image texture = Objects.requireNonNull(textures.get(textureName), "texture is null");
            phongMaterial.setDiffuseMap(texture);
            materials.put(textureName, phongMaterial);
        });

        this.worldRoot.getChildren().add(waterGroup);
        Platform.runLater(this::startWaterAnimation);
    }

    private static MeshView createMeshView(TriangleMesh triangleMesh, PhongMaterial phongMaterial, XYPair coordinate) {
        if (triangleMesh == null)
            throw new IllegalArgumentException("triangleMesh is null");
        if (phongMaterial == null)
            throw new IllegalArgumentException("phongMaterial is null");
        if (coordinate == null)
            throw new IllegalArgumentException("coordinate is null");

        var meshView = new MeshView(triangleMesh);
        meshView.setMaterial(phongMaterial);
        meshView.setCullFace(CullFace.BACK);
        meshView.setTranslateX(coordinate.y());
        meshView.setTranslateZ(coordinate.x());
        return meshView;
    }

    @Override
    public void update(FullMap fullMap) {
        if (fullMap == null)
            throw new IllegalArgumentException("fullMap is null");

        Platform.runLater(() -> render(fullMap));
    }

    private void render(FullMap fullMap) {
        if (coordinateTopBlocks.size() < 100) {
            fullMap.nodes().forEach((coordinate, fullMapNode) -> {
                if (!coordinateTopBlocks.containsKey(coordinate)) {
                    MeshView topBlock = createPillar(coordinate, fullMapNode);
                    coordinateTopBlocks.put(coordinate, topBlock);
                }
            });
            sortWaterByCameraDistance();
            waterGroup.toFront();
        }

        fullMap.nodes().forEach((coordinate, fullMapNode) -> {
            if (fullMapNode.isRevealed())
                switch (fullMapNode.terrain()) {
                    case Mountain -> coordinateTopBlocks.get(coordinate).setVisible(false);
                    case Grass -> {
                        MeshView topBlock = coordinateTopBlocks.get(coordinate);
                        topBlock.setMesh(meshes.get("block_15-16"));
                        topBlock.setMaterial(materials.get("dirt_path"));
                    }
                    case Water -> {
                        MeshView topBlock = coordinateTopBlocks.get(coordinate);
                        topBlock.setMesh(meshes.get("block_16-16"));
                        topBlock.setMaterial(materials.get("ice"));
                    }
                }
        });

        handleEntities(fullMap);
    }

    private void sortWaterByCameraDistance() {
        List<Node> sortedChildren = new ArrayList<>(waterGroup.getChildren());

        sortedChildren.sort(Comparator.comparingDouble(node -> {
            double dx = node.getTranslateX() + 0.5 - cameraMovementDetector.getLastCameraX();
            double dy = node.getTranslateY() + 0.5 - cameraMovementDetector.getLastCameraY();
            double dz = node.getTranslateZ() + 0.5 - cameraMovementDetector.getLastCameraZ();
            return dx * dx + dy * dy + dz * dz;
        }));

        waterGroup.getChildren().setAll(sortedChildren);
    }

    MeshView createPillar(XYPair coordinate, FullMapNode fullMapNode) {
        MeshView baseBlock = createMeshView(meshes.get("block_16-16"), materials.get("stone"), coordinate);
        baseBlock.setTranslateY(0);
        worldRoot.getChildren().add(baseBlock);

        switch (fullMapNode.terrain()) {
            case Grass -> {
                MeshView grassBlock = createMeshView(meshes.get("block_16-16"), materials.get("grass"), coordinate);
                grassBlock.setTranslateY(-1);
                worldRoot.getChildren().add(grassBlock);
                return grassBlock;
            }
            case Water -> {
                MeshView waterBlock = createMeshView(meshes.get("block_14-16"), WATER_MATERIAL, coordinate);
                waterBlock.setTranslateY(-1);
                waterGroup.getChildren().add(waterBlock);
                return waterBlock;
            }
            case Mountain -> {
                MeshView bottomStoneBlock = createMeshView(meshes.get("block_16-16"), materials.get("stone"), coordinate);
                bottomStoneBlock.setTranslateY(-1);
                worldRoot.getChildren().add(bottomStoneBlock);

                MeshView topStoneBlock = createMeshView(meshes.get("block_16-16"), materials.get("stone"), coordinate);
                topStoneBlock.setTranslateY(-2);
                worldRoot.getChildren().add(topStoneBlock);

                MeshView snowBlock = createMeshView(meshes.get("block_02-16"), materials.get("snow"), coordinate);
                snowBlock.setTranslateY(-3);
                worldRoot.getChildren().add(snowBlock);
                return snowBlock;
            }
        }
        return baseBlock;
    }

    private void startWaterAnimation() {
        var waterTimer = new AnimationTimer() {
            private long lastUpdate = 0;
            private int frameIndex = 0;

            @Override
            public void handle(long timestampNow) {
                if (timestampNow - lastUpdate >= 25_000_000) {
                    WATER_MATERIAL.setDiffuseMap(waterTextures[frameIndex]);
                    frameIndex = (frameIndex + 1) % 64;
                    lastUpdate = timestampNow;

                    if (cameraMovementDetector.cameraMoved())
                        sortWaterByCameraDistance();
                }
            }
        };
        waterTimer.start();
    }

    private void handleEntities(FullMap fullMap) {
        fullMap.getOptionalMyPlayerPosition().ifPresent(coordinate -> {
            if (myPlayerModel == null) {
                myPlayerModel = createMeshView(meshes.get("rabbit"), materials.get("gold_rabbit"), coordinate);
                worldRoot.getChildren().add(myPlayerModel);
            }
            double xOffset = fullMap.getOptionalEnemyPlayerPosition().filter(coordinate::equals).isPresent() ? -0.25 : 0;
            double yOffset = fullMap.nodes().get(coordinate).isMountain() ? -3 : -2;
            movePlayer(myPlayerModel, coordinate, xOffset, yOffset);
        });

        fullMap.getOptionalEnemyPlayerPosition().ifPresent(coordinate -> {
            if (enemyPlayerModel == null) {
                enemyPlayerModel = createMeshView(meshes.get("rabbit"), materials.get("salt_rabbit"), coordinate);
                worldRoot.getChildren().add(enemyPlayerModel);
            }
            double xOffset = fullMap.getOptionalMyPlayerPosition().filter(coordinate::equals).isPresent() ? 0.25 : 0;
            double yOffset = fullMap.nodes().get(coordinate).isMountain() ? -3 : -2;
            movePlayer(enemyPlayerModel, coordinate, xOffset, yOffset);
        });

        fullMap.getOptionalMyTreasurePosition().ifPresent(pos -> {
            if (myTreasureModel == null) {
                myTreasureModel = createTreasure();
                myTreasureModel.setTranslateX(pos.y());
                myTreasureModel.setTranslateZ(pos.x());
                worldRoot.getChildren().add(myTreasureModel);
            }
            myTreasureModel.setVisible(!fullMap.isMyTreasureCollected());
        });
    }

    private static void movePlayer(Node playerModel, XYPair targetCoordinate, double xOffset, double targetY) {
        if (playerModel == null)
            throw new IllegalArgumentException("playerModel is null");
        if (targetCoordinate == null)
            throw new IllegalArgumentException("targetCoordinate is null");

        double targetX = targetCoordinate.y();
        double targetZ = targetCoordinate.x();

        double deltaX = targetX - playerModel.getTranslateX();
        double deltaZ = targetZ - playerModel.getTranslateZ();

        var translateTransition = new TranslateTransition(Duration.millis(300), playerModel);
        translateTransition.setToX(targetX + xOffset);
        translateTransition.setToY(targetY);
        translateTransition.setToZ(targetZ);

        if (Math.abs(deltaX) < 0.01 && Math.abs(deltaZ) < 0.01)
            translateTransition.play();
        else {
            var rotateTransition = getRotateTransition(playerModel, deltaZ, deltaX);
            var parallelTransition = new ParallelTransition(translateTransition, rotateTransition);
            parallelTransition.play();
        }
    }

    private static RotateTransition getRotateTransition(Node playerModel, double deltaZ, double deltaX) {
        if (playerModel == null)
            throw new IllegalArgumentException("playerModel is null");

        double targetAngle = -Math.toDegrees(Math.atan2(deltaZ, deltaX));
        double currentAngle = playerModel.getRotate();

        double deltaAngle = (targetAngle - currentAngle) % 360;
        if (deltaAngle > 180)
            deltaAngle -= 360;
        if (deltaAngle < -180)
            deltaAngle += 360;

        double finalAngle = currentAngle + deltaAngle;

        var rotateTransition = new RotateTransition(Duration.millis(300), playerModel);
        rotateTransition.setAxis(Rotate.Y_AXIS);
        rotateTransition.setFromAngle(currentAngle);
        rotateTransition.setToAngle(finalAngle);
        return rotateTransition;
    }

    private Node createTreasure() {
        Cylinder gold = new Cylinder(0.2, 0.05);
        gold.setMaterial(new PhongMaterial(Color.GOLD));
        gold.setTranslateY(-0.4);
        var rotateTransition = new RotateTransition(Duration.seconds(2), gold);
        rotateTransition.setAxis(Rotate.X_AXIS);
        rotateTransition.setByAngle(360);
        rotateTransition.setCycleCount(Animation.INDEFINITE);
        rotateTransition.setInterpolator(Interpolator.LINEAR);
        rotateTransition.play();
        return gold;
    }
}