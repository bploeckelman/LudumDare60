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

public class WeaponPickupEffect implements ParticleEffect {

    private static final int STARS = 16;
    private static final float HEAD = 120f;

    public static class Params implements ParticleEffectParams {
        public final float x;
        public final float y;
        public boolean spawned;

        public Params(float x, float y) {
            this.x = x;
            this.y = y;
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

        var particles = new ArrayList<ParticleData>(STARS + 1);

        particles.add(ParticleData.initializer(pool.obtain())
                .keyframe(ParticleAnim.STAR.get().getKeyFrame(0f))
                .startPos(params.x, params.y + HEAD)
                .startSize(40f)
                .endSize(110f)
                .startRotation(0f)
                .endRotation(90f)
                .startColor(1f, 1f, 0.8f, 1f)
                .endColor(1f, 0.8f, 0.2f, 0f)
                .interpolation(Interpolation.pow2Out)
                .timeToLive(0.6f)
                .init());

        for (int i = 0; i < STARS; i++) {
            var angle = MathUtils.lerp(10f, 170f, i / (STARS - 1f)) + MathUtils.random(-8f, 8f);
            var size = MathUtils.random(36f, 60f);
            var spin = MathUtils.randomSign() * MathUtils.random(270f, 540f);
            var keyframe = ParticleAnim.STAR.get().getKeyFrame(MathUtils.random());
            particles.add(ParticleData.initializer(pool.obtain())
                    .keyframe(keyframe)
                    .startPos(params.x, params.y + HEAD)
                    .velocityDirection(angle, MathUtils.random(180f, 300f))
                    .acceleration(0f, -400f)
                    .startSize(size)
                    .endSize(size * 0.4f)
                    .startRotation(0f)
                    .endRotation(spin)
                    .startColor(1f, 1f, 0.6f, 1f)
                    .endColor(1f, 0.7f, 0.1f, 0f)
                    .interpAlpha(Interpolation.pow3In)
                    .interpolation(Interpolation.pow2Out)
                    .timeToLive(MathUtils.random(0.8f, 1.1f))
                    .init());
        }
        return particles;
    }
}
