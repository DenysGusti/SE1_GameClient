package client.javafx;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import client.data.XYPair;
import javafx.animation.*;
import javafx.scene.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AnimationManagerTest {
    private AnimationManager animationManager;
    private Node nodeMock;

    @BeforeEach
    void setUp() {
        animationManager = new AnimationManager();
        nodeMock = mock(Node.class);
    }

    @Test
    void CreatePlayerAnimation_NullPlayerModel_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                animationManager.createPlayerAnimation(null, new XYPair(0, 0), 0., 0.));
    }

    @Test
    void CreatePlayerAnimation_NullTargetCoordinate_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                animationManager.createPlayerAnimation(nodeMock, null, 0., 0.));
    }

    @Test
    void CreateTreasureAnimation_NullTreasureModel_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                animationManager.createTreasureAnimation(null));
    }

    @Test
    void CreatePlayerAnimation_AlreadyAtTarget_ReturnsOnlyTranslateTransition() {
        // (10, 5) -> targetX=5, targetZ=10
        when(nodeMock.getTranslateX()).thenReturn(5.);
        when(nodeMock.getTranslateZ()).thenReturn(10.);

        Transition result = animationManager.createPlayerAnimation(nodeMock, new XYPair(10, 5), 0., 0.);

        assertThat(result, instanceOf(TranslateTransition.class));
    }

    @Test
    void CreatePlayerAnimation_NeedsMovement_ReturnsParallelTransition() {
        when(nodeMock.getTranslateX()).thenReturn(0.);
        when(nodeMock.getTranslateZ()).thenReturn(0.);

        Transition result = animationManager.createPlayerAnimation(nodeMock, new XYPair(10, 10), 0., 0.);

        assertThat(result, instanceOf(ParallelTransition.class));
    }

    @Test
    void CreatePlayerAnimation_AngleNormalization_PositiveWrap() {
        // targetAngle = -90. currentAngle = -300
        // delta = 210 -> > 180 -> delta becomes -150
        // finalAngle = -300 + (-150) = -450
        when(nodeMock.getRotate()).thenReturn(-300.);
        when(nodeMock.getTranslateX()).thenReturn(0.);
        when(nodeMock.getTranslateZ()).thenReturn(0.);

        var result = (ParallelTransition) animationManager.createPlayerAnimation(nodeMock, new XYPair(1, 0), 0., 0.);
        var rot = (RotateTransition) result.getChildren().stream()
                .filter(c -> c instanceof RotateTransition).findFirst().orElseThrow();

        assertThat(rot.getToAngle(), is(-450.));
    }

    @Test
    void CreatePlayerAnimation_AngleNormalization_NegativeWrap() {
        // targetAngle = 90. currentAngle = 300
        // delta = -210 -> < -180 -> delta becomes 150
        // finalAngle = 300 + 150 = 450
        when(nodeMock.getRotate()).thenReturn(300.);
        when(nodeMock.getTranslateX()).thenReturn(0.);
        when(nodeMock.getTranslateZ()).thenReturn(0.);

        var result = (ParallelTransition) animationManager.createPlayerAnimation(nodeMock, new XYPair(-1, 0), 0., 0.);
        var rot = (RotateTransition) result.getChildren().stream()
                .filter(c -> c instanceof RotateTransition).findFirst().orElseThrow();

        assertThat(rot.getToAngle(), is(450.));
    }

    @Test
    void CreatePlayerAnimation_SmallAngle_NoWrap() {
        // targetAngle = 0, currentAngle = 10, delta = -10
        when(nodeMock.getRotate()).thenReturn(10.);
        when(nodeMock.getTranslateX()).thenReturn(0.);
        when(nodeMock.getTranslateZ()).thenReturn(0.);

        var result = (ParallelTransition) animationManager.createPlayerAnimation(nodeMock, new XYPair(0, 1), 0., 0.);
        var rot = (RotateTransition) result.getChildren().stream()
                .filter(c -> c instanceof RotateTransition).findFirst().orElseThrow();

        assertThat(rot.getToAngle(), is(0.));
    }

    @Test
    void CreateTreasureAnimation_ValidModel_ReturnsParallelTransition() {
        Animation result = animationManager.createTreasureAnimation(nodeMock);

        assertThat(result, notNullValue());
        assertThat(result, instanceOf(ParallelTransition.class));
    }
}