package client.mvc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class Publisher<T> {
    private static final Logger logger = LoggerFactory.getLogger(Publisher.class);

    private final Collection<Subscriber<T>> subscribers = new ArrayList<>();

    public void subscribe(Subscriber<T> subscriber) {
        Objects.requireNonNull(subscriber, "subscriber must not be null");
        subscribers.add(subscriber);
    }

    public void update(T data) {
        Objects.requireNonNull(data, "data must not be null");
        subscribers.forEach(listener -> listener.update(data));
    }
}
