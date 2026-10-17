package lando.systems.ld60.particles;

import com.badlogic.gdx.utils.Pool;

import java.util.List;

public interface ParticleEffect {
    List<ParticleData> spawn(ParticleEffectParams params, Pool<ParticleData> pool);
}
