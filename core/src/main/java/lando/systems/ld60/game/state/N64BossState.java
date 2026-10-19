package lando.systems.ld60.game.state;

import com.badlogic.gdx.math.MathUtils;
import com.zendogames.zenlib.ecs.Entity;
import com.zendogames.zenlib.ecs.components.*;
import com.zendogames.zenlib.state.State;
import com.zendogames.zenlib.state.StateMachine;
import lando.systems.ld60.assets.SoundType;
import lando.systems.ld60.events.AudioEvent;
import lando.systems.ld60.game.ai.Senses;
import lando.systems.ld60.game.anim.Move;
import lando.systems.ld60.game.characters.N64;
import lando.systems.ld60.game.characters.N64.Props;
import lando.systems.ld60.game.combat.Strike;
import lando.systems.ld60.game.components.BrawlerAnimClips;
import lando.systems.ld60.game.components.Fighter;
import lando.systems.ld60.game.components.Hover;
import lando.systems.ld60.game.components.Hurtable;
import lando.systems.ld60.game.movement.BrawlerMotion;

/**
 * The N64 controller boss ({@link N64}): floats about the street facing the player, jabs with
 * the joystick in reach, and every so often spins round, arms the rumble pak on its back with a
 * building shudder, and fires it at where the player is standing. A heavy hit (the player's
 * power swing, a pipe) during the spin-up knocks it out of it; a plain hit staggers it only every
 * {@link Props#staggerEvery} hits. Its {@code Hurtable} ({@link N64#ROLE}) requests {@link #HURT}
 * through {@link #onHurt} and {@link #DEAD} when its health runs out.
 * <p>
 * The states steer through {@code Input} like any brain, play their clips as {@code Fighter.move}
 * so the {@code BrawlerPresentSystem} leaves them alone, and write the float to the motion's
 * height axis each tick ({@link #hover}).
 */
