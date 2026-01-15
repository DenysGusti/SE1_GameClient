package client.observer;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class PublisherTest {
    private Publisher<String> publisher;
    private Subscriber<String> subscriberMock;

    @BeforeEach
    public void setUp() {
        publisher = new Publisher<>();
        subscriberMock = Mockito.mock(Subscriber.class);
    }

    @Test
    public void ValidSubscriber_SubscribeCalled_SubscriberIsAdded() {
        var testData = "UpdateData";
        publisher.subscribe(subscriberMock);
        publisher.notify(testData);

        verify(subscriberMock, times(1)).update(testData);
    }

    @Test
    public void MultipleSubscribers_NotifyCalled_AllSubscribersAreNotified() {
        Subscriber<String> secondSubscriberMock = Mockito.mock(Subscriber.class);
        String testData = "BroadcastData";

        publisher.subscribe(subscriberMock);
        publisher.subscribe(secondSubscriberMock);
        publisher.notify(testData);

        verify(subscriberMock).update(testData);
        verify(secondSubscriberMock).update(testData);
    }

    @Test
    public void NullSubscriber_SubscribeCalled_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> publisher.subscribe(null));
    }

    @Test
    public void NullData_NotifyCalled_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> publisher.notify(null));
    }
}