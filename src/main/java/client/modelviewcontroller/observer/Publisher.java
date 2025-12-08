package client.modelviewcontroller.observer;

import java.util.ArrayList;
import java.util.List;

public class Publisher<T> {
    private final List<Subscriber<T>> subscribers = new ArrayList<>();

    public void subscribe(Subscriber<T> subscriber) {
        if (subscriber == null)
            throw new IllegalArgumentException("subscriber is null");

        subscribers.add(subscriber);
    }

    public void notify(T data) {
        if (data == null)
            throw new IllegalArgumentException("data is null");

        subscribers.forEach(listener -> listener.update(data));
    }
}
