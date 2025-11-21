package client.modelviewcontroller.observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Publisher<T> {
    private static final Logger logger = LoggerFactory.getLogger(Publisher.class);

    private final List<Subscriber<T>> subscribers = new ArrayList<>();

    public void subscribe(Subscriber<T> subscriber) {
        if (subscriber == null)
            throw new IllegalArgumentException("subscriber must not be null");

        subscribers.add(subscriber);
    }

    public void notify(T data) {
        if (data == null)
            throw new IllegalArgumentException("data must not be null");

        subscribers.forEach(listener -> listener.update(data));
    }
}
