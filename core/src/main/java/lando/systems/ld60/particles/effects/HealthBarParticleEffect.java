package lando.systems.ld60.particles.effects;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Pool;
import com.zendogames.zenlib.assets.ZenAnimHero;
import com.zendogames.zenlib.ecs.components.Position;
import lando.systems.ld60.Main;
import lando.systems.ld60.particles.ParticleData;
import lando.systems.ld60.particles.ParticleEffect;
import lando.systems.ld60.particles.ParticleEffectParams;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HealthBarParticleEffect implements ParticleEffect {

    public static class Params implements ParticleEffectParams {
        public Position target;
        public boolean spawned;

        public Params(float x, float y) {
            this.target = new Position(x, y);
        }

        @Override
        public boolean isComplete() {
            return spawned;
        }
    }

    @Override
    public List<ParticleData> spawn(ParticleEffectParams paramaters, Pool<ParticleData> pool) {
        var params = (HealthBarParticleEffect.Params) paramaters;
        if (params.spawned) return Collections.emptyList();
        params.spawned = true;


        var particles = new ArrayList<ParticleData>(16);
        for (int i = 0; i < 1; i++) {
            float speed = 30;
            float ttl = MathUtils.random(.3f, .6f);
            particles.add(ParticleData.initializer(pool.obtain())
                .keyframe(new TextureRegion(Main.game.pixel))
                .interpolation(Interpolation.linear)
                .startPos(params.target.x, params.target.y + 5)
                .velocity(0, speed)
                .startSize(MathUtils.random(1f, 3f), 10f)
                .startColor(.9f, .5f, .01f, .9f)
                .endColor(.3f, 0.3f, .3f, 0f)
                .timeToLive(ttl)
                .init());
        }
        return particles;
    }
}
