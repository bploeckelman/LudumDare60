package lando.systems.ld60.game.components;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Pool;
import com.zendogames.zenlib.ecs.ComponentType;
import com.zendogames.zenlib.ecs.Entity;
import com.zendogames.zenlib.ecs.components.Renderable;
import lando.systems.ld60.assets.EmitterType;
import lando.systems.ld60.particles.ParticleData;
import lando.systems.ld60.particles.ParticleEffect;
import lando.systems.ld60.particles.ParticleEffectParams;

import java.util.List;

public class Emitter extends Renderable {

    public static final ComponentType<Emitter> TYPE = ComponentType.register(Emitter.class);
    public static Emitter get(Entity e) { return TYPE.get(e); }
    public static boolean has(Entity e) { return TYPE.has(e); }

    public final EmitterType emitterType;
    public final ParticleEffect effect;

    public ParticleEffectParams params;

    public Emitter(EmitterType emitterType, ParticleEffectParams params) {
        this.emitterType = emitterType;
        this.effect = emitterType.effect;
        this.params = params;
    }

    @Override
    public void render(Batch batch, float x, float y) {
        throw new GdxRuntimeException("render() not yet implemented for Emitter renderable");
    }

    public List<ParticleData> spawn(Pool<ParticleData> pool) {
        return effect.spawn(params, pool);
    }

    public boolean isComplete() {
        return params.isComplete();
    }
}
