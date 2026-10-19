package lando.systems.ld60.particles.effects;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Pool;
import com.zendogames.zenlib.ecs.components.Position;
import lando.systems.ld60.assets.GameAtlas;
import lando.systems.ld60.particles.ParticleData;
import lando.systems.ld60.particles.ParticleEffect;
import lando.systems.ld60.particles.ParticleEffectParams;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HeartPickupEffect implements ParticleEffect {

    public static class Params implements ParticleEffectParams {
        public Position origin;
        public Position playerPos;
        public boolean spawned;

        public Params(Position origin, Position playerPos) {
            this.origin = origin;
            this.playerPos = playerPos;
        }

        @Override
        public boolean isComplete() {
            return spawned;
        }
    }

    @Override
    public List<ParticleData> spawn(ParticleEffectParams parameters, Pool<ParticleData> pool) {
        var params = (Params) parameters;
        if (params.spawned) return Collections.emptyList();
        params.spawned = true;

        int count = 8;
        var particles = new ArrayList<ParticleData>(count);
        for (int i = 0; i < count; i++) {
            float ttl = MathUtils.random(0.4f, 0.7f);
            float startSize = MathUtils.random(14f, 22f);
            float startRot = MathUtils.random(-25f, 25f);

            particles.add(ParticleData.initializer(pool.obtain())
                .keyframe(GameAtlas.SPRITES.get().findRegion("pickups/health"))
                .interpolation(Interpolation.swingIn)
                .startPos(params.origin.x, params.origin.y)
                .jitter(15f)
                .targetPos(params.playerPos, 0f, 70f)
                .startSize(startSize)
                .endSize(4f)
                .startRotation(startRot)
                .endRotation(0f)
                .startColor(1f, 0.2f, 0.3f, 1f)
                .endColor(1f, 0.6f, 0.7f, 0.8f)
                .timeToLive(ttl)
                .init());
        }
        return particles;
    }
}
