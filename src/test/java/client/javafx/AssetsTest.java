package client.javafx;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import client.data.XYPair;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

class AssetsTest {
    private Assets assets;
    private Map<String, Image> textureMap;
    private Image[] waterTextures;
    private Map<String, TriangleMesh> meshMap;

    @BeforeEach
    void setUp() {
        textureMap = new HashMap<>();
        textureMap.put("grass", new WritableImage(1, 1));

        waterTextures = new Image[]{new WritableImage(1, 1), new WritableImage(1, 1)};

        meshMap = new HashMap<>();
        meshMap.put("cube", new TriangleMesh());

        assets = new Assets(textureMap, waterTextures, meshMap);
    }

    @Test
    void Constructor_NullTextures_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Assets(null, waterTextures, meshMap));
    }

    @Test
    void Constructor_NullWaterTextures_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Assets(textureMap, null, meshMap));
    }

    @Test
    void Constructor_NullMeshes_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Assets(textureMap, waterTextures, null));
    }

    @Test
    void GetMaterial_NullName_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> assets.getMaterial(null));
    }

    @Test
    void GetMaterial_MissingMaterial_ThrowsNoSuchElementException() {
        assertThrows(NoSuchElementException.class, () -> assets.getMaterial("nonExistent"));
    }

    @Test
    void GetMaterial_ValidName_ReturnsMaterial() {
        assertThat(assets.getMaterial("grass"), notNullValue());
        assertThat(assets.getMaterial("water"), notNullValue());
    }

    @Test
    void RotateWaterTexture_CyclesThroughArray() {
        assets.rotateWaterTexture();
        assertThat(assets.getMaterial("water").getDiffuseMap(), is(waterTextures[1]));

        assets.rotateWaterTexture();
        assertThat(assets.getMaterial("water").getDiffuseMap(), is(waterTextures[0]));
    }

    @Test
    void GetMesh_NullName_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> assets.getMesh(null));
    }

    @Test
    void GetMesh_MissingMesh_ThrowsNoSuchElementException() {
        assertThrows(NoSuchElementException.class, () -> assets.getMesh("missingMesh"));
    }

    @Test
    void GetMesh_ValidName_ReturnsMesh() {
        assertThat(assets.getMesh("cube"), notNullValue());
    }

    @Test
    void CreateMeshView_NullMeshName_ThrowsIllegalArgumentException() {
        var coordinate = new XYPair(1, 1);
        assertThrows(IllegalArgumentException.class, () -> assets.createMeshView(null, "grass", coordinate, 0));
    }

    @Test
    void CreateMeshView_NullMaterialName_ThrowsIllegalArgumentException() {
        var coordinate = new XYPair(1, 1);
        assertThrows(IllegalArgumentException.class, () -> assets.createMeshView("cube", null, coordinate, 0));
    }

    @Test
    void CreateMeshView_NullCoordinate_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> assets.createMeshView("cube", "grass", null, 0));
    }

    @Test
    void CreateMeshView_ValidInput_ReturnsConfiguredView() {
        var coordinate = new XYPair(10, 20);
        double yPos = 5.;

        MeshView view = assets.createMeshView("cube", "grass", coordinate, yPos);

        assertThat(view, is(notNullValue()));
        assertThat(view.getMesh(), is(meshMap.get("cube")));
        assertThat(view.getMaterial(), is(assets.getMaterial("grass")));

        // (10, 20) -> X=20, Z=10
        assertThat(view.getTranslateX(), is(20.0));
        assertThat(view.getTranslateZ(), is(10.0));
        assertThat(view.getTranslateY(), is(5.0));
    }
}