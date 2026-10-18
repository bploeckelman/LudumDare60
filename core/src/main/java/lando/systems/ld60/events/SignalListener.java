package lando.systems.ld60.events;

public interface SignalListener<T> {
    void receive(Signal<T> signal, T object);
}
