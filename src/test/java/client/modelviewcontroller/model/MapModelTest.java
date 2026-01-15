package client.modelviewcontroller.model;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import client.data.fromclient.HalfMap;
import client.data.fromserver.FullMap;
import client.halfmaplogic.validation.exception.HalfMapGenerationException;
import client.observer.Subscriber;

public class MapModelTest {
    private MapModel mapModel;
    private Subscriber<FullMap> fullMapSubscriber;
    private Subscriber<HalfMap> halfMapSubscriber;
    private Subscriber<List<HalfMapGenerationException>> errorSubscriber;

    @BeforeEach
    public void setUp() {
        mapModel = new MapModel();
        fullMapSubscriber = mock(Subscriber.class);
        halfMapSubscriber = mock(Subscriber.class);
        errorSubscriber = mock(Subscriber.class);
    }

    @Test
    public void ValidFullMap_UpdateFullMapCalled_SubscriberIsNotified() {
        var fullMap = FullMap.emptyFullMap();
        mapModel.subscribeOnFullMapUpdated(fullMapSubscriber);
        mapModel.updateFullMap(fullMap);

        verify(fullMapSubscriber).update(fullMap);
    }

    @Test
    public void ValidHalfMap_UpdateHalfMapCalled_SubscriberIsNotified() {
        var halfMap = mock(HalfMap.class);
        mapModel.subscribeOnHalfMapGenerated(halfMapSubscriber);
        mapModel.updateHalfMap(halfMap);

        verify(halfMapSubscriber).update(halfMap);
    }

    @Test
    public void ValidErrorsList_UpdateHalfMapValidationErrorsCalled_SubscriberIsNotified() {
        var errors = List.of(new HalfMapGenerationException("Error"));
        mapModel.subscribeOnHalfMapValidationErrors(errorSubscriber);
        mapModel.updateHalfMapValidationErrors(errors);

        verify(errorSubscriber).update(errors);
    }

    @Test
    public void NullFullMap_UpdateFullMapCalled_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> mapModel.updateFullMap(null));
    }

    @Test
    public void NullSubscriber_SubscribeOnFullMapUpdatedCalled_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> mapModel.subscribeOnFullMapUpdated(null));
    }

    @Test
    public void NullHalfMap_UpdateHalfMapCalled_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> mapModel.updateHalfMap(null));
    }

    @Test
    public void NullSubscriber_SubscribeOnHalfMapGeneratedCalled_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> mapModel.subscribeOnHalfMapGenerated(null));
    }

    @Test
    public void NullErrorsList_UpdateHalfMapValidationErrorsCalled_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> mapModel.updateHalfMapValidationErrors(null));
    }

    @Test
    public void NullSubscriber_SubscribeOnHalfMapValidationErrorsCalled_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> mapModel.subscribeOnHalfMapValidationErrors(null));
    }
}