package lando.systems.ld60.particles.effects;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Pool;
import lando.systems.ld60.assets.ParticleAnim;
import lando.systems.ld60.particles.ParticleData;
import lando.systems.ld60.particles.ParticleEffect;
import lando.systems.ld60.particles.ParticleEffectParams;

import java.util.ArrayList;
import java.util.List;

public class KickEffect implements ParticleEffect {

    private static final int PUFFS = 5;

    public static class Params implements ParticleEffectParams {
        public final float x;
        public final float y;
        public final int direction;
        public boolean spawned;

        public Params(float x, float y, int direction) {
            this.x = x;
            this.y = y;
            this.direction = direction;
        }

        @Override
        public boolean isComplete() {
            return spawned;
        }
    }

    @Override
    public List<ParticleData> spawn(ParticleEffectParams parameters, Pool<ParticleData> pool) {
        var params = (Params) parameters;
        params.spawned = true;

        var particles = new ArrayList<ParticleData>(PUFFS);
        for (int i = 0; i < PUFFS; i++) {
            var size = MathUtils.random(15f, 25f);
            var keyframe = ParticleAnim.FLAME.get().getKeyFrame(MathUtils.random(ParticleAnim.SMOKE.asset().get().getAnimationDuration()));
            particles.add(ParticleData.initializer(pool.obtain())
                    .keyframe(keyframe)
                    .startPos(params.x, params.y)
                    .jitter(4f)
                    .velocity(MathUtils.random(40f, 90f) * params.direction, MathUtils.random(5f, 25f))
                    .startSize(size)
                    .endSize(size * 1.6f)
                    .startColor(1f, 0.2f, 0.2f, 1f)
                    .endColor(1f, 0.1f, 0.1f, .4f)
                    .interpolation(Interpolation.pow4In)
                    .timeToLive(MathUtils.random(0.5f, 0.75f))
                    .init());
        }
        return particles;
    }
}
