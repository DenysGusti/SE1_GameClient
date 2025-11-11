package client.ai;

import client.data.XYPair;
import client.data.fromserver.FullMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FullMapServiceTest {

    private FullMapService mapService;

    // Test coordinates
    private final XYPair coordinateTopLeft = new XYPair(2, 2);
    private final XYPair coordinateBottomLeft = new XYPair(2, 8);
    private final XYPair coordinateTopRight = new XYPair(18, 2);

    // Fort positions
    private final XYPair fortTopLeft = new XYPair(1, 1);
    private final XYPair fortBottomLeft = new XYPair(1, 8);
    private final XYPair fortTopRight = new XYPair(15, 1);

    @BeforeEach
    void setUp() {
        mapService = new FullMapService();
    }

    @Test
    @DisplayName("TALL MAP (10x10): Fort Top-Left, should correctly identify sides")
    void isOnMySide_TallMap_FortTopLeft() {
        // Simulates a 10x10 map (seam is horizontal at y=5)
        var tallMap = new FullMap(Map.of(), new XYPair(0, 0), new XYPair(9, 9),
                null, null, fortTopLeft, null, null);

        assertTrue(mapService.isOnMySide(tallMap, coordinateTopLeft)); // (2,2) is on my side
        assertFalse(mapService.isOnMySide(tallMap, coordinateBottomLeft)); // (2,8) is on enemy side
    }

    @Test
    @DisplayName("TALL MAP (10x10): Fort Bottom-Left, should correctly identify sides")
    void isOnMySide_TallMap_FortBottom() {
        var tallMap = new FullMap(Map.of(), new XYPair(0, 0), new XYPair(9, 9),
                null, null, fortBottomLeft, null, null);

        assertFalse(mapService.isOnMySide(tallMap, coordinateTopLeft)); // (2,2) is on enemy side
        assertTrue(mapService.isOnMySide(tallMap, coordinateBottomLeft)); // (2,8) is on my side
    }

    @Test
    @DisplayName("WIDE MAP (20x5): Fort Top-Left, should correctly identify sides")
    void isOnMySide_WideMap_FortTopLeft() {
        // Simulates a 20x5 map (seam is vertical at x=10)
        var wideMap = new FullMap(Map.of(), new XYPair(0, 0), new XYPair(19, 4),
                null, null, fortTopLeft, null, null);

        assertTrue(mapService.isOnMySide(wideMap, coordinateTopLeft)); // (2,2) is on my side
        assertFalse(mapService.isOnMySide(wideMap, coordinateTopRight)); // (18,2) is on enemy side
    }

    @Test
    @DisplayName("WIDE MAP (20x5): Fort Top-Right, should correctly identify sides")
    void isOnMySide_WideMap_FortTopRight() {
        var wideMap = new FullMap(Map.of(), new XYPair(0, 0), new XYPair(19, 4),
                null, null, fortTopRight, null, null);

        assertFalse(mapService.isOnMySide(wideMap, coordinateTopLeft)); // (2,2) is on enemy side
        assertTrue(mapService.isOnMySide(wideMap, coordinateTopRight)); // (18,2) is on my side
    }

    @Test
    @DisplayName("isOnMySide should throw NullPointerException for null inputs")
    void isOnMySide_NullChecks() {
        var wideMap = new FullMap(Map.of(), new XYPair(0, 0), new XYPair(19, 4),
                null, null, fortTopLeft, null, null);
        var mapNoFort = new FullMap(Map.of(), new XYPair(0, 0), new XYPair(19, 4),
                null, null, null, null, null);
        var mapNoBounds = new FullMap(Map.of(), null, null,
                null, null, fortTopLeft, null, null);

        assertThrows(NullPointerException.class, () -> mapService.isOnMySide(null, coordinateTopLeft));
        assertThrows(NullPointerException.class, () -> mapService.isOnMySide(wideMap, null));
        assertThrows(NullPointerException.class, () -> mapService.isOnMySide(mapNoFort, coordinateTopLeft));
        assertThrows(NullPointerException.class, () -> mapService.isOnMySide(mapNoBounds, coordinateTopLeft));
    }
}