package lando.systems.ld60.particles.effects;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Pool;
import com.zendogames.zenlib.assets.ZenAnimHero;
import com.zendogames.zenlib.ecs.components.Position;
import lando.systems.ld60.particles.ParticleData;
import lando.systems.ld60.particles.ParticleEffect;
import lando.systems.ld60.particles.ParticleEffectParams;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CloneEffect implements ParticleEffect {

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
    public List<ParticleData> spawn(ParticleEffectParams parameters, Pool<ParticleData> pool) {
        var params = (Params) parameters;
        if (params.spawned) return Collections.emptyList();
        params.spawned = true;

        var particles = new ArrayList<ParticleData>(16);
        for (int i = 0; i < 16; i++) {
            float angle = MathUtils.random(0f, 360f);
            float speed = MathUtils.random(20f, 50f);
            float ttl = MathUtils.random(1.5f, 3f);
            float startRot = MathUtils.random(0f, 360f);
            float endRot = startRot + MathUtils.randomSign() * MathUtils.random(180f, 540f);
            var startSize = MathUtils.random(22f, 35f);
            particles.add(ParticleData.initializer(pool.obtain())
                    .animation(ZenAnimHero.RUN.get())
                    .interpolation(Interpolation.pow2Out)
                    .startPos(params.target.x, params.target.y)
                    .velocityDirection(angle, speed)
                    .startSize(startSize)
                    .endSize(0f)
                    .startRotation(startRot)
                    .endRotation(endRot)
                    .startColor(1f, 1f, 0.6f, 1f)
                    .endColor(1f, 0.7f, 0f, 0f)
                    .timeToLive(ttl)
                    .init());
        }
        return particles;
    }
}
