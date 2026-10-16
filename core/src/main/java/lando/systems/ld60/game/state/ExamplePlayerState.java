package lando.systems.ld60.game.state;

import com.zendogames.zenlib.Log;
import com.zendogames.zenlib.assets.ZenAnimHero;
import com.zendogames.zenlib.ecs.Entity;
import com.zendogames.zenlib.ecs.components.*;
import com.zendogames.zenlib.ecs.movement.platformer.PlatformerMode;
import com.zendogames.zenlib.ecs.movement.platformer.PlatformerMotion;
import com.zendogames.zenlib.ecs.movement.topdown.TopDownMotion;
import com.zendogames.zenlib.input.InputControls;
import com.zendogames.zenlib.state.State;
import com.zendogames.zenlib.state.StateMachine;
import lando.systems.ld60.game.components.Health;

public enum ExamplePlayerState implements State<Entity> {
    NORMAL {
        @Override
        public State<Entity> update(StateMachine<Entity> m, Entity e, float dt) {
            var animator = Animator.get(e);
            var mover    = Mover.get(e);

            // Top down movement updates are simple, handle them here
            if (mover.is(TopDownMotion.class)) {
                var motion = mover.motion(TopDownMotion.class);
                if (motion.faceX != 0) {
                    animator.facing = motion.faceX; // left/right flip, up/down keep last horizontal facing
                }
                animator.play(mover.isMoving() ? ZenAnimHero.RUN : ZenAnimHero.IDLE);
                return this;
            }

            // Otherwise, apply platformer movement updates if appropriate
            if (mover.is(PlatformerMotion.class)) {
                var motion = mover.motion(PlatformerMotion.class);

                animator.facing = motion.facing;
                if (Sensor.has(e)) Sensor.get(e).facing = motion.facing;
                if (motion.grounded && !motion.jumped) {
                    var running = Math.abs(mover.xVel) > 1f;
                    animator.play(running ? ZenAnimHero.RUN : ZenAnimHero.IDLE);
                }

                if (motion.dropping) { animator.play(ZenAnimHero.IDLE); }
                if (motion.jumped)   { animator.play(ZenAnimHero.JUMP); animator.scale.set(0.8f, 1.2f); }
                if (motion.landed)   { animator.play(ZenAnimHero.IDLE); animator.scale.set(1.2f, 0.8f); }

                if (motion.mode == PlatformerMode.LEDGE) { animator.play(ZenAnimHero.IDLE); }
                if (motion.mode == PlatformerMode.DASH)  { animator.play(ZenAnimHero.RUN); animator.scale.set(1.3f, 0.7f); }

                return this;
            }

            Log.warn(TAG, "unhandled mover motion type in player state for %s: %s", e, mover.motion.getClass().getSimpleName());
            return this;
        }
    },
    HURT {
        @Override
        public void enter(StateMachine<Entity> m, Entity e) {
            var mover = Mover.get(e);

            if (mover.is(PlatformerMotion.class)) {
                // launch away from attacker in an arc
                var motion = mover.motion(PlatformerMotion.class);
                motion.launch(mover, -motion.facing, /*dist*/ 2f, /*height*/ 1.5f, /*lock*/ 1f);
            } else if (mover.is(TopDownMotion.class)) {
                // shoved straight away from the attacker at full speed; update() keeps steering that way
                var motion = mover.motion(TopDownMotion.class);
                mover.xVel = -motion.faceX * motion.tuning.maxSpeed();
                mover.yVel = -motion.faceY * motion.tuning.maxSpeed();
            }

            var animator = Animator.get(e);
            animator.start(ZenAnimHero.HURT);
            animator.scale.set(1.3f, 0.7f);

            Health.get(e).current--;
        }

        @Override
        public State<Entity> update(StateMachine<Entity> m, Entity e, float dt) {
            var mover = Mover.get(e);
            if (mover.is(TopDownMotion.class)) {
                // the keyboard wrote Input already (INPUT runs before DECIDE): steer along the shove instead,
                // which the motion zeroes at a wall, so the player stops there and stays stopped
                var input = Input.get(e);
                input.moveX = (int) Math.signum(mover.xVel);
                input.moveY = (int) Math.signum(mover.yVel);
            }
            return Animator.get(e).isComplete() ? NORMAL : this;
        }

        @Override
        public boolean interruptible(StateMachine<Entity> m, Entity e, State<Entity> by) {
            return by == DEAD;
        }
    },
    DEAD {
        @Override
        public void enter(StateMachine<Entity> m, Entity e) {
            Mover.get(e).stop();
            Input.get(e).reset();
            Animator.get(e).start(ZenAnimHero.DEATH);
        }

        @Override
        public boolean interruptible(StateMachine<Entity> m, Entity e, State<Entity> by) {
            return false;
        }
    }
    ;

    private static final String TAG = ExamplePlayerState.class.getSimpleName();

    @Override
    public State<Entity> update(StateMachine<Entity> stateMachine, Entity entity, float v) {
        // Stay in the current state, unless a specific state's update() overrides
        return this;
    }

    public static void revivePlayer(Entity e, InputControls controls) {
        Health.get(e).restore();
        Input.get(e).controls = controls;
        Behavior.get(e).machine.force(e, ExamplePlayerState.NORMAL);
    }
}
