package lando.systems.ld60.events;

import com.badlogic.gdx.utils.SnapshotArray;

public class Signal<T> {
    private final SnapshotArray<SignalListener<T>> listeners;

    public Signal() {
        listeners = new SnapshotArray<>(false, 16);
    }

    public void register(SignalListener<T> listener) {
        if (!listeners.contains(listener, true)) {
            listeners.add(listener);
        }
    }

    public void remove(SignalListener<T> listener) {
        listeners.removeValue(listener, true);
    }

    public void removeAllListeners() {
        listeners.clear();
    }

    public void dispatch(T object) {
        // begin() creates a snapshot of the array so listeners can be safely
        // added/removed during this loop without throwing a ConcurrentModificationException
        final Object[] items = listeners.begin();
        for (int i = 0, n = listeners.size; i < n; i++) {
            @SuppressWarnings("unchecked")
            SignalListener<T> listener = (SignalListener<T>) items[i];
            listener.receive(this, object);
        }
        listeners.end();
    }
}
