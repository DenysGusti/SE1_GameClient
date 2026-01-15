package client.modelviewcontroller.view;

import client.data.fromserver.FullMap;
import client.javafx.*;
import client.observer.Subscriber;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FullMapViewJavaFX implements Subscriber<FullMap> {
    private static final Logger logger = LoggerFactory.getLogger(FullMapViewJavaFX.class);

    private final Assets assets;
    private final TerrainManager terrainManager;
    private final EntityManager entityManager;
    private final CameraMovementDetector cameraMovementDetector;

    public FullMapViewJavaFX(CameraMovementDetector cameraMovementDetector, Assets assets, TerrainManager terrainManager, EntityManager entityManager) {
        if (cameraMovementDetector == null)
            throw new IllegalArgumentException("cameraMovementDetector is null");
        if (assets == null)
            throw new IllegalArgumentException("assets is null");
        if (terrainManager == null)
            throw new IllegalArgumentException("terrainManager is null");
        if (entityManager == null)
            throw new IllegalArgumentException("entityManager is null");

        this.cameraMovementDetector = cameraMovementDetector;
        this.assets = assets;
        this.terrainManager = terrainManager;
        this.entityManager = entityManager;

        Platform.runLater(this::startWaterAnimation);
    }

    @Override
    public void update(FullMap fullMap) {
        if (fullMap == null)
            throw new IllegalArgumentException("fullMap is null");

        Platform.runLater(() -> {
            terrainManager.renderTerrain(fullMap, cameraMovementDetector.getLastCameraPosition());
            entityManager.handleEntities(fullMap, terrainManager);
        });
    }

    private void startWaterAnimation() {
        new AnimationTimer() {
            private long lastUpdateTimestamp = 0;
            private static final long UPDATE_THRESHOLD_NS = 25_000_000;

            @Override
            public void handle(long timestampNow) {
                if (timestampNow - lastUpdateTimestamp >= UPDATE_THRESHOLD_NS) {
                    assets.rotateWaterTexture();
                    lastUpdateTimestamp = timestampNow;

                    if (cameraMovementDetector.cameraMoved())
                        terrainManager.sortWaterByCameraDistance(cameraMovementDetector.getLastCameraPosition());
                }
            }
        }.start();
    }
}