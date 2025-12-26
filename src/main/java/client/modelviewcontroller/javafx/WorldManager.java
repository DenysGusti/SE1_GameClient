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

    public WorldManager(Group worldRoot) {
        if (worldRoot == null)
            throw new IllegalArgumentException("worldRoot is null");

        Box xAxis = createAxis(AXIS_LENGTH, AXIS_THICKNESS, AXIS_THICKNESS, Color.RED);
        Box yAxis = createAxis(AXIS_THICKNESS, AXIS_LENGTH, AXIS_THICKNESS, Color.GREEN);
        Box zAxis = createAxis(AXIS_THICKNESS, AXIS_THICKNESS, AXIS_LENGTH, Color.BLUE);

        var ambientLight = new AmbientLight(Color.color(0.3, 0.3, 0.3));
        var pointLight = new PointLight(Color.WHITE);
        pointLight.setTranslateY(-50);
        pointLight.setTranslateX(5);
        pointLight.setTranslateZ(10);

        worldRoot.getChildren().addAll(xAxis, yAxis, zAxis, ambientLight, pointLight);
    }

    private static Box createAxis(double width, double height, double depth, Color color) {
        var box = new Box(width, height, depth);

        var phongMaterial = new PhongMaterial(color);
        phongMaterial.setSpecularColor(Color.WHITE);
        phongMaterial.setSpecularPower(32);
        box.setMaterial(phongMaterial);

        return box;
    }
}