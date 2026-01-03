package client.modelviewcontroller.javafx;

import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorldManager {
    private static final Logger logger = LoggerFactory.getLogger(WorldManager.class);

    private static final double AXIS_LENGTH = 100;
    private static final double AXIS_THICKNESS = 0.05;

    private final static double INITIAL_POINT_LIGHT_X = 20;
    private final static double INITIAL_POINT_LIGHT_Y = -15;
    private final static double INITIAL_POINT_LIGHT_Z = -10;

    public WorldManager(Group worldRoot) {
        if (worldRoot == null)
            throw new IllegalArgumentException("worldRoot is null");

        Box xAxis = createAxis(AXIS_LENGTH, AXIS_THICKNESS, AXIS_THICKNESS, Color.RED);
        Box yAxis = createAxis(AXIS_THICKNESS, AXIS_LENGTH, AXIS_THICKNESS, Color.GREEN);
        Box zAxis = createAxis(AXIS_THICKNESS, AXIS_THICKNESS, AXIS_LENGTH, Color.BLUE);

        var ambientLight = new AmbientLight(Color.color(0.2, 0.2, 0.2));
        var pointLight = new PointLight(Color.WHITE);
        pointLight.setTranslateX(INITIAL_POINT_LIGHT_X);
        pointLight.setTranslateY(INITIAL_POINT_LIGHT_Y);
        pointLight.setTranslateZ(INITIAL_POINT_LIGHT_Z);

        worldRoot.getChildren().addAll(xAxis, yAxis, zAxis, ambientLight, pointLight);
    }

    private static Box createAxis(double width, double height, double depth, Color color) {
        var box = new Box(width, height, depth);

        var phongMaterial = new PhongMaterial(color);
        box.setMaterial(phongMaterial);

        return box;
    }
}