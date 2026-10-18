package lando.systems.ld60.particles.effects;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Pool;
import com.zendogames.zenlib.Log;
import com.zendogames.zenlib.assets.ZenAnimHero;
import com.zendogames.zenlib.ecs.Entity;
import com.zendogames.zenlib.ecs.components.Position;
import com.zendogames.zenlib.ecs.components.SpineAnimator;
import lando.systems.ld60.particles.ParticleData;
import lando.systems.ld60.particles.ParticleEffect;
import lando.systems.ld60.particles.ParticleEffectParams;

import java.util.List;

public class FootStepDustCloudEffect implements ParticleEffect {

    public static class Params implements ParticleEffectParams {

        private static final String TAG = FootStepDustCloudEffect.class.getSimpleName();
        public final Entity target;
        public boolean footstepOccurred = false;

        public Params(Entity target) {
            this.target = target;
            var anim = SpineAnimator.get(target);
            if (anim != null) {
                anim.onEvent(event -> {
                    if ("footstep".equals(event.getData().getName())) {
                        footstepOccurred = true;
                        Log.info(TAG, "Footstep occurred");
                    }
                });
            }
        }

        @Override
        public boolean isComplete() {
            return !target.isAlive();
        }
    }

    @Override
    public List<ParticleData> spawn(ParticleEffectParams parameters, Pool<ParticleData> pool) {
        var params = (Params) parameters;
        var position = Position.get(params.target);

        if (params.footstepOccurred) {
            float vy = 30f;
            float ttl = MathUtils.random(.5f, .8f);
            var startSize = MathUtils.random(25f, 35f);
            params.footstepOccurred = false;
            return List.of(ParticleData.initializer(pool.obtain())
                    .animation(ZenAnimHero.EFFECT_LAND.get())
                    .startPos(position.x + MathUtils.random(-3f, 3f), position.y + 10f)
                    .velocity(0, vy)
                    .startSize(startSize)
                    .endSize(startSize * 1.5f)
                    .startColor(0.85f, 0.85f, 0.85f, 0.85f)
                    .endColor(0.7f, 0.7f, 0.7f, 0f)
                    .timeToLive(ttl)
                    .init());
        } else {
            return List.of();
        }
    }
}
