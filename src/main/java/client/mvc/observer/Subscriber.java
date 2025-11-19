package client.mvc.observer;

public interface Subscriber<T> {
    void update(T data);
}
