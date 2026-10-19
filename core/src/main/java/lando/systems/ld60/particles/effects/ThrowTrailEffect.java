package lando.systems.ld60.particles.effects;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Pool;
import com.zendogames.zenlib.ecs.Entity;
import com.zendogames.zenlib.ecs.components.Mover;
import com.zendogames.zenlib.ecs.components.Position;
import lando.systems.ld60.assets.ParticleAnim;
import lando.systems.ld60.game.movement.BrawlerMotion;
import lando.systems.ld60.particles.ParticleData;
import lando.systems.ld60.particles.ParticleEffect;
import lando.systems.ld60.particles.ParticleEffectParams;
import lando.systems.ld60.utils.Util;

import java.util.ArrayList;
import java.util.List;

public class ThrowTrailEffect implements ParticleEffect {

    private static final int COUNT = 3;

    public static class Params implements ParticleEffectParams {
        public final Entity target;

        public Params(Entity target) {
            this.target = target;
        }

        @Override
        public boolean isComplete() {
            return !target.isAlive() || target.isRemoving();
        }
    }

    @Override
    public List<ParticleData> spawn(ParticleEffectParams parameters, Pool<ParticleData> pool) {
        var params = (Params) parameters;
        if (params.isComplete()) return List.of();

        var position = Position.get(params.target);
        var mover = Mover.get(params.target);
        var z = (mover.motion instanceof BrawlerMotion m) ? m.z : 0f;
        var direction = (mover.motion instanceof BrawlerMotion m) ? m.facing : 1f;
        var particles = new ArrayList<ParticleData>(COUNT);
        for (int i = 0; i < COUNT; i++) {
            var color = Util.randomColor();
            var size = MathUtils.random(30f, 40f);
            particles.add(ParticleData.initializer(pool.obtain())
                    .keyframe(ParticleAnim.STAR.get().getKeyFrame(MathUtils.random()))
                    .startPos(position.x - 30f * direction, position.y + z + 5f)
                    .velocity(-.2f * mover.xVel + MathUtils.random(-10f, 10f), MathUtils.random(-5f, 15f))
                    .acceleration(0f, -100f)
                    .startSize(size)
                    .endSize(size * 0.5f)
                    .startColor(color)
                    .endColor(color.r, color.g, color.b, 0f)
                    .timeToLive(MathUtils.random(0.25f, 0.45f))
                    .init());
        }
        return particles;
    }
}
