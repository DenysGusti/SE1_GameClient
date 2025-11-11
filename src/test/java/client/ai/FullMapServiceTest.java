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
    @DisplayName("TALL MAP (10x10): Fort Top-Left, checks side")
    void TallMapWithFortTopLeft_CheckSideOfCoordinates_ReturnsTrueForTopFalseForBottom() {
        var tallMap = new FullMap(Map.of(), new XYPair(0, 0), new XYPair(9, 9),
                null, null, fortTopLeft, null, null);

        assertAll(
                () -> assertTrue(mapService.isOnMySide(tallMap, coordinateTopLeft), "Top-Left coord should be on my side"),
                () -> assertFalse(mapService.isOnMySide(tallMap, coordinateBottomLeft), "Bottom-Left coord should be on enemy side")
        );
    }

    @Test
    @DisplayName("TALL MAP (10x10): Fort Bottom-Left, checks side")
    void TallMapWithFortBottom_CheckSideOfCoordinates_ReturnsFalseForTopTrueForBottom() {
        var tallMap = new FullMap(Map.of(), new XYPair(0, 0), new XYPair(9, 9),
                null, null, fortBottomLeft, null, null);

        assertAll(
                () -> assertFalse(mapService.isOnMySide(tallMap, coordinateTopLeft), "Top-Left coord should be on enemy side"),
                () -> assertTrue(mapService.isOnMySide(tallMap, coordinateBottomLeft), "Bottom-Left coord should be on my side")
        );
    }

    @Test
    @DisplayName("WIDE MAP (20x5): Fort Top-Left, checks side")
    void WideMapWithFortTopLeft_CheckSideOfCoordinates_ReturnsTrueForLeftFalseForRight() {
        var wideMap = new FullMap(Map.of(), new XYPair(0, 0), new XYPair(19, 4),
                null, null, fortTopLeft, null, null);

        assertAll(
                () -> assertTrue(mapService.isOnMySide(wideMap, coordinateTopLeft), "Top-Left coord should be on my side"),
                () -> assertFalse(mapService.isOnMySide(wideMap, coordinateTopRight), "Top-Right coord should be on enemy side")
        );
    }

    @Test
    @DisplayName("WIDE MAP (20x5): Fort Top-Right, checks side")
    void WideMapWithFortTopRight_CheckSideOfCoordinates_ReturnsFalseForLeftTrueForRight() {
        var wideMap = new FullMap(Map.of(), new XYPair(0, 0), new XYPair(19, 4),
                null, null, fortTopRight, null, null);

        assertAll(
                () -> assertFalse(mapService.isOnMySide(wideMap, coordinateTopLeft), "Top-Left coord should be on enemy side"),
                () -> assertTrue(mapService.isOnMySide(wideMap, coordinateTopRight), "Top-Right coord should be on my side")
        );
    }

    @Test
    @DisplayName("Negative Test: isOnMySide throws NullPointerException for null inputs")
    void NullInputs_CheckIsOnMySide_ThrowsNullPointerException() {
        var wideMap = new FullMap(Map.of(), new XYPair(0, 0), new XYPair(19, 4),
                null, null, fortTopLeft, null, null);
        var mapNoFort = new FullMap(Map.of(), new XYPair(0, 0), new XYPair(19, 4),
                null, null, null, null, null);
        var mapNoBounds = new FullMap(Map.of(), null, null,
                null, null, fortTopLeft, null, null);

        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> mapService.isOnMySide(null, coordinateTopLeft), "Map cannot be null"),
                () -> assertThrows(NullPointerException.class,
                        () -> mapService.isOnMySide(wideMap, null), "Coordinate cannot be null"),
                () -> assertThrows(NullPointerException.class,
                        () -> mapService.isOnMySide(mapNoFort, coordinateTopLeft), "Fort position cannot be null"),
                () -> assertThrows(NullPointerException.class,
                        () -> mapService.isOnMySide(mapNoBounds, coordinateTopLeft), "BottomRight coordinate cannot be null")
        );
    }
}