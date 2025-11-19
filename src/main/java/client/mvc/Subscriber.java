package client.mvc;

public interface Subscriber<T> {
    void update(T data);
}
