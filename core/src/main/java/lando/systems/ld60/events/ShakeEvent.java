package lando.systems.ld60.events;

public class ShakeEvent implements SignalEvent {

    public static final Signal<ShakeEvent> signal = new Signal<>();

    public static void trigger() {
        trigger(.8f);
    }

    public static void trigger(float intensity) {
        signal.dispatch(new ShakeEvent(intensity));
    }

    public final float trauma;

    private ShakeEvent(float trauma) {
        this.trauma = trauma;
    }
}
