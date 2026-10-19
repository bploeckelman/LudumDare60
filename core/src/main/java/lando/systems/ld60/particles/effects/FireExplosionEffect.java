package lando.systems.ld60.particles.effects;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Pool;
import lando.systems.ld60.assets.ParticleAnim;
import lando.systems.ld60.particles.ParticleData;
import lando.systems.ld60.particles.ParticleEffect;
import lando.systems.ld60.particles.ParticleEffectParams;

import java.util.ArrayList;
import java.util.List;

public class FireExplosionEffect implements ParticleEffect {

    private static final int FIREBALLS = 40;
    private static final int EMBERS    = 30;
    private static final int SMOKE     = 12;
    private static final float BASE_SIZE = 100f;

    public static class Params implements ParticleEffectParams {
        public final Rectangle body;
        public boolean spawned;

        public Params(float x, float y, float width, float height) {
            this.body = new Rectangle(x - width / 2f, y, width, height);
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

        var body = params.body;
        float cx = body.x + body.width / 2f;
        float cy = body.y + body.height / 2f;
        float sx = body.width / BASE_SIZE;
        float sy = body.height / BASE_SIZE;

        var particles = new ArrayList<ParticleData>(1 + FIREBALLS + EMBERS + SMOKE);

        particles.add(ParticleData.initializer(pool.obtain())
                .keyframe(ParticleAnim.LIGHT.get().getKeyFrame(0f))
                .startPos(cx, cy)
                .startSize(body.width)
                .endSize(body.width * 2.5f)
                .startColor(1f, 1f, 1f, 1f)
                .endColor(1f, 0.5f, 0.5f, 0f)
                .interpolation(Interpolation.pow3Out)
                .timeToLive(0.5f)
                .init());

        for (int i = 0; i < FIREBALLS; i++) {
            var anim = MathUtils.randomBoolean() ? ParticleAnim.FLAME : ParticleAnim.FIRE;
            float size = MathUtils.random(40f, 100f);
            float angle = MathUtils.random(360f);
            float speed = MathUtils.random(40f, 250f);
            particles.add(ParticleData.initializer(pool.obtain())
                    .keyframe(anim.get().getKeyFrame(MathUtils.random(anim.asset().get().getAnimationDuration())))
                    .startPos(MathUtils.random(body.x, body.x + body.width), MathUtils.random(body.y, body.y + body.height))
                    .velocity(MathUtils.cosDeg(angle) * speed * sx, MathUtils.sinDeg(angle) * speed * sy)
                    .acceleration(0f, 140f * sy)
                    .startSize(size * 0.5f)
                    .endSize(size * 1.5f)
                    .startRotation(MathUtils.random(360f))
                    .startColor(1f, MathUtils.random(0.75f, 1f), 0.3f, 1f)
                    .endColor(0.5f, 0f, 0f, 0.7f)
                    .interpolation(Interpolation.pow2Out)
                    .interpAlpha(Interpolation.pow2In)
                    .timeToLive(MathUtils.random(0.5f, 1f))
                    .init());
        }

        for (int i = 0; i < EMBERS; i++) {
            var anim = ParticleAnim.SPARK;
            float size = MathUtils.random(8f, 18f);
            float angle = MathUtils.random(360f);
            float speed = MathUtils.random(200f, 450f);
            particles.add(ParticleData.initializer(pool.obtain())
                    .keyframe(anim.get().getKeyFrame(MathUtils.random(anim.asset().get().getAnimationDuration())))
                    .startPos(cx, cy)
                    .jitter(20f)
                    .velocity(MathUtils.cosDeg(angle) * speed * sx, MathUtils.sinDeg(angle) * speed * sy)
                    .acceleration(0f, -600f * sy)
                    .startSize(size)
                    .endSize(size * 0.3f)
                    .startColor(1f, 0.8f, 0.4f, 1f)
                    .endColor(0.9f, 0.2f, 0f, 0f)
                    .timeToLive(MathUtils.random(0.5f, 1.5f))
                    .init());
        }

        // smoke: dark, slow, lingers after the fire
        for (int i = 0; i < SMOKE; i++) {
            var anim = ParticleAnim.SMOKE;
            float size = MathUtils.random(50f, 90f);
            particles.add(ParticleData.initializer(pool.obtain())
                    .keyframe(anim.get().getKeyFrame(MathUtils.random(anim.asset().get().getAnimationDuration())))
                    .startPos(MathUtils.random(body.x, body.x + body.width), MathUtils.random(body.y, body.y + body.height))
                    .velocity(MathUtils.random(-20f, 20f) * sx, MathUtils.random(30f, 70f) * sy)
                    .startSize(size * 0.6f)
                    .endSize(size * 1.5f)
                    .startRotation(MathUtils.random(360f))
                    .startColor(0.35f, 0.2f, 0.15f, 0.6f)
                    .endColor(0.1f, 0.1f, 0.1f, 0f)
                    .interpolation(Interpolation.pow2Out)
                    .timeToLive(MathUtils.random(1.2f, 1.8f))
                    .init());
        }
        return particles;
    }
}
