package client.data;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class XYPairTest {
    private static final XYPair FULL_MAP_SIZE = new XYPair(20, 5);
    private static final XYPair[] CORNER_COORDINATES = new XYPair[]{
            new XYPair(0, 0), new XYPair(19, 0), new XYPair(19, 4), new XYPair(0, 4)
    };
    private static final XYPair[] BORDER_COORDINATES = new XYPair[]{
            new XYPair(1, 0), new XYPair(19, 1), new XYPair(18, 4), new XYPair(0, 3)
    };
    private static final XYPair CENTRAL_COORDINATE = new XYPair(10, 2);

    @ParameterizedTest
    @MethodSource("provideBorderScenarios")
    public void PointAndGridSize_IsOnBorder_ReturnsCorrectBoolean(XYPair point, boolean expected) {
        assertThat(point.isOnBorder(FULL_MAP_SIZE), is(expected));
    }

    private static Stream<Arguments> provideBorderScenarios() {
        return Stream.of(
                Arguments.of(BORDER_COORDINATES[0], true),
                Arguments.of(BORDER_COORDINATES[1], true),
                Arguments.of(BORDER_COORDINATES[2], true),
                Arguments.of(BORDER_COORDINATES[3], true),
                Arguments.of(CORNER_COORDINATES[0], true),
                Arguments.of(CORNER_COORDINATES[1], true),
                Arguments.of(CORNER_COORDINATES[2], true),
                Arguments.of(CORNER_COORDINATES[3], true),
                Arguments.of(CENTRAL_COORDINATE, false)
        );
    }

    @ParameterizedTest
    @MethodSource("provideCornerScenarios")
    public void PointAndGridSize_IsOnCorner_ReturnsCorrectBoolean(XYPair point, boolean expected) {
        assertThat(point.isOnCorner(FULL_MAP_SIZE), is(expected));
    }

    private static Stream<Arguments> provideCornerScenarios() {
        return Stream.of(
                Arguments.of(BORDER_COORDINATES[0], false),
                Arguments.of(BORDER_COORDINATES[1], false),
                Arguments.of(BORDER_COORDINATES[2], false),
                Arguments.of(BORDER_COORDINATES[3], false),
                Arguments.of(CORNER_COORDINATES[0], true),
                Arguments.of(CORNER_COORDINATES[1], true),
                Arguments.of(CORNER_COORDINATES[2], true),
                Arguments.of(CORNER_COORDINATES[3], true),
                Arguments.of(CENTRAL_COORDINATE, false)
        );
    }

    @ParameterizedTest
    @MethodSource("provideAdjacentScenarios")
    public void Point_GetAdjacentNeighbors_ReturnsCorrectCount(XYPair point, int expectedCount) {
        assertThat(point.getAdjacentNeighbors(FULL_MAP_SIZE), hasSize(expectedCount));
    }

    private static Stream<Arguments> provideAdjacentScenarios() {
        return Stream.of(
                Arguments.of(BORDER_COORDINATES[0], 3),
                Arguments.of(BORDER_COORDINATES[1], 3),
                Arguments.of(BORDER_COORDINATES[2], 3),
                Arguments.of(BORDER_COORDINATES[3], 3),
                Arguments.of(CORNER_COORDINATES[0], 2),
                Arguments.of(CORNER_COORDINATES[1], 2),
                Arguments.of(CORNER_COORDINATES[2], 2),
                Arguments.of(CORNER_COORDINATES[3], 2),
                Arguments.of(CENTRAL_COORDINATE, 4)
        );
    }

    @ParameterizedTest
    @MethodSource("provideDiagonalScenarios")
    public void Point_GetDiagonalNeighbors_ReturnsCorrectCount(XYPair point, int expectedCount) {
        assertThat(point.getDiagonalNeighbors(FULL_MAP_SIZE), hasSize(expectedCount));
    }

    private static Stream<Arguments> provideDiagonalScenarios() {
        return Stream.of(
                Arguments.of(BORDER_COORDINATES[0], 2),
                Arguments.of(BORDER_COORDINATES[1], 2),
                Arguments.of(BORDER_COORDINATES[2], 2),
                Arguments.of(BORDER_COORDINATES[3], 2),
                Arguments.of(CORNER_COORDINATES[0], 1),
                Arguments.of(CORNER_COORDINATES[1], 1),
                Arguments.of(CORNER_COORDINATES[2], 1),
                Arguments.of(CORNER_COORDINATES[3], 1),
                Arguments.of(CENTRAL_COORDINATE, 4)
        );
    }

    @Test
    public void CentralPoint_GetAllNeighbors_ReturnsEightPoints() {
        assertThat(CENTRAL_COORDINATE.getAllNeighbors(FULL_MAP_SIZE), hasSize(8));
    }

    @Test
    public void CentralPoint_GetAllNeighborsWithThis_ReturnsNinePointsIncludingSelf() {
        List<XYPair> result = CENTRAL_COORDINATE.getAllNeighborsWithThis(FULL_MAP_SIZE);
        assertThat(result, hasSize(9));
        assertThat(result, hasItem(CENTRAL_COORDINATE));
    }

    @Test
    public void NullGridSize_GetAdjacentNeighbors_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> CENTRAL_COORDINATE.getAdjacentNeighbors(null));
    }

    @Test
    public void NullGridSize_GetDiagonalNeighbors_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> CENTRAL_COORDINATE.getDiagonalNeighbors(null));
    }

    @Test
    public void NullGridSize_GetAllNeighbors_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> CENTRAL_COORDINATE.getAllNeighbors(null));
    }

    @Test
    public void NullGridSize_GetAllNeighborsWithThis_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> CENTRAL_COORDINATE.getAllNeighborsWithThis(null));
    }

    @Test
    public void NullGridSize_IsOnBorder_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> CENTRAL_COORDINATE.isOnBorder(null));
    }

    @Test
    public void NullGridSize_IsOnCorner_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> CENTRAL_COORDINATE.isOnCorner(null));
    }
}