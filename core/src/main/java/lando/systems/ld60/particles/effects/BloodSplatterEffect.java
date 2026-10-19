package lando.systems.ld60.particles.effects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Pool;
import lando.systems.ld60.assets.ParticleAnim;
import lando.systems.ld60.game.combat.Hitbox;
import lando.systems.ld60.particles.ParticleData;
import lando.systems.ld60.particles.ParticleEffect;
import lando.systems.ld60.particles.ParticleEffectParams;

import java.util.ArrayList;
import java.util.List;

public class BloodSplatterEffect implements ParticleEffect {

    public static class Params implements ParticleEffectParams {
        public final float x;
        public final float y;
        public final int count;
        public boolean spawned;
        public Color tint;

        public Params(float x, float y, int count, Color tint) {
            this.x = x;
            this.y = y;
            this.count = count;
            this.tint = tint;
        }

        public Params(float x, float y, int count) {
            this(x, y, count, Color.BLUE);
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

        var frames = ParticleAnim.SPLAT.get().getKeyFrames();
        var particles = new ArrayList<ParticleData>(params.count);
        var color = params.tint;
        for (int i = 0; i < params.count; i++) {
            var reach = MathUtils.random(-30f, 30f);
            var size = MathUtils.random(32f, 64f);
            particles.add(ParticleData.initializer(pool.obtain())
                    .keyframe(frames[MathUtils.random(frames.length - 1)])
                    .startPos(params.x + reach, params.y + MathUtils.random(-Hitbox.DEPTH, Hitbox.DEPTH))
                    .startSize(size * 1.25f, size * 0.5f)
                    .startColor(color.r * .5f, color.g * .5f, color.b * .5f, 1f)
                    .endColor(color.r * .2f, color.g * .2f, color.b * .2f, 0.75f)
                    .interpolation(Interpolation.pow4In)
                    .timeToLive(MathUtils.random(4f, 6f))
                    .persist()
                    .init());
        }
        return particles;
    }
}
