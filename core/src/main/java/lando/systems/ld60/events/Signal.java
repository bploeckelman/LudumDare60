package lando.systems.ld60.events;

import com.badlogic.gdx.utils.SnapshotArray;

/**
 * One event type's subscriber list. A listener for a supertype of {@code T} (down to
 * {@link SignalEvent} itself) may subscribe too, so one catch-all listener can watch any signal.
 * @param <T>
 */
public class Signal<T extends SignalEvent> {
    private final SnapshotArray<SignalListener<? super T>> listeners;

    public Signal() {
        listeners = new SnapshotArray<>(false, 16);
    }

    /** Identity-based: pass the same object to {@link #remove}, so keep a method reference in a field. */
    public void register(SignalListener<? super T> listener) {
        if (!listeners.contains(listener, true)) {
            listeners.add(listener);
        }
    }

    public void remove(SignalListener<? super T> listener) {
        listeners.removeValue(listener, true);
    }

    public void removeAllListeners() {
        listeners.clear();
    }

    public void dispatch(T signalEvent) {
        // begin() creates a snapshot of the array so listeners can be safely
        // added/removed during this loop without throwing a ConcurrentModificationException
        final Object[] items = listeners.begin();
        for (int i = 0, n = listeners.size; i < n; i++) {
            @SuppressWarnings("unchecked")
            SignalListener<T> listener = (SignalListener<T>) items[i];
            listener.receive(this, signalEvent);
        }
        listeners.end();
    }
}
