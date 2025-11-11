package client.data;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

class XYPairTest {

    private static XYPair gridSize;

    @BeforeAll
    static void setUp() {
        gridSize = new XYPair(10, 10);
    }

    @Test
    @DisplayName("A coordinate in the middle of the grid")
    void MiddleCoordinate_GetAdjacentNeighbors_ReturnsFourNeighbors() {
        Set<XYPair> neighbors = new XYPair(5, 5).getAdjacentNeighbors(gridSize);
        assertAll(
                () -> assertThat(neighbors, hasSize(4)),
                () -> assertThat(neighbors, containsInAnyOrder(new XYPair(4, 5), new XYPair(6, 5), new XYPair(5, 4), new XYPair(5, 6)))
        );
    }

    @Test
    @DisplayName("A coordinate in the corner of the grid")
    void CornerCoordinate_GetAdjacentNeighbors_ReturnsTwoNeighbors() {
        Set<XYPair> neighbors = new XYPair(0, 0).getAdjacentNeighbors(gridSize);
        assertAll(
                () -> assertThat(neighbors, hasSize(2)),
                () -> assertThat(neighbors, containsInAnyOrder(new XYPair(1, 0), new XYPair(0, 1)))
        );
    }

    @Test
    @DisplayName("A coordinate on the edge of the grid")
    void EdgeCoordinate_GetAdjacentNeighbors_ReturnsThreeNeighbors() {
        Set<XYPair> neighbors = new XYPair(9, 5).getAdjacentNeighbors(gridSize);
        assertAll(
                () -> assertThat(neighbors, hasSize(3)),
                () -> assertThat(neighbors, containsInAnyOrder(new XYPair(8, 5), new XYPair(9, 4), new XYPair(9, 6)))
        );
    }

    @Test
    @DisplayName("A coordinate in the middle of the grid")
    void MiddleCoordinate_GetAllNeighbors_ReturnsEightNeighbors() {
        Set<XYPair> neighbors = new XYPair(5, 5).getAllNeighbors(gridSize);
        assertThat(neighbors, hasSize(8));
    }

    @Test
    @DisplayName("A coordinate in the middle of the grid")
    void MiddleCoordinate_GetAllNeighborsWithThis_ReturnsNineNeighborsIncludingSelf() {
        var middle = new XYPair(5, 5);
        Set<XYPair> neighbors = middle.getAllNeighborsWithThis(gridSize);
        assertAll(
                () -> assertThat(neighbors, hasSize(9)),
                () -> assertThat(neighbors, hasItem(middle))
        );
    }

    @Test
    @DisplayName("A coordinate on the border")
    void BorderCoordinate_IsOnBorder_ReturnsTrue() {
        assertTrue(new XYPair(0, 5).isOnBorder(gridSize));
    }

    @Test
    @DisplayName("A coordinate not on the border")
    void InternalCoordinate_IsOnBorder_ReturnsFalse() {
        assertFalse(new XYPair(5, 5).isOnBorder(gridSize));
    }

    @DisplayName("Data-Driven Test: Checks various coordinates for corner status")
    @ParameterizedTest(name = "Coord ({0},{1})_IsOnCorner_Returns {2}")
    @CsvSource({
            "0, 0, true",   // Top-left
            "9, 0, true",   // Top-right
            "0, 9, true",   // Bottom-left
            "9, 9, true",   // Bottom-right
            "5, 0, false",  // Top-edge (not a corner)
            "5, 5, false"   // Middle (not a corner)
    })
    void VariousCoordinates_IsOnCorner_ReturnsCorrectBoolean(int x, int y, boolean expected) {
        var coordinate = new XYPair(x, y);
        assertEquals(expected, coordinate.isOnCorner(gridSize));
    }
}