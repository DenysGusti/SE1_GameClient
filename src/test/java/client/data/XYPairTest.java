package client.data;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
    @DisplayName("Should return 4 adjacent neighbors for a middle coordinate")
    void getAdjacentNeighbors_middle() {
        Set<XYPair> neighbors = new XYPair(5, 5).getAdjacentNeighbors(gridSize);
        assertThat(neighbors, hasSize(4));
        assertThat(neighbors, containsInAnyOrder(new XYPair(4, 5), new XYPair(6, 5), new XYPair(5, 4), new XYPair(5, 6)));
    }

    @Test
    @DisplayName("Should return 2 adjacent neighbors for a top-left corner")
    void getAdjacentNeighbors_corner() {
        Set<XYPair> neighbors = new XYPair(0, 0).getAdjacentNeighbors(gridSize);
        assertThat(neighbors, hasSize(2));
        assertThat(neighbors, containsInAnyOrder(new XYPair(1, 0), new XYPair(0, 1)));
    }

    @Test
    @DisplayName("Should return 3 adjacent neighbors for a side edge")
    void getAdjacentNeighbors_edge() {
        Set<XYPair> neighbors = new XYPair(9, 5).getAdjacentNeighbors(gridSize);
        assertThat(neighbors, hasSize(3));
        assertThat(neighbors, containsInAnyOrder(new XYPair(8, 5), new XYPair(9, 4), new XYPair(9, 6)));
    }

    @Test
    @DisplayName("Should return 8 total neighbors for a middle coordinate")
    void getAllNeighbors_middle() {
        Set<XYPair> neighbors = new XYPair(5, 5).getAllNeighbors(gridSize);
        assertThat(neighbors, hasSize(8));
    }

    @Test
    @DisplayName("Should return 9 neighbors (all + self) for a middle coordinate")
    void getAllNeighborsWithThis_middle() {
        var middle = new XYPair(5, 5);
        Set<XYPair> neighbors = middle.getAllNeighborsWithThis(gridSize);
        assertThat(neighbors, hasSize(9));
        assertThat(neighbors, hasItem(middle));
    }

    @Test
    @DisplayName("isOnBorder should return true for border coordinates")
    void isOnBorder_true() {
        assertTrue(new XYPair(0, 5).isOnBorder(gridSize));
    }

    @Test
    @DisplayName("isOnBorder should return false for internal coordinates")
    void isOnBorder_false() {
        assertFalse(new XYPair(5, 5).isOnBorder(gridSize));
    }
}