package client.modelviewcontroller.observer;

public interface Subscriber<T> {
    void update(T data);
}
