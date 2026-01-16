package client.modelviewcontroller.view;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import client.data.ETerrain;
import client.data.XYPair;
import client.data.fromclient.HalfMap;

class HalfMapViewTest {
    private HalfMapView halfMapView;
    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @BeforeEach
    void setUp() {
        halfMapView = new HalfMapView();
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @AfterEach
    void tearDown() {
        System.setOut(standardOut);
    }

    @Test
    void Update_NullHalfMap_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> halfMapView.update(null));
    }

    @Test
    void Update_ValidHalfMap_CoversFortAndStandardTerrain() {
        Map<XYPair, ETerrain> nodes = new HashMap<>();

        nodes.put(new XYPair(0, 0), ETerrain.Grass);
        nodes.put(new XYPair(1, 0), ETerrain.Mountain);
        nodes.put(new XYPair(2, 0), ETerrain.Water);

        Set<XYPair> forts = new HashSet<>();
        forts.add(new XYPair(0, 0));

        var halfMap = new HalfMap(nodes, forts);

        halfMapView.update(halfMap);

        String output = outputStreamCaptor.toString();

        assertThat(output, containsString("🌿"));
        assertThat(output, containsString("🏔️"));
        assertThat(output, containsString("🌊"));
        assertThat(output, containsString("🏰"));
        assertThat(output, containsString("▪️"));
    }

    @Test
    void Update_LargeMapIndices_CoversTensDigitLogic() {
        Map<XYPair, ETerrain> nodes = new HashMap<>();
        nodes.put(new XYPair(0, 0), ETerrain.Grass);
        var halfMap = new HalfMap(nodes, Set.of());

        halfMapView.update(halfMap);

        String output = outputStreamCaptor.toString();

        assertThat(output, containsString("0️⃣"));
        assertThat(output, containsString("1️⃣"));
        assertThat(output, containsString("2️⃣"));
        assertThat(output, containsString("3️⃣"));
        assertThat(output, containsString("4️⃣"));
        assertThat(output, containsString("5️⃣"));
        assertThat(output, containsString("6️⃣"));
        assertThat(output, containsString("7️⃣"));
        assertThat(output, containsString("8️⃣"));
        assertThat(output, containsString("9️⃣"));
    }
}