public enum N64BossState implements State<Entity> {
    /** Floats toward punching range of the nearest player and faces it; jabs in reach, spins up the special on its rest timer. */
    IDLE {
        @Override
        public void enter(StateMachine<Entity> m, Entity e) {
            if (m.previous() == null) rest(e); // at spawn; after that the rest is re-armed where the special ends (SHOOT, or the knock out of it)
        }

        @Override
        public State<Entity> update(StateMachine<Entity> m, Entity e, float dt) {
            hover(e, dt);
            var input  = Input.get(e);
            var motion = motion(e);
            var timers = Timers.get(e);
            var player = Senses.nearestPlayerTo(e);
            if (player == null) {
                input.moveX = 0;
                input.moveY = 0;
                return this;
            }
            var pos  = Position.get(e);
            var tPos = Position.get(player);
            int dx = tPos.x - pos.x;
            int dy = tPos.y - pos.y;

            // close in to standoff distance along the street (never backs off) and onto the player's line; face it while standing
            input.moveX = (Math.abs(dx) > Props.standoff + Props.settle) ? Integer.signum(dx) : 0;
            input.moveY = (Math.abs(dy) > Props.depth / 2) ? Integer.signum(dy) : 0;
            if (input.moveX == 0 && dx != 0) motion.facing = Integer.signum(dx);

            if (!timers.has(SPECIAL_REST)) return SPIN;
            var inReach = Math.abs(dx) <= Props.reach && Math.abs(dy) <= Props.depth;
            if (inReach && !timers.has(PUNCH_REST)) return PUNCH;
            return this;
        }
    },
    /** The joystick jab, through; the hit box is the clip's window. */
    PUNCH {
        @Override
        public void enter(StateMachine<Entity> m, Entity e) {
            Mover.get(e).stop();
            begin(e, Props.punchMove);
        }

        @Override
        public State<Entity> update(StateMachine<Entity> m, Entity e, float dt) {
            hover(e, dt);
            hold(e);
            if (Props.punchMove.playing(e)) return this;
            Timers.get(e).after(PUNCH_REST, MathUtils.random(Props.punchRestMin, Props.punchRestMax), rest -> {});
            return IDLE;
        }

        @Override
        public void exit(StateMachine<Entity> m, Entity e) {
            end(e);
        }

        @Override
        public boolean interruptible(StateMachine<Entity> m, Entity e, State<Entity> by) {
            return by == DEAD;
        }
    },
    /** Turns its back on the player (the rumble pak comes round); a heavy hit here knocks it back out ({@link #onHurt}). */
    SPIN {
        @Override
        public void enter(StateMachine<Entity> m, Entity e) {
            Mover.get(e).stop();
            begin(e, Props.spinMove);
            AudioEvent.playSound(SoundType.SWING1, 0.75f); // TODO: a spin-up sound
        }

        @Override
        public State<Entity> update(StateMachine<Entity> m, Entity e, float dt) {
            hover(e, dt);
            hold(e);
            return Props.spinMove.playing(e) ? this : ARMED;
        }

        @Override
        public void exit(StateMachine<Entity> m, Entity e) {
            end(e);
        }

        @Override
        public boolean interruptible(StateMachine<Entity> m, Entity e, State<Entity> by) {
            return by == HURT || by == DEAD;
        }
    },
    /** Pak out, shuddering harder and harder for a random moment, then fires. Still open to a heavy hit. */
    ARMED {
        @Override
        public void enter(StateMachine<Entity> m, Entity e) {
            Mover.get(e).stop();
            begin(e, Props.armedMove);
            Timers.get(e).after(FIRE, MathUtils.random(Props.armedHoldMin, Props.armedHoldMax), fire -> {});
        }

        @Override
        public State<Entity> update(StateMachine<Entity> m, Entity e, float dt) {
            hover(e, dt);
            hold(e);
            var fire = Timers.get(e).get(FIRE);
            if (fire == null) return SHOOT;
            shake(e, 1f - fire.remainingSeconds() / fire.durationSeconds());
            return this;
        }

        @Override
        public void exit(StateMachine<Entity> m, Entity e) {
            end(e);
            shake(e, 0);
            Timers.get(e).cancel(FIRE);
        }

        @Override
        public boolean interruptible(StateMachine<Entity> m, Entity e, State<Entity> by) {
            return by == HURT || by == DEAD;
        }
    },
    /** The shot: the pak leaves on the muzzle flash frame ({@link N64#shoot}), the clip turns the boss back round. */
    SHOOT {
        @Override
        public void enter(StateMachine<Entity> m, Entity e) {
            Mover.get(e).stop();
            begin(e, Props.shootMove);
            Timers.get(e).after(LAUNCH, Props.launchTime, launch -> N64.shoot(launch.entity()));
        }

        @Override
        public State<Entity> update(StateMachine<Entity> m, Entity e, float dt) {
            hover(e, dt);
            hold(e);
            return Props.shootMove.playing(e) ? this : IDLE;
        }

        @Override
        public void exit(StateMachine<Entity> m, Entity e) {
            end(e);
            Timers.get(e).cancel(LAUNCH);
            rest(e);
        }

        @Override
        public boolean interruptible(StateMachine<Entity> m, Entity e, State<Entity> by) {
            return by == DEAD;
        }
    },
    /** Staggered: the hurt clip through, then back to it. */
    HURT {
        @Override
        public void enter(StateMachine<Entity> m, Entity e) {
            Mover.get(e).stop();
            begin(e, Props.hurtMove);
            if (m.previous() == SPIN || m.previous() == ARMED) rest(e); // knocked out of the special: it starts over after a rest
        }

        @Override
        public State<Entity> update(StateMachine<Entity> m, Entity e, float dt) {
            hover(e, dt);
            hold(e);
            return Props.hurtMove.playing(e) ? this : IDLE;
        }

        @Override
        public void exit(StateMachine<Entity> m, Entity e) {
            end(e);
        }

        @Override
        public boolean interruptible(StateMachine<Entity> m, Entity e, State<Entity> by) {
            return by == DEAD;
        }
    },
    /** Health ran out: drops out of the air onto the street; the present system plays the death clip and holds it. */
    DEAD {
        @Override
        public void enter(StateMachine<Entity> m, Entity e) {
            Mover.get(e).stop();
            Input.get(e).reset();
            end(e);
            shake(e, 0);
            var timers = Timers.get(e);
            timers.cancel(FIRE);
            timers.cancel(LAUNCH);
            timers.cancel(SPECIAL_REST);
            timers.cancel(PUNCH_REST);
            motion(e).grounded = false; // falls from its hover under the motion's gravity
            var clips = BrawlerAnimClips.get(e);
            if (clips != null) clips.deadOrHurt().start(e, false); // now, not after the move it was in plays out
        }

        @Override
        public boolean interruptible(StateMachine<Entity> m, Entity e, State<Entity> by) {
            return false;
        }
    };

