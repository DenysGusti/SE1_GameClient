package client.modelviewcontroller.view;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import client.data.fromserver.FullMap;
import client.javafx.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FullMapViewJavaFXTest {
    private CameraMovementDetector cameraDetectorMock;
    private Assets assetsMock;
    private TerrainManager terrainManagerMock;
    private EntityManager entityManagerMock;

    @BeforeEach
    void setUp() {
        cameraDetectorMock = mock(CameraMovementDetector.class);
        assetsMock = mock(Assets.class);
        terrainManagerMock = mock(TerrainManager.class);
        entityManagerMock = mock(EntityManager.class);
    }

    @Test
    void Constructor_NullCameraMovementDetector_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new FullMapViewJavaFX(null, assetsMock, terrainManagerMock, entityManagerMock));
    }

    @Test
    void Constructor_NullAssets_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new FullMapViewJavaFX(cameraDetectorMock, null, terrainManagerMock, entityManagerMock));
    }

    @Test
    void Constructor_NullTerrainManager_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new FullMapViewJavaFX(cameraDetectorMock, assetsMock, null, entityManagerMock));
    }

    @Test
    void Constructor_NullEntityManager_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new FullMapViewJavaFX(cameraDetectorMock, assetsMock, terrainManagerMock, null));
    }

    @Test
    void Constructor_ValidInputs_ThrowsIllegalStateExceptionDueToToolkit() {
        assertThrows(IllegalStateException.class, () ->
                new FullMapViewJavaFX(cameraDetectorMock, assetsMock, terrainManagerMock, entityManagerMock));
    }

    @Test
    void Update_NullFullMap_ThrowsIllegalArgumentException() {
        var mockedView = mock(FullMapViewJavaFX.class);
        doCallRealMethod().when(mockedView).update(any());

        assertThrows(IllegalArgumentException.class, () -> mockedView.update(null));
    }

    @Test
    void Update_ValidFullMap_ThrowsIllegalStateExceptionDueToToolkit() {
        var mockedView = mock(FullMapViewJavaFX.class);
        doCallRealMethod().when(mockedView).update(any());

        assertThrows(IllegalStateException.class, () -> mockedView.update(mock(FullMap.class)));
    }
}