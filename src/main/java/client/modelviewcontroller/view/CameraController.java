package client.modelviewcontroller.view;

import javafx.scene.Camera;
import javafx.scene.transform.Rotate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CameraController {
    private static final Logger logger = LoggerFactory.getLogger(CameraController.class);

    private static final double ROTATION_PERIOD_DEGREES = 360;
    private static final double MAX_PITCH_ANGLE_DEGREES = 89;
    private static final double ROTATION_SPEED = 0.15;

    private final Camera camera;
    private final Rotate yRotate;
    private final Rotate xRotate;
    private double yaw;
    private double pitch;

    public CameraController(Camera camera, double initialYaw, double initialPitch) {
        if (camera == null)
            throw new IllegalArgumentException("camera is null");

        this.camera = camera;
        yaw = initialYaw;
        pitch = initialPitch;
        yRotate = new Rotate(yaw, Rotate.Y_AXIS);
        xRotate = new Rotate(pitch, Rotate.X_AXIS);
        camera.getTransforms().addAll(yRotate, xRotate);
    }

    public void rotate(double deltaX, double deltaY) {
        yaw = yaw + deltaX * ROTATION_SPEED;
        yaw = (yaw % ROTATION_PERIOD_DEGREES + ROTATION_PERIOD_DEGREES) % ROTATION_PERIOD_DEGREES;
        pitch = Math.clamp(pitch - deltaY * ROTATION_SPEED, -MAX_PITCH_ANGLE_DEGREES, MAX_PITCH_ANGLE_DEGREES);
        yRotate.setAngle(yaw);
        xRotate.setAngle(pitch);
    }

    // move forward/backward
    public void moveForward(double amount) {
        double yRad = Math.toRadians(yaw);
        camera.setTranslateX(camera.getTranslateX() + amount * Math.sin(yRad));
        camera.setTranslateZ(camera.getTranslateZ() + amount * Math.cos(yRad));
    }

    // move left/right
    public void moveStrafe(double amount) {
        double yRad = Math.toRadians(yaw + 90);
        camera.setTranslateX(camera.getTranslateX() + amount * Math.sin(yRad));
        camera.setTranslateZ(camera.getTranslateZ() + amount * Math.cos(yRad));
    }

    // move up/down
    public void moveVertical(double amount) {
        camera.setTranslateY(camera.getTranslateY() + amount);
    }

    public double getX() {
        return camera.getTranslateX();
    }

    public double getY() {
        return camera.getTranslateY();
    }

    public double getZ() {
        return camera.getTranslateZ();
    }

    public double getYaw() {
        return yaw;
    }

    public double getPitch() {
        return pitch;
    }
}