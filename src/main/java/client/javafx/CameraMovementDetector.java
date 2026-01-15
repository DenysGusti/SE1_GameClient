package client.javafx;

import javafx.geometry.Point3D;
import javafx.scene.Camera;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CameraMovementDetector {
    private static final Logger logger = LoggerFactory.getLogger(CameraMovementDetector.class);

    private static final double EPS = 1e-2;

    private final Camera camera;

    private Point3D lastCameraPosition;

    public CameraMovementDetector(Camera camera) {
        if (camera == null)
            throw new IllegalArgumentException("camera is null");

        this.camera = camera;
        lastCameraPosition = new Point3D(camera.getTranslateX(), camera.getTranslateY(), camera.getTranslateZ());
    }

    public boolean cameraMoved() {
        Point3D newCameraPosition = new Point3D(camera.getTranslateX(), camera.getTranslateY(), camera.getTranslateZ());

        boolean moved = newCameraPosition.distance(lastCameraPosition) > EPS;
        if (moved)
            lastCameraPosition = newCameraPosition;

        return moved;
    }

    public Point3D getLastCameraPosition() {
        return lastCameraPosition;
    }
}
