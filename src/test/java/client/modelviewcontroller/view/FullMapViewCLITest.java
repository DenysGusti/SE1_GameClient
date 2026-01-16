package client.modelviewcontroller.view;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import client.data.ETerrain;
import client.data.XYPair;
import client.data.fromserver.FullMap;
import client.data.fromserver.FullMapNode;
import org.mockito.ArgumentMatchers;

class FullMapViewCLITest {
    private FullMapViewCLI view;
    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @BeforeEach
    void setUp() {
        view = new FullMapViewCLI();
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @AfterEach
    void tearDown() {
        System.setOut(standardOut);
    }

    @Test
    void Update_NullFullMap_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> view.update(null));
    }

    @Test
    void Update_ComplexMap_CoversAllBorderAndCenterBranches() {
        var fullMap = mock(FullMap.class);

        var topLeft = new XYPair(10, 10);
        when(fullMap.size()).thenReturn(new XYPair(2, 2));
        when(fullMap.getOptionalTopLeftCoordinate()).thenReturn(Optional.of(topLeft));

        var coordinateMyFort = new XYPair(10, 10);
        var coordinateEnemyFort = new XYPair(11, 10);
        var coordinateTreasure = new XYPair(10, 11);
        var coordinateCombat = new XYPair(11, 11);

        Map<XYPair, FullMapNode> nodes = new HashMap<>();
        nodes.put(coordinateMyFort, new FullMapNode(ETerrain.Grass, true));
        nodes.put(coordinateEnemyFort, new FullMapNode(ETerrain.Mountain, false));
        nodes.put(coordinateTreasure, new FullMapNode(ETerrain.Water, true));
        nodes.put(coordinateCombat, new FullMapNode(ETerrain.Grass, true));

        when(fullMap.nodes()).thenReturn(nodes);
        when(fullMap.getTerrain(ArgumentMatchers.any(XYPair.class))).thenAnswer(inv -> nodes.get(inv.getArgument(0)).terrain());
        when(fullMap.isRevealed(ArgumentMatchers.any(XYPair.class))).thenAnswer(inv -> nodes.get(inv.getArgument(0)).isRevealed());

        when(fullMap.getOptionalMyFortPosition()).thenReturn(Optional.of(coordinateMyFort));
        when(fullMap.getOptionalEnemyFortPosition()).thenReturn(Optional.of(coordinateEnemyFort));
        when(fullMap.getOptionalMyTreasurePosition()).thenReturn(Optional.of(coordinateTreasure));

        when(fullMap.isMyTreasureCollected()).thenReturn(false);

        when(fullMap.getOptionalMyPlayerPosition()).thenReturn(Optional.of(coordinateCombat));
        when(fullMap.getOptionalEnemyPlayerPosition()).thenReturn(Optional.of(coordinateCombat));

        view.update(fullMap);

        String output = outputStreamCaptor.toString();

        assertThat(output, containsString("🏰"));
        assertThat(output, containsString("🏯"));
        assertThat(output, containsString("🪙"));

        assertThat(output, containsString("☁️"));
        assertThat(output, containsString("💰"));
        assertThat(output, containsString("⚔️"));
    }

    @Test
    void Update_SeparatePlayersAndCollectedTreasure_CoversIsolatedPlayerAndTreasureBranches() {
        var fullMap = mock(FullMap.class);
        var p1Coordinate = new XYPair(0, 0);
        var p2Coordinate = new XYPair(1, 0);
        var treasureOnlyCoordinate = new XYPair(0, 1);

        when(fullMap.size()).thenReturn(new XYPair(2, 2));
        when(fullMap.getOptionalTopLeftCoordinate()).thenReturn(Optional.empty());

        Map<XYPair, FullMapNode> nodes = new HashMap<>();
        nodes.put(p1Coordinate, new FullMapNode(ETerrain.Grass, true));
        nodes.put(p2Coordinate, new FullMapNode(ETerrain.Grass, true));
        nodes.put(treasureOnlyCoordinate, new FullMapNode(ETerrain.Grass, true));

        when(fullMap.nodes()).thenReturn(nodes);
        when(fullMap.getTerrain(any())).thenReturn(ETerrain.Grass);
        when(fullMap.isRevealed(any())).thenReturn(true);

        when(fullMap.getOptionalMyPlayerPosition()).thenReturn(Optional.of(p1Coordinate));
        when(fullMap.getOptionalEnemyPlayerPosition()).thenReturn(Optional.of(p2Coordinate));

        when(fullMap.getOptionalMyTreasurePosition()).thenReturn(Optional.of(treasureOnlyCoordinate));
        when(fullMap.isMyTreasureCollected()).thenReturn(true);

        view.update(fullMap);

        String output = outputStreamCaptor.toString();
        assertThat(output, containsString("😇"));
        assertThat(output, containsString("😈"));

        assertThat(output, containsString("🪙"));
        assertThat(output, not(containsString("💰")));
    }
}