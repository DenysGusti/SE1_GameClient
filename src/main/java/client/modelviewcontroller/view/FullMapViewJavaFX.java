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

    private static final PhongMaterial WATER_MATERIAL = new PhongMaterial(new Color(1, 1, 1, 0.98)) {{
        setSpecularColor(Color.TRANSPARENT);
    }};

    private final Map<String, PhongMaterial> materials = new HashMap<>();
    private final Image[] waterTextures;
    private final Map<String, TriangleMesh> meshes;

    private final Group worldRoot;
    private final Group waterGroup = new Group();
    private final Set<XYPair> coordinates = new HashSet<>();

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
            Objects.requireNonNull(textures.get(textureName), "texture is null");
            phongMaterial.setDiffuseMap(textures.get(textureName));
            materials.put(textureName, phongMaterial);
        });

        this.worldRoot.getChildren().add(waterGroup);
        Platform.runLater(this::startWaterAnimation);
    }

    private static MeshView createMeshView(TriangleMesh triangleMesh, PhongMaterial phongMaterial) {
        if (triangleMesh == null)
            throw new IllegalArgumentException("triangleMesh is null");
        if (phongMaterial == null)
            throw new IllegalArgumentException("phongMaterial is null");

        var meshView = new MeshView(triangleMesh);
        meshView.setMaterial(phongMaterial);
        meshView.setCullFace(CullFace.BACK);
        return meshView;
    }

    @Override
    public void update(FullMap fullMap) {
        if (fullMap == null)
            throw new IllegalArgumentException("fullMap is null");

        Platform.runLater(() -> render(fullMap));
    }

    private void render(FullMap fullMap) {
        if (coordinates.size() < 100) {
            fullMap.nodes().forEach((coordinate, fullMapNode) -> {
                if (coordinates.add(coordinate))
                    createPillar(coordinate, fullMapNode);
            });
            sortWaterByCameraDistance();
            waterGroup.toFront();
        }

        handleEntities(fullMap);
    }

    private void sortWaterByCameraDistance() {
        List<Node> sortedChildren = new ArrayList<>(waterGroup.getChildren());

        sortedChildren.sort(Comparator.comparingDouble(node -> {
            double dx = node.getTranslateX() - cameraMovementDetector.getLastCameraX();
            double dy = node.getTranslateY() - cameraMovementDetector.getLastCameraY();
            double dz = node.getTranslateZ() - cameraMovementDetector.getLastCameraZ();
            return dx * dx + dy * dy + dz * dz;
        }));

        waterGroup.getChildren().setAll(sortedChildren);
    }

    void createPillar(XYPair coordinate, FullMapNode fullMapNode) {
        MeshView baseBlock = createMeshView(meshes.get("block"), materials.get("stone"));
        baseBlock.setTranslateX(coordinate.y());
        baseBlock.setTranslateZ(coordinate.x());
        baseBlock.setTranslateY(0);
        worldRoot.getChildren().add(baseBlock);

        switch (fullMapNode.terrain()) {
            case Grass -> {
                MeshView grassBlock = createMeshView(meshes.get("block"), materials.get("grass"));
                grassBlock.setTranslateX(coordinate.y());
                grassBlock.setTranslateZ(coordinate.x());
                grassBlock.setTranslateY(-1);
                worldRoot.getChildren().add(grassBlock);
            }
            case Water -> {
                MeshView waterBlock = createMeshView(meshes.get("block_14-16"), WATER_MATERIAL);
                waterBlock.setTranslateX(coordinate.y());
                waterBlock.setTranslateZ(coordinate.x());
                waterBlock.setTranslateY(-1);
                waterGroup.getChildren().add(waterBlock);
            }
            case Mountain -> {
                MeshView bottomStoneBlock = createMeshView(meshes.get("block"), materials.get("stone"));
                bottomStoneBlock.setTranslateX(coordinate.y());
                bottomStoneBlock.setTranslateZ(coordinate.x());
                bottomStoneBlock.setTranslateY(-1);
                worldRoot.getChildren().add(bottomStoneBlock);

                MeshView topStoneBlock = createMeshView(meshes.get("block"), materials.get("stone"));
                topStoneBlock.setTranslateX(coordinate.y());
                topStoneBlock.setTranslateZ(coordinate.x());
                topStoneBlock.setTranslateY(-2);
                worldRoot.getChildren().add(topStoneBlock);

                MeshView snowBlock = createMeshView(meshes.get("block_2-16"), materials.get("snow"));
                snowBlock.setTranslateX(coordinate.y());
                snowBlock.setTranslateZ(coordinate.x());
                snowBlock.setTranslateY(-3);
                worldRoot.getChildren().add(snowBlock);
            }
        }

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
                myPlayerModel = createMeshView(meshes.get("rabbit"), materials.get("gold_rabbit"));
                worldRoot.getChildren().add(myPlayerModel);
            }
            double xOffset = fullMap.getOptionalEnemyPlayerPosition().filter(coordinate::equals).isPresent() ? -0.25 : 0;
            double yOffset = fullMap.nodes().get(coordinate).isMountain() ? -3 : -2;
            animateMovement(myPlayerModel, coordinate, xOffset, yOffset);
        });

        fullMap.getOptionalEnemyPlayerPosition().ifPresent(coordinate -> {
            if (enemyPlayerModel == null) {
                enemyPlayerModel = createMeshView(meshes.get("rabbit"), materials.get("salt_rabbit"));
                worldRoot.getChildren().add(enemyPlayerModel);
            }
            double xOffset = fullMap.getOptionalMyPlayerPosition().filter(coordinate::equals).isPresent() ? 0.25 : 0;
            double yOffset = fullMap.nodes().get(coordinate).isMountain() ? -3 : -2;
            animateMovement(enemyPlayerModel, coordinate, xOffset, yOffset);
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

    private void animateMovement(Node node, XYPair coordinate, double xOffset, double yOffset) {
        var translateTransition = new TranslateTransition(Duration.millis(300), node);
        translateTransition.setToX(coordinate.y() + 0.5 + xOffset);
        translateTransition.setToY(yOffset);
        translateTransition.setToZ(coordinate.x() + 0.5);
        translateTransition.play();
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