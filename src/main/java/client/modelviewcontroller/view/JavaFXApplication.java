package client.modelviewcontroller.view;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.robot.Robot;
import javafx.stage.Stage;

import java.util.HashSet;
import java.util.Set;

public class JavaFXApplication extends Application {
    private final static WorldManager WORLD_MANAGER = new WorldManager();

    private final static double WINDOW_WIDTH = 1200;
    private final static double WINDOW_HEIGHT = 800;

    private final static double CAMERA_NEAR_CLIP = 0.1;
    private final static double CAMERA_FAR_CLIP = 1000;

    private final static double INITIAL_CAMERA_X = 20;
    private final static double INITIAL_CAMERA_Y = -15;
    private final static double INITIAL_CAMERA_Z = -10;

    private final static double INITIAL_CAMERA_YAW = 315;
    private final static double INITIAL_CAMERA_PITCH = -35;

    private final static double MOVEMENT_SPEED = 0.2;

    private final Set<KeyCode> activeKeys = new HashSet<>();
    private boolean mouseLocked = false;
    private boolean isCentering = false;

    public static Group getWorldGroup() {
        return WORLD_MANAGER.getWorldRoot();
    }

    @Override
    public void start(Stage stage) {
        var camera = new PerspectiveCamera(false);

        camera.setNearClip(CAMERA_NEAR_CLIP);
        camera.setFarClip(CAMERA_FAR_CLIP);

        camera.setTranslateX(INITIAL_CAMERA_X);
        camera.setTranslateY(INITIAL_CAMERA_Y);
        camera.setTranslateZ(INITIAL_CAMERA_Z);

        var cameraController = new CameraController(camera, INITIAL_CAMERA_YAW, INITIAL_CAMERA_PITCH);

        var subScene = new SubScene(WORLD_MANAGER.getWorldRoot(), WINDOW_WIDTH, WINDOW_HEIGHT, true, SceneAntialiasing.BALANCED);
        subScene.setCamera(camera);
        subScene.setFill(Color.BLACK);

        var debugHUD = new DebugHUD();
        var rootStackPane = new StackPane(subScene, debugHUD.getNode());
        var scene = new Scene(rootStackPane, WINDOW_WIDTH, WINDOW_HEIGHT);

        var robot = new Robot();
        setupInput(scene, stage, cameraController, robot);

        var animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (mouseLocked) {
                    handleMovement(cameraController);
                    debugHUD.update(cameraController);
                }
            }
        };

        animationTimer.start();

        stage.setTitle("SE1 Game Client");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    private void handleMovement(CameraController cameraController) {
        if (activeKeys.contains(KeyCode.W))
            cameraController.moveForward(MOVEMENT_SPEED);
        if (activeKeys.contains(KeyCode.S))
            cameraController.moveForward(-MOVEMENT_SPEED);
        if (activeKeys.contains(KeyCode.A))
            cameraController.moveStrafe(-MOVEMENT_SPEED);
        if (activeKeys.contains(KeyCode.D))
            cameraController.moveStrafe(MOVEMENT_SPEED);
        if (activeKeys.contains(KeyCode.SPACE))
            cameraController.moveVertical(-MOVEMENT_SPEED);
        if (activeKeys.contains(KeyCode.SHIFT))
            cameraController.moveVertical(MOVEMENT_SPEED);
    }

    private void setupInput(Scene scene, Stage stage, CameraController cameraController, Robot robot) {
        scene.setOnMouseMoved(mouseEvent -> {
            if (!mouseLocked || isCentering) {
                isCentering = false;
                return;
            }

            double centerX = scene.getWidth() * 0.5;
            double centerY = scene.getHeight() * 0.5;

            double dx = mouseEvent.getSceneX() - centerX;
            double dy = mouseEvent.getSceneY() - centerY;

            cameraController.rotate(dx, dy);

            isCentering = true;
            robot.mouseMove(stage.getX() + scene.getX() + centerX, stage.getY() + scene.getY() + centerY);
        });

        scene.setOnKeyPressed(keyEvent -> {
            activeKeys.add(keyEvent.getCode());

            if (keyEvent.getCode() == KeyCode.ESCAPE) {
                mouseLocked = !mouseLocked;
                scene.setCursor(mouseLocked ? Cursor.NONE : Cursor.DEFAULT);
            }
        });

        scene.setOnKeyReleased(keyEvent -> activeKeys.remove(keyEvent.getCode()));
    }
}