package lando.systems.ld60.events;

public class DistortionEvent {

    public static final Signal<DistortionEvent> signal = new Signal<>();

    public static void trigger() {
        trigger(.8f, .4f);
    }

    public static void trigger(float intensity, float duration) {
        signal.dispatch(new DistortionEvent(intensity, duration));
    }

    public final float intensity;
    public final float duration;

    private DistortionEvent(float intensity, float duration) {
        this.intensity = intensity;
        this.duration = duration;
    }
}
