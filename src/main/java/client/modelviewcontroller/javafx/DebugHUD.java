package client.modelviewcontroller.javafx;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DebugHUD {
    private static final Logger logger = LoggerFactory.getLogger(DebugHUD.class);

    private static final String LABEL_STYLE = "-fx-background-color: rgba(0, 0, 0, 0.4); -fx-padding: 15; -fx-font-family: 'Consolas';";

    private final Label label = new Label();
    private final VBox container = new VBox();

    public DebugHUD() {
        label.setTextFill(Color.LIME);
        label.setStyle(LABEL_STYLE);
        container.getChildren().add(label);
    }

    public void update(CameraController cameraController) {
        double yaw = cameraController.getYaw();
        double pitch = cameraController.getPitch();

        String zDir = (yaw <= 45 || yaw > 315) ? "+Z (forward)" : (yaw > 135 && yaw <= 225 ? "-Z (backward)" : "");
        String xDir = (yaw > 45 && yaw <= 135) ? "+X (right)" : (yaw > 225 && yaw <= 315 ? "-X (left)" : "");
        String yDir = pitch > 0 ? "-Y (up)" : "+Y (down)";

        String lookingAt = Stream.of(zDir, xDir, yDir).filter(s -> !s.isEmpty()).collect(Collectors.joining(" "));

        label.setText(String.format(Locale.ROOT,
                """
                        Position:   [X:%5.1f Y:%5.1f Z:%5.1f]
                        Looking at: [yaw:%5.1f pitch:%5.1f] %s
                        Controls:   WASD-move, Space-up, LShift-down, Esc-lock/unlock mouse
                        """,
                cameraController.getX(), cameraController.getY(), cameraController.getZ(), yaw, pitch, lookingAt));
    }

    public VBox getNode() {
        return container;
    }
}