    @Override
    public State<Entity> update(StateMachine<Entity> m, Entity e, float dt) {
        return this;
    }

    // Timer names: the rests between moves, the armed hold, and the shot's launch frame
    static final String SPECIAL_REST = "special";
    static final String PUNCH_REST   = "punch";
    static final String FIRE         = "fire";
    static final String LAUNCH       = "launch";

    /**
     * The boss's {@code Hurtable.onHurt}: a heavy hit (one of {@link Props#heavyHits}) during the
     * spin-up knocks it out of it; otherwise every {@link Props#staggerEvery}th hit staggers it while
     * it is idling. A punch, a shot or a stagger in progress is not interrupted.
     */
    public static void onHurt(Entity e, Strike strike) {
        var behavior = Behavior.get(e);
        if (behavior == null) return;
        var heavy = Props.heavyHits.contains(strike.hit().name());
        if (behavior.is(SPIN) || behavior.is(ARMED)) {
            if (heavy) behavior.request(HURT);
        } else if (behavior.is(IDLE) && Hurtable.get(e).hits % Props.staggerEvery == 0) {
            behavior.request(HURT);
        }
    }

    /** Arms the rest before the next special; {@link #IDLE} spins up when it has run out. */
    static void rest(Entity e) {
        Timers.get(e).after(SPECIAL_REST, MathUtils.random(Props.specialRestMin, Props.specialRestMax), rest -> {});
    }

    static BrawlerMotion motion(Entity e) {
        return Mover.get(e).motion(BrawlerMotion.class);
    }

    /** The float: this tick's hover height onto the motion's height axis, unless the motion owns it (airborne, or dropped dead). */
    static void hover(Entity e, float dt) {
        var hover  = Hover.get(e);
        var motion = motion(e);
        if (hover == null || !motion.grounded) return;
        motion.z = hover.advance(dt);
    }

    /** Starts {@code move} as the fighter's move in progress, so the present system keeps off its clip until {@link #end}. */
    static void begin(Entity e, Move move) {
        var fighter = Fighter.get(e);
        if (fighter != null) fighter.move = move;
        move.start(e);
    }

    static void end(Entity e) {
        var fighter = Fighter.get(e);
        if (fighter != null) fighter.move = null;
    }

    /** Stands still: the steer written last tick must not carry through a move. */
    static void hold(Entity e) {
        var input = Input.get(e);
        input.moveX = 0;
        input.moveY = 0;
        input.actionPressed = false;
    }

    /** Shudders the art sideways by up to {@link Props#shakeMax} region pixels at {@code intensity} 0..1; 0 puts it back. */
    static void shake(Entity e, float intensity) {
        var anim = Animator.get(e);
        if (anim == null) return;
        anim.origin.x = Props.originX + ((intensity > 0) ? MathUtils.random(-1f, 1f) * intensity * Props.shakeMax : 0);
    }
}
