package lando.systems.ld60.game.components;

import com.zendogames.zenlib.ecs.Component;
import com.zendogames.zenlib.ecs.ComponentType;
import com.zendogames.zenlib.ecs.Entity;
import lando.systems.ld60.particles.ParticleData;

public class Particle implements Component {

    public static final ComponentType<Particle> TYPE = ComponentType.register(Particle.class);
    public static Particle get(Entity e) { return TYPE.get(e); }
    public static boolean  has(Entity e) { return TYPE.has(e); }

    public final ParticleData data;

    public Particle(ParticleData data) {
        this.data = data;
    }
}
