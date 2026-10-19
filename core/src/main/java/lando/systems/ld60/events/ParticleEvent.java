package lando.systems.ld60.events;

import com.zendogames.zenlib.render.RenderLayer;
import lando.systems.ld60.assets.EmitterType;
import lando.systems.ld60.particles.ParticleEffectParams;

public class ParticleEvent implements SignalEvent {
    public static final Signal<ParticleEvent> signal = new Signal<>();

    // Static helper to keep calling code incredibly clean
    public static void spawn(EmitterType type, ParticleEffectParams params, RenderLayer layer) {
        signal.dispatch(new ParticleEvent(type, params, layer));
    }

    public final EmitterType type;
    public final ParticleEffectParams params;
    public final RenderLayer layer;

    private ParticleEvent(EmitterType type, ParticleEffectParams params, RenderLayer layer) {
        this.type = type;
        this.params = params;
        this.layer = layer;
    }
}
