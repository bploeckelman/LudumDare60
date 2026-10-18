package lando.systems.ld60.particles.effects;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Pool;
import lando.systems.ld60.Main;
import lando.systems.ld60.particles.ParticleData;
import lando.systems.ld60.particles.ParticleEffect;
import lando.systems.ld60.particles.ParticleEffectParams;

import java.util.ArrayList;
import java.util.List;

public class BloodSprayEffect implements ParticleEffect {

    private static final int DROPLETS = 50;

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

        var baseAngle = (params.direction < 0) ? 170f : 10f;
        var particles = new ArrayList<ParticleData>(DROPLETS);
        var pixel = new TextureRegion(Main.game.pixel);
        for (int i = 0; i < DROPLETS; i++) {
            var size = MathUtils.random(5f, 10f);
            particles.add(ParticleData.initializer(pool.obtain())
                    .keyframe(pixel)
                    .startPos(params.x, params.y)
                    .jitter(2f)
                    .velocityDirection(baseAngle + MathUtils.random(-25f, 45f) * params.direction, MathUtils.random(80f, 160f))
                    .acceleration(0f, -200f)
                    .startSize(size)
                    .endSize(size * 0.5f)
                    .startColor(0.1f, 0.1f, 0.8f, 1f)
                    .endColor(0f, 0f, 0.2f, 0f)
                    .interpolation(Interpolation.pow2In)
                    .timeToLive(MathUtils.random(0.5f, 0.9f))
                    .init());
        }
        return particles;
    }
}
