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
import lando.systems.ld60.game.components.GameInput;
import lando.systems.ld60.game.components.Health;
import lando.systems.ld60.game.movement.BrawlerMotion;

public enum ExamplePlayerState implements State<Entity> {
    // NOTE: in practice we should have a separate State<Entity> implementation
    //  for each core entity type, we're only sharing here for example purposes,
    //  but the code gets cluttered with conditions when trying to catch many
    //  different cases (spine animation vs normal, different movement types, etc)
    NORMAL_SPINEBOY {
        @Override
        public State<Entity> update(StateMachine<Entity> m, Entity e, float dt) {
            var mover = Mover.get(e);
            if (!mover.is(BrawlerMotion.class)) return this;
            if (GameInput.get(e).attack.pressed) return ATTACK_SPINEBOY;

            var anim   = SpineAnimator.get(e);
            var motion = mover.motion(BrawlerMotion.class);
            if (!motion.grounded) {
                var entry = anim.play("jump", false);
                // run the animation clip at the jump's speed, so its tuck
                // and landing poses line up with take-off and touchdown
                var animDuration = entry.getAnimation().getDuration();
                var hangTimeDuration = motion.tuning.hangTime(motion.tuning.jumpHeight());
                entry.setTimeScale(animDuration / hangTimeDuration);
            }
            else if (motion.landed)    { anim.start("run-to-idle", false); anim.queue("idle", true, 0); }
            else                       { locomotion(anim, mover, motion.tuning.maxSpeedX() * 0.5f); }

            return this;
        }

        static void locomotion(SpineAnimator anim, Mover mover, float runSpeed) {
            var speed = (float) Math.hypot(mover.xVel, mover.yVel);
            var want  = (speed == 0) ? "idle" : (speed >= runSpeed) ? "run" : "walk";

            if (anim.is(want)) return;
            if (want.equals("idle") && (anim.is("run-to-idle") || anim.is("idle-turn"))) return; // idle is queued behind it
            if (want.equals("idle") && anim.is("run")) {
                anim.start("run-to-idle", false);
                anim.queue("idle", true, 0);
                return;
            }
            anim.play(want); // crossfade from the mixes table; also drops a queued idle if the player moves again
        }
    },
    /** Stands and shoots; {@code GameHitboxes.SPINEBOY}'s "shoot" window puts the hit box out while the muzzle flash shows. */
    ATTACK_SPINEBOY {
        @Override
        public void enter(StateMachine<Entity> m, Entity e) {
            Mover.get(e).stop();
            shoot(SpineAnimator.get(e));
        }

        @Override
        public State<Entity> update(StateMachine<Entity> m, Entity e, float dt) {
            // the keyboard wrote Input already (INPUT runs before DECIDE): hold still, no jump, until the clip ends
            var input = Input.get(e);
            input.moveX = 0;
            input.moveY = 0;
            input.actionPressed = false;
            return SpineAnimator.get(e).is("shoot") ? this : NORMAL_SPINEBOY;
        }
    },
    /** Thrown by a hit ({@code GameStrikes} already turned and launched the mover): no controls until the motion is done with it. */
    HURT_SPINEBOY {
        @Override
        public void enter(StateMachine<Entity> m, Entity e) {
            var anim = SpineAnimator.get(e);
            anim.scale.set(anim.defaultScale.x * 1.2f, anim.defaultScale.y * 0.8f); // squash, eases back
        }

        @Override
        public State<Entity> update(StateMachine<Entity> m, Entity e, float dt) {
            var input = Input.get(e);
            input.moveX = 0;
            input.moveY = 0;

            var anim   = SpineAnimator.get(e);
            var motion = Mover.get(e).motion(BrawlerMotion.class);
            anim.play(motion.grounded ? "idle" : "jump", motion.grounded);
            return motion.launched ? this : NORMAL_SPINEBOY;
        }

        @Override
        public boolean interruptible(StateMachine<Entity> m, Entity e, State<Entity> by) {
            return false; // runs its course; a second hit re-launches the mover without re-entering
        }
    },
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

    /** Where spineboy's 'aim' IK points the gun: the crosshair bone's position from the feet, in skeleton units (x is always ahead, facing flips the skeleton). */
    static final float AIM_X = 300, AIM_Y = 85;

    /**
     * spineboy fires: "shoot" on track 0 then idle, and "aim" on track 1 for half a second so the
     * gun levels at the crosshair instead of the clip's pointing-down rest pose (the gun's recoil key
     * gives way to the aim pose). The player's attack and the dummy's timer both use it.
     */
    public static void shoot(SpineAnimator anim) {
        anim.skeleton.findBone("crosshair").getPose().setPosition(AIM_X, AIM_Y);
        anim.start("shoot", false);
        anim.queue("idle", true, 0);
        anim.start(1, "aim", true);
        anim.state.addEmptyAnimation(1, /*mix*/ 0.2f, /*delay*/ 0.5f);
    }
}
