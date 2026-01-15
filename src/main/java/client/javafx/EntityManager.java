package client.javafx;

import client.data.XYPair;
import client.data.fromserver.FullMap;
import javafx.animation.Animation;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityManager {
    private static final Logger logger = LoggerFactory.getLogger(EntityManager.class);

    private static final double Y_SURFACE = -1.9375;
    private static final double Y_FORT = -2;
    private static final double Y_MOUNTAIN = -3;

    private static final double PLAYER_X_OFFSET = 0.25;

    private static final double Y_FLOATING_TREASURE = -2.125;
    private static final double Y_LAYING_TREASURE = -1.78125;

    private final Group worldRoot;
    private final Assets assets;
    private final AnimationManager animationManager;

    private MeshView myPlayerModel;
    private MeshView enemyPlayerModel;
    private Node myTreasureModel;
    private Animation myTreasureAnimation;

    public EntityManager(Group worldRoot, Assets assets, AnimationManager animationManager) {
        if (worldRoot == null)
            throw new IllegalArgumentException("worldRoot is null");
        if (assets == null)
            throw new IllegalArgumentException("assets is null");
        if (animationManager == null)
            throw new IllegalArgumentException("animationManager is null");

        this.worldRoot = worldRoot;
        this.assets = assets;
        this.animationManager = animationManager;
    }

    public void handleEntities(FullMap fullMap, TerrainManager terrainManager) {
        if (fullMap == null)
            throw new IllegalArgumentException("fullMap is null");
        if (terrainManager == null)
            throw new IllegalArgumentException("terrainManager is null");

        fullMap.getOptionalMyPlayerPosition().ifPresent(coordinate -> {
            double targetY = getTargetY(fullMap, coordinate);
            if (myPlayerModel == null) {
                myPlayerModel = assets.createMeshView("rabbit", "gold_rabbit", coordinate, targetY);
                worldRoot.getChildren().add(myPlayerModel);
            }
            double xOffset = fullMap.getOptionalEnemyPlayerPosition().filter(coordinate::equals).isPresent() ? -PLAYER_X_OFFSET : 0;
            animationManager.movePlayer(myPlayerModel, coordinate, xOffset, targetY);
        });

        fullMap.getOptionalEnemyPlayerPosition().ifPresent(coordinate -> {
            double targetY = getTargetY(fullMap, coordinate);
            if (enemyPlayerModel == null) {
                enemyPlayerModel = assets.createMeshView("rabbit", "salt_rabbit", coordinate, targetY);
                worldRoot.getChildren().add(enemyPlayerModel);
            }
            double xOffset = fullMap.getOptionalMyPlayerPosition().filter(coordinate::equals).isPresent() ? PLAYER_X_OFFSET : 0;
            animationManager.movePlayer(enemyPlayerModel, coordinate, xOffset, targetY);
        });

        fullMap.getOptionalMyTreasurePosition().ifPresent(coordinate -> {
            if (myTreasureModel == null) {
                myTreasureModel = assets.createMeshView("emerald", "emerald", coordinate, Y_FLOATING_TREASURE);
                worldRoot.getChildren().add(myTreasureModel);
                myTreasureAnimation = animationManager.createTreasureAnimation(myTreasureModel);
                myTreasureAnimation.play();
            }

            if (myTreasureAnimation != null && fullMap.isMyTreasureCollected()) {
                myTreasureAnimation.stop();
                myTreasureModel.setRotationAxis(Rotate.Z_AXIS);
                myTreasureModel.setRotate(90);
                myTreasureModel.setTranslateY(Y_LAYING_TREASURE);
            }
        });

        fullMap.getOptionalMyFortPosition().ifPresent(coordinate ->
                terrainManager.setTopBlockBlockStyle(coordinate, "block_16-16", "cyan_wool"));
        fullMap.getOptionalEnemyFortPosition().ifPresent(coordinate ->
                terrainManager.setTopBlockBlockStyle(coordinate, "block_16-16", "red_wool"));
    }

    private static double getTargetY(FullMap fullMap, XYPair coordinate) {
        if (fullMap == null)
            throw new IllegalArgumentException("fullMap is null");
        if (coordinate == null)
            throw new IllegalArgumentException("coordinate is null");

        if (fullMap.isMountain(coordinate))
            return Y_MOUNTAIN;

        boolean isOnFort = fullMap.getOptionalMyFortPosition().filter(coordinate::equals).isPresent() ||
                fullMap.getOptionalEnemyFortPosition().filter(coordinate::equals).isPresent();

        return isOnFort ? Y_FORT : Y_SURFACE;
    }
}