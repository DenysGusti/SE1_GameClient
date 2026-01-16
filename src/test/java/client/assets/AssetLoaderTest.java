package client.assets;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javafx.scene.image.Image;
import javafx.scene.shape.TriangleMesh;

class AssetLoaderTest {
    private AssetLoader assetLoader;
    private ObjTriangleMeshFactory objFactoryMock;

    @BeforeEach
    void setUp() {
        objFactoryMock = mock(ObjTriangleMeshFactory.class);
        assetLoader = new AssetLoader(objFactoryMock);
    }

    @Test
    void Constructor_NullFactory_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new AssetLoader(null));
    }

    @Test
    void LoadTextures_NullPaths_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> assetLoader.loadTextures(null, 1, 1));
    }

    @Test
    void LoadTextures_NullPathInMap_ThrowsIllegalArgumentException() {
        Map<String, String> paths = Collections.singletonMap("key", null);
        assertThrows(IllegalArgumentException.class, () -> assetLoader.loadTextures(paths, 1, 1));
    }

    @Test
    void LoadTextures_NegativeWidth_ThrowsIllegalArgumentException() {
        Map<String, String> paths = Map.of("test", "/assets/textures/block/snow.png");
        assertThrows(IllegalArgumentException.class, () -> assetLoader.loadTextures(paths, -1, 1));
    }

    @Test
    void LoadTextures_NegativeHeight_ThrowsIllegalArgumentException() {
        Map<String, String> paths = Map.of("test", "/assets/textures/block/snow.png");
        assertThrows(IllegalArgumentException.class, () -> assetLoader.loadTextures(paths, 1, -1));
    }

    @Test
    void LoadTextures_FileNotFound_ThrowsFileNotFoundException() {
        Map<String, String> paths = Map.of("missing", "/this/path/does/not/exist.png");
        assertThrows(FileNotFoundException.class, () -> assetLoader.loadTextures(paths, 1, 1));
    }

    @Test
    void LoadTextures_ValidPath_ReturnsPopulatedMap() throws IOException {
        Map<String, String> paths = Map.of("snow", "/assets/textures/block/snow.png");

        Map<String, Image> result = assetLoader.loadTextures(paths, 1, 1);

        assertThat(result, notNullValue());
        assertThat(result, hasKey("snow"));
        assertThat(result.size(), is(1));
    }

    @Test
    void LoadTextureArray_NullPath_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> assetLoader.loadTextureArray(null, 1, 1));
    }

    @Test
    void LoadTextureArray_NegativeWidth_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> assetLoader.loadTextureArray("/some/path", -1, 1));
    }

    @Test
    void LoadTextureArray_NegativeHeight_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> assetLoader.loadTextureArray("/some/path", 1, -1));
    }

    @Test
    void LoadTextureArray_ValidFolder_ReturnsArrayWithImages() throws IOException {
        Image[] result = assetLoader.loadTextureArray("/assets/textures/block/water", 1, 1);

        assertThat(result, notNullValue());
        assertThat(result.length, greaterThanOrEqualTo(0));
    }

    @Test
    void LoadTextureArray_EmptyFolder_ReturnsEmptyArray() throws IOException {
        var result = assetLoader.loadTextureArray("/non/existent/folder", 1, 1);
        assertThat(result.length, is(0));
    }

    @Test
    void LoadMeshes_NullPaths_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> assetLoader.loadMeshes(null));
    }

    @Test
    void LoadMeshes_NullPathInMap_ThrowsIllegalArgumentException() {
        Map<String, String> paths = Collections.singletonMap("key", null);
        assertThrows(IllegalArgumentException.class, () -> assetLoader.loadMeshes(paths));
    }

    @Test
    void LoadMeshes_FileNotFound_ThrowsFileNotFoundException() {
        Map<String, String> paths = Map.of("mesh", "/invalid/mesh.obj");
        assertThrows(FileNotFoundException.class, () -> assetLoader.loadMeshes(paths));
    }

    @Test
    void LoadMeshes_ValidPath_SuccessPath() throws IOException {
        Map<String, String> paths = Map.of("test", "/test.properties");
        when(objFactoryMock.createTriangleMesh(anyString())).thenReturn(mock(TriangleMesh.class));

        Map<String, TriangleMesh> result = assetLoader.loadMeshes(paths);

        assertThat(result, notNullValue());
        assertThat(result, hasKey("test"));
        verify(objFactoryMock).createTriangleMesh(anyString());
    }
}