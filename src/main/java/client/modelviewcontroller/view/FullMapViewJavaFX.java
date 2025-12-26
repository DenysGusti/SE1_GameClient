package client.modelviewcontroller.view;

import client.data.ETerrain;
import client.data.XYPair;
import client.data.fromserver.FullMap;
import client.data.fromserver.FullMapNode;
import client.modelviewcontroller.observer.Subscriber;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class FullMapViewJavaFX implements Subscriber<FullMap> {
    private static final Logger logger = LoggerFactory.getLogger(FullMapViewJavaFX.class);

    protected static final Map<ETerrain, Color> terrainColorConverter = Map.of(
            ETerrain.Grass, Color.rgb(78, 176, 83),
            ETerrain.Mountain, Color.rgb(190, 195, 199),
            ETerrain.Water, Color.rgb(79, 132, 198)
    );

    private static final Color MY_FORT_COLOR = Color.DARKBLUE;
    private static final Color ENEMY_FORT_COLOR = Color.DARKRED;
    private static final Color MY_TREASURE_COLOR = Color.GOLDENROD;

    private final Group worldRoot;
    private final Map<XYPair, Box> terrainNodes = new HashMap<>();

    private Sphere myPlayerModel = null;
    private Sphere enemyPlayerModel = null;
    private Node myTreasureModel = null;

    public FullMapViewJavaFX(Group worldRoot) {
        if (worldRoot == null)
            throw new IllegalArgumentException("worldRoot is null");

        this.worldRoot = worldRoot;
    }

    @Override
    public void update(FullMap fullMap) {
        if (fullMap == null)
            throw new IllegalArgumentException("fullMap is null");

        Platform.runLater(() -> render(fullMap));
    }

    private void render(FullMap fullMap) {
        fullMap.nodes().forEach((coordinate, fullMapNode) -> {
            Box tile = terrainNodes.computeIfAbsent(coordinate, key -> {
                var box = new Box(1, 0.2, 1);
                box.setTranslateX(coordinate.y());
                box.setTranslateZ(coordinate.x());
                worldRoot.getChildren().add(box);
                return box;
            });

            Color tileColor = getTileColor(fullMap, coordinate, fullMapNode);

            var phongMaterial = new PhongMaterial(tileColor);
            phongMaterial.setDiffuseColor(tileColor);
            tile.setMaterial(phongMaterial);
        });

        fullMap.getOptionalMyPlayerPosition().ifPresent(coordinate -> {
            if (myPlayerModel == null) {
                myPlayerModel = createPlayerModel(Color.CYAN);
                worldRoot.getChildren().add(myPlayerModel);
            }
            animateMovement(myPlayerModel, coordinate, 0);
        });

        fullMap.getOptionalEnemyPlayerPosition().ifPresent(pos -> {
            if (enemyPlayerModel == null) {
                enemyPlayerModel = createPlayerModel(Color.ORANGERED);
                worldRoot.getChildren().add(enemyPlayerModel);
            }
            double xOffset = fullMap.getOptionalMyPlayerPosition().filter(pos::equals).isPresent() ? 0.2 : 0;
            animateMovement(enemyPlayerModel, pos, xOffset);
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

    private static Color getTileColor(FullMap fullMap, XYPair coordinate, FullMapNode fullMapNode) {
        Color tileColor = terrainColorConverter.get(fullMapNode.terrain());
        if (!fullMapNode.isRevealed())
            tileColor = tileColor.interpolate(Color.BLACK, 0.4);
        else {
            if (fullMap.getOptionalMyFortPosition().filter(coordinate::equals).isPresent())
                tileColor = MY_FORT_COLOR;
            else if (fullMap.getOptionalEnemyFortPosition().filter(coordinate::equals).isPresent())
                tileColor = ENEMY_FORT_COLOR;
            else if (fullMap.getOptionalMyTreasurePosition().filter(coordinate::equals).isPresent())
                tileColor = MY_TREASURE_COLOR;
        }
        return tileColor;
    }

    private static Sphere createPlayerModel(Color color) {
        var sphere = new Sphere(0.25);
        sphere.setTranslateY(-0.5);

        var phongMaterial = new PhongMaterial(color);
        phongMaterial.setDiffuseColor(color);
        phongMaterial.setSpecularColor(Color.WHITE);
        phongMaterial.setSpecularPower(32);
        sphere.setMaterial(phongMaterial);

        return sphere;
    }

    private void animateMovement(Node node, XYPair coordinate, double xOffset) {
        var translateTransition = new TranslateTransition(Duration.millis(300), node);
        translateTransition.setToX(coordinate.y() + xOffset);
        translateTransition.setToZ(coordinate.x());
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