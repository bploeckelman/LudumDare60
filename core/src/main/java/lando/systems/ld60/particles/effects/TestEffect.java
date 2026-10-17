package lando.systems.ld60.particles.effects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Pool;
import lando.systems.ld60.particles.ParticleData;
import lando.systems.ld60.particles.ParticleEffect;
import lando.systems.ld60.particles.ParticleEffectParams;

import java.util.ArrayList;
import java.util.List;

public class TestEffect implements ParticleEffect {

    private static final int COUNT = 50;

    public static class Params implements ParticleEffectParams {
        public float x;
        public float y;
        public TextureRegion keyframe;
        public boolean spawned;

        public Params(float x, float y, TextureRegion keyframe) {
            this.x = x;
            this.y = y;
            this.keyframe = keyframe;
        }

        @Override
        public boolean isComplete() {
            return spawned;
        }
    }

    @Override
    public List<ParticleData> spawn(ParticleEffectParams parameters, Pool<ParticleData> pool) {
        var params = (TestEffect.Params) parameters;
        params.spawned = true;

        var particles = new ArrayList<ParticleData>(COUNT);
        for (int i = 0; i < COUNT; i++) {
            var startSize = MathUtils.random(2f, 5f);
            particles.add(ParticleData.initializer(pool.obtain())
                    .keyframe(params.keyframe)
                    .startPos(params.x, params.y)
                    .velocityDirection(MathUtils.random(360f), MathUtils.random(10f, 30f))
                    .startSize(startSize)
                    .endSize(0f)
                    .startColor(Color.LIGHT_GRAY)
                    .startAlpha(0.8f)
                    .endAlpha(0f)
                    .timeToLive(MathUtils.random(0.3f, 0.7f))
                    .init());
        }
        return particles;
    }
}
