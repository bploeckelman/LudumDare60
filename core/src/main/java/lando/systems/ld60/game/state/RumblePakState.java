package lando.systems.ld60.game.state;

import com.badlogic.gdx.math.MathUtils;
import com.zendogames.zenlib.ecs.Entity;
import com.zendogames.zenlib.ecs.components.Animator;
import com.zendogames.zenlib.ecs.components.Position;
import com.zendogames.zenlib.state.State;
import com.zendogames.zenlib.state.StateMachine;
import lando.systems.ld60.game.characters.N64;
import lando.systems.ld60.game.components.RumblePak;

/**
 * The N64 boss's rumble pak from the muzzle to the floor: {@link #FLY} tumbles the first frames
 * of the projectile clip while the entity crosses the street to its landing spot, {@link #FALL}
 * plays the rest (the pack dropping to the floor) and sets it off when the clip is done.
 */
public enum RumblePakState implements State<Entity> {
    /** Toward {@code RumblePak.landX/landY} at its speed, looping the airborne frames. */
    FLY {
        @Override
        public State<Entity> update(StateMachine<Entity> m, Entity e, float dt) {
            var pak  = RumblePak.get(e);
            var pos  = Position.get(e);
            var anim = Animator.get(e);

            // The airborne frames, over and over, until it is down (the animation system adds dt after this)
            anim.time %= N64.Props.pakFlightLoop;

            float dx = pak.landX - pak.x;
            float dy = pak.landY - pak.y;
            float dist = (float) Math.hypot(dx, dy);
            float step = pak.speed * dt;
            var arrived = dist <= step;
            if (arrived) {
                pak.x = pak.landX;
                pak.y = pak.landY;
            } else {
                pak.x += dx / dist * step;
                pak.y += dy / dist * step;
            }
            pos.savePrev();
            pos.set(MathUtils.round(pak.x), MathUtils.round(pak.y));
            anim.order = -pos.y;
            return arrived ? FALL : this;
        }
    },
    /** The pack comes down on the target; when the clip has played through, it goes off. */
    FALL {
        @Override
        public void enter(StateMachine<Entity> m, Entity e) {
            Animator.get(e).time = N64.Props.pakFlightLoop; // the first falling frame
        }

        @Override
        public State<Entity> update(StateMachine<Entity> m, Entity e, float dt) {
            if (e.isRemoving()) return this;
            if (Animator.get(e).isComplete()) {
                N64.explode(e);
                e.world().despawn(e);
            }
            return this;
        }
    };

    @Override
    public boolean interruptible(StateMachine<Entity> m, Entity e, State<Entity> by) {
        return false;
    }
}
