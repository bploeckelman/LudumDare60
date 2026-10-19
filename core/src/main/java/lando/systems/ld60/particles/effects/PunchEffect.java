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

public class PunchEffect implements ParticleEffect {

    private static final int SPARKS = 6;

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

        var baseAngle = (params.direction < 0) ? 180f : 0f;
        var particles = new ArrayList<ParticleData>(SPARKS);
        for (int i = 0; i < SPARKS; i++) {
            var size = MathUtils.random(30f, 50f);
            var keyframe = ParticleAnim.MAGIC.get().getKeyFrame(MathUtils.random());
            particles.add(ParticleData.initializer(pool.obtain())
                    .keyframe(keyframe)
                    .startPos(params.x, params.y + 15f)
                    .jitter(3f)
                    .velocityDirection(baseAngle + MathUtils.random(-30f, 30f), MathUtils.random(60f, 120f))
                    .startSize(size)
                    .endSize(size * 0.3f)
                    .startColor(1f, 1f, 0.7f, 1f)
                    .endColor(1f, 0.6f, 0.2f, 0f)
                    .interpolation(Interpolation.pow2Out)
                    .timeToLive(MathUtils.random(0.25f, .5f))
                    .init());
        }
        return particles;
    }
}
