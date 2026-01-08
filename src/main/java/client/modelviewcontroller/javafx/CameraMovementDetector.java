package client.modelviewcontroller.javafx;

import javafx.scene.Camera;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CameraMovementDetector {
    private static final Logger logger = LoggerFactory.getLogger(CameraMovementDetector.class);
    private static final double EPS = 1e-2;

    private final Camera camera;

    private double lastCameraX = 0;
    private double lastCameraY = 0;
    private double lastCameraZ = 0;

    public CameraMovementDetector(Camera camera) {
        if (camera == null)
            throw new IllegalArgumentException("camera is null");

        this.camera = camera;
    }

    public boolean cameraMoved() {
        double newCameraX = camera.getTranslateX();
        double newCameraY = camera.getTranslateY();
        double newCameraZ = camera.getTranslateZ();

        double absDeltaX = Math.abs(newCameraX - lastCameraX);
        double absDeltaY = Math.abs(newCameraY - lastCameraY);
        double absDeltaZ = Math.abs(newCameraZ - lastCameraZ);

        lastCameraX = newCameraX;
        lastCameraY = newCameraY;
        lastCameraZ = newCameraZ;

        return absDeltaX > EPS || absDeltaY > EPS || absDeltaZ > EPS;
    }

    public double getLastCameraX() {
        return lastCameraX;
    }

    public double getLastCameraY() {
        return lastCameraY;
    }

    public double getLastCameraZ() {
        return lastCameraZ;
    }
}
