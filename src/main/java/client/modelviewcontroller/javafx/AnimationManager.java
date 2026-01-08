package client.modelviewcontroller.javafx;

import client.data.XYPair;
import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnimationManager {
    private static final Logger logger = LoggerFactory.getLogger(AnimationManager.class);

    private static final double EPS = 1e-2;

    private static final double PLAYER_MOVE_S = 0.3;
    private static final double TREASURE_ROTATE_S = 6;
    private static final double TREASURE_FLOAT_S = 1.5;
    private static final double FLOATING_TREASURE_DELTA_Y = -0.1875;

    public void movePlayer(Node playerModel, XYPair targetCoordinate, double xOffset, double targetY) {
        if (playerModel == null)
            throw new IllegalArgumentException("playerModel is null");
        if (targetCoordinate == null)
            throw new IllegalArgumentException("targetCoordinate is null");

        double targetX = targetCoordinate.y();
        double targetZ = targetCoordinate.x();

        var translateTransition = new TranslateTransition(Duration.seconds(PLAYER_MOVE_S), playerModel);
        translateTransition.setToX(targetX + xOffset);
        translateTransition.setToY(targetY);
        translateTransition.setToZ(targetZ);

        double deltaX = targetX - Math.round(playerModel.getTranslateX());
        double deltaZ = targetZ - Math.round(playerModel.getTranslateZ());

        if (Math.abs(deltaX) < EPS && Math.abs(deltaZ) < EPS)
            translateTransition.play();
        else {
            var rotateTransition = getRotateTransition(playerModel, deltaZ, deltaX);
            var parallelTransition = new ParallelTransition(translateTransition, rotateTransition);
            parallelTransition.play();
        }
    }

    private static RotateTransition getRotateTransition(Node playerModel, double deltaZ, double deltaX) {
        if (playerModel == null)
            throw new IllegalArgumentException("playerModel is null");

        double targetAngle = -Math.toDegrees(Math.atan2(deltaZ, deltaX));
        double currentAngle = playerModel.getRotate();

        double deltaAngle = (targetAngle - currentAngle) % 360;
        if (deltaAngle > 180)
            deltaAngle -= 360;
        if (deltaAngle < -180)
            deltaAngle += 360;

        double finalAngle = currentAngle + deltaAngle;

        var rotateTransition = new RotateTransition(Duration.seconds(PLAYER_MOVE_S), playerModel);
        rotateTransition.setAxis(Rotate.Y_AXIS);
        rotateTransition.setToAngle(finalAngle);
        return rotateTransition;
    }

    public Animation createTreasureAnimation(Node treasureModel) {
        if (treasureModel == null)
            throw new IllegalArgumentException("treasureModel is null");

        var rotateTransition = new RotateTransition(Duration.seconds(TREASURE_ROTATE_S), treasureModel);
        rotateTransition.setAxis(Rotate.Y_AXIS);
        rotateTransition.setByAngle(360);
        rotateTransition.setInterpolator(Interpolator.LINEAR);
        rotateTransition.setCycleCount(Animation.INDEFINITE);

        var translateTransition = new TranslateTransition(Duration.seconds(TREASURE_FLOAT_S), treasureModel);
        translateTransition.setByY(FLOATING_TREASURE_DELTA_Y);
        translateTransition.setCycleCount(Animation.INDEFINITE);
        translateTransition.setAutoReverse(true);
        translateTransition.setInterpolator(Interpolator.EASE_BOTH);

        return new ParallelTransition(treasureModel, rotateTransition, translateTransition);
    }
}
