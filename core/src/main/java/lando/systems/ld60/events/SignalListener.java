package lando.systems.ld60.events;

public interface SignalListener<T extends SignalEvent> {
    void receive(Signal<T> signal, T object);
}
