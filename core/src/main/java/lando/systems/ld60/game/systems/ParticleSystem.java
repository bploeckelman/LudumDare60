package lando.systems.ld60.game.systems;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool;
import com.zendogames.zenlib.ecs.Entity;
import com.zendogames.zenlib.ecs.Family;
import com.zendogames.zenlib.ecs.IteratingSystem;
import com.zendogames.zenlib.ecs.SystemOrder;
import com.zendogames.zenlib.ecs.components.Animator;
import com.zendogames.zenlib.ecs.components.Image;
import com.zendogames.zenlib.ecs.components.Position;
import com.zendogames.zenlib.ecs.components.Renderable;
import lando.systems.ld60.game.components.Emitter;
import lando.systems.ld60.game.components.Particle;
import lando.systems.ld60.particles.ParticleData;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ParticleSystem extends IteratingSystem implements Disposable {

    public final Pool<ParticleData> pool = new Pool<>() {
        @Override
        protected ParticleData newObject() {
            return new ParticleData();
        }
    };

    public List<ParticleData> activeData = new ArrayList<>();
    public List<Entity> activeParticles = new ArrayList<>();

    public ParticleSystem() {
        super(Family.one(Emitter.TYPE, Particle.TYPE), SystemOrder.PRESENT);
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        // Remove dead particle entities
        for (int i = activeParticles.size() - 1; i >= 0; i--) {
            var entity = activeParticles.get(i);
            var particle = Particle.get(entity);
            if (particle != null && particle.data.isDead()) {
                activeParticles.remove(i);
                world.despawn(entity);
            }
        }

        // Free dead particle data back to the pool
        for (int i = activeData.size() - 1; i >= 0; --i) {
            var particleData = activeData.get(i);
            if (particleData.isDead()) {
                activeData.remove(i);
                pool.free(particleData);
            }
        }

        // Remove completed emitters
        for (var entity : family().entities()) {
            var emitter = Emitter.get(entity);
            if (emitter == null) continue;
            if (emitter.isComplete()) {
                world.despawn(entity);
            }
        }
    }

    @Override
    protected void process(Entity entity, float delta) {
        var particle = Particle.get(entity);
        var emitter = Emitter.get(entity);

        if (particle != null) {
            updateParticle(entity, particle, delta);
        }
        else if (emitter != null) {
            var particlesData = emitter.spawn(pool);
            if (!particlesData.isEmpty()) {
                var entities = particlesData.stream()
                    .map(data -> createEntity(data, emitter))
                    .collect(Collectors.toList());

                activeData.addAll(particlesData);
                activeParticles.addAll(entities);
            }
        }
    }

    @Override
    public void dispose() {
        clear();
    }

    public void clear() {
        activeData.forEach(pool::free);
        activeData.clear();
        activeParticles.forEach(world::despawn);
        activeParticles.clear();
    }

    private Entity createEntity(ParticleData data, Emitter emitter) {
        var entity = world.createEntity();
        entity.add(new Particle(data));
        entity.add(new Position(data.xStart, data.yStart));

        Renderable renderable = null;
        if (data.animation != null) {
            renderable = new Animator(data.animation);
            entity.add((Animator) renderable);
        }
        else if (data.keyframe  != null) {
            renderable = new Image(data.keyframe);
            entity.add((Image) renderable);
        }
        if (renderable != null) {
            renderable.tint.set(data.rStart, data.gStart, data.bStart, data.aStart);
            renderable.origin.set(data.widthStart/2f, data.heightStart/2f);
            renderable.size.set(data.widthStart, data.heightStart);
            renderable.layer = emitter.layer;
            renderable.order = emitter.order;
        }
        return entity;
    }

    private void updateParticle(Entity entity, Particle particle, float delta) {
        var data = particle.data;
        var position = Position.get(entity);
        var animator = Animator.get(entity);
        var image = Image.get(entity);

        // so RenderSystem interpolates from last tick's position, not the spawn point
        position.savePrev();

        float lifetime, progress;

        if (data.timed) {
            data.ttl -= delta;
            if (data.ttl <= 0f && !data.persistent) {
                data.dead = true;
            }
            lifetime = MathUtils.clamp(data.ttl / data.ttlMax , 0f, 1f);
        } else {
            data.ttl += delta;
            lifetime = MathUtils.clamp(data.ttl, 0f, 1f);
        }
        progress = data.interpolation.apply(0f, 1f, MathUtils.clamp(1f - lifetime, 0f, 1f));

        // TODO: push to Animator component
        if (data.animation != null) {
            if (!data.persistent && !data.animUnlocked && data.timed) {
                data.animTime = progress * data.animation.getAnimationDuration();
            } else {
                data.animTime += delta;
            }
            data.keyframe = data.animation.getKeyFrame(data.animTime);
        }

        if (data.path != null) {
            // https://github.com/libgdx/libgdx/wiki/Path-interface-and-Splines#make-the-sprite-traverse-at-constant-speed
            var pathPos = data.path.derivativeAt(progress);
            var arcLength = progress + (delta * data.ttl / data.path.spanCount()) / pathPos.len();
            pathPos.set(data.path.valueAt(arcLength));
            position.set(pathPos.x, pathPos.y);
        }
        else if (data.targeted) {
            // If we're tracking a position component for the target, update the target coords
            if (data.targetPosition != null) {
                data.xTarget = data.targetPosition.x;
                data.yTarget = data.targetPosition.y;
            }
            position.set(
                MathUtils.lerp(data.xStart, data.xTarget, progress),
                MathUtils.lerp(data.yStart, data.yTarget, progress));
        }
        else {
            data.velocity.x += data.accel.x * delta;
            data.velocity.y += data.accel.y * delta;

            data.accel.x *= data.accDamp;
            data.accel.y *= data.accDamp;
            if (MathUtils.isEqual(data.accel.x, 0f, 0.01f)) data.accel.x = 0f;
            if (MathUtils.isEqual(data.accel.y, 0f, 0.01f)) data.accel.y = 0f;

            data.px += data.velocity.x * delta;
            data.py += data.velocity.y * delta;
            position.x = (int) data.px;
            position.y = (int) data.py;
        }

        data.width  = MathUtils.lerp(data.widthStart,  data.widthEnd,  progress);
        data.height = MathUtils.lerp(data.heightStart, data.heightEnd, progress);
        if (animator != null) {
            animator.origin.set(data.width/2f, data.height/2f);
            animator.size.set(data.width, data.height);
        } else if (image != null) {
            image.origin.set(data.width/2f, data.height/2f);
            image.size.set(data.width, data.height);
        }

        // TODO: this won't work because Image and Animator don't support rotation
        data.rotation = MathUtils.lerp(data.rotationStart, data.rotationEnd, progress);

        data.r = MathUtils.lerp(data.rStart, data.rEnd, progress);
        data.g = MathUtils.lerp(data.gStart, data.gEnd, progress);
        data.b = MathUtils.lerp(data.bStart, data.bEnd, progress);
        data.a = MathUtils.lerp(data.aStart, data.aEnd, progress);
        if (animator != null) {
            animator.tint.set(data.r, data.g, data.b, data.a);
        } else if (image != null) {
            image.tint.set(data.r, data.g, data.b, data.a);
        }

        data.collisionRect.set(
            position.x - data.width/2,
            position.y - data.height/2,
            data.width, data.height);
    }
}
