package client.modelviewcontroller.javafx;

import javafx.scene.Camera;
import javafx.scene.transform.Rotate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CameraController {
    private static final Logger logger = LoggerFactory.getLogger(CameraController.class);

    private static final double ROTATION_PERIOD_DEGREES = 360;
    private static final double MAX_PITCH_ANGLE_DEGREES = 89;
    private static final double MIN_SPEED = 0.01;
    private static final double MAX_SPEED = 1.0;
    private static final double ROTATION_SPEED = 0.15;
    private static final double MOVEMENT_FACTOR = 0.1;

    private final Camera camera;
    private final Rotate yRotate;
    private final Rotate xRotate;
    private double yaw;
    private double pitch;
    private double movementSpeed;

    public CameraController(Camera camera, double yaw, double pitch, double movementSpeed) {
        if (camera == null)
            throw new IllegalArgumentException("camera is null");
        if (movementSpeed < 0)
            throw new IllegalArgumentException("movementSpeed is negative");

        this.camera = camera;
        this.yaw = yaw;
        this.pitch = pitch;
        this.movementSpeed = movementSpeed;

        yRotate = new Rotate(yaw, Rotate.Y_AXIS);
        xRotate = new Rotate(pitch, Rotate.X_AXIS);

        camera.getTransforms().addAll(yRotate, xRotate);
    }

    public void rotate(double deltaX, double deltaY) {
        double scaledDeltaX = deltaX * ROTATION_SPEED;
        double scaledDeltaY = deltaY * ROTATION_SPEED;
        yaw = (yaw + scaledDeltaX) % ROTATION_PERIOD_DEGREES;
        yaw = (yaw + ROTATION_PERIOD_DEGREES) % ROTATION_PERIOD_DEGREES;
        pitch = Math.clamp(pitch - scaledDeltaY, -MAX_PITCH_ANGLE_DEGREES, MAX_PITCH_ANGLE_DEGREES);
        yRotate.setAngle(yaw);
        xRotate.setAngle(pitch);
    }

    // move forward/backward
    public void moveForward(double amount) {
        double scaledAmount = amount * movementSpeed;
        double yRad = Math.toRadians(yaw);
        camera.setTranslateX(camera.getTranslateX() + scaledAmount * Math.sin(yRad));
        camera.setTranslateZ(camera.getTranslateZ() + scaledAmount * Math.cos(yRad));
    }

    // move left/right
    public void moveStrafe(double amount) {
        double scaledAmount = amount * movementSpeed;
        double yRad = Math.toRadians(yaw);
        camera.setTranslateX(camera.getTranslateX() + scaledAmount * Math.cos(yRad));
        camera.setTranslateZ(camera.getTranslateZ() - scaledAmount * Math.sin(yRad));
    }

    // move up/down
    public void moveVertical(double amount) {
        double scaledAmount = amount * movementSpeed;
        camera.setTranslateY(camera.getTranslateY() + scaledAmount);
    }

    public void changeMovementSpeed(double amount) {
        movementSpeed *= 1 + Math.signum(amount) * MOVEMENT_FACTOR;
        movementSpeed = Math.clamp(movementSpeed, MIN_SPEED, MAX_SPEED);
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