package lando.systems.ld60.particles.effects;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Pool;
import com.zendogames.zenlib.ecs.components.Position;
import com.zendogames.zenlib.ecs.components.Renderable;
import lando.systems.ld60.Main;
import lando.systems.ld60.assets.ParticleAnim;
import lando.systems.ld60.particles.ParticleData;
import lando.systems.ld60.particles.ParticleEffect;
import lando.systems.ld60.particles.ParticleEffectParams;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SpawnCloudEffect implements ParticleEffect {

    public static class Params implements ParticleEffectParams {
        public Rectangle target;
        public int spawned;

        public Params(Position p) {
            this.target = new Rectangle(p.x-24, p.y, 48, 80);
            spawned = 0;
        }

        @Override
        public boolean isComplete() {
            return spawned > 10;
        }
    }

    @Override
    public List<ParticleData> spawn(ParticleEffectParams paramaters, Pool<ParticleData> pool) {
        var params = (SpawnCloudEffect.Params) paramaters;
        params.spawned++;

        var particles = new ArrayList<ParticleData>(16);
        for (int i = 0; i < 9; i++) {
            float angle = MathUtils.random(0, 360);
            float speed = MathUtils.random(0, 80f);
            float ttl = MathUtils.random(.3f, .8f);
            float startSize = MathUtils.random(30f, 65f);
            float randomX = MathUtils.random(params.target.x, params.target.x + params.target.width);
            float randomY = MathUtils.random(params.target.y, params.target.y + params.target.height);
            var keyframe = ParticleAnim.SMOKE.get().getKeyFrame(MathUtils.random(ParticleAnim.SMOKE.asset().get().getAnimationDuration()));
            particles.add(ParticleData.initializer(pool.obtain())
                .keyframe(keyframe)
                .interpolation(Interpolation.linear)
                .startPos(randomX, randomY)
                .velocity(MathUtils.cosDeg(angle) * speed, MathUtils.sinDeg(angle) * speed)
                .startSize(startSize)
                .endSize(MathUtils.random(startSize*.4f, startSize))
                .startColor(1f, 1f, 1f, 1.0f)
                .endColor(.3f, 0.3f, .3f, 0f)
                .timeToLive(ttl)
                .init());
        }
        return particles;
    }
}
