package lando.systems.ld60.game.state;

import com.badlogic.gdx.math.MathUtils;
import com.zendogames.zenlib.ecs.Entity;
import com.zendogames.zenlib.ecs.components.*;
import com.zendogames.zenlib.state.State;
import com.zendogames.zenlib.state.StateMachine;
import lando.systems.ld60.assets.AnimAtari;
import lando.systems.ld60.assets.SoundType;
import lando.systems.ld60.events.AudioEvent;
import lando.systems.ld60.game.ai.Senses;
import lando.systems.ld60.game.anim.Move;
import lando.systems.ld60.game.characters.Atari;
import lando.systems.ld60.game.characters.Atari.Props;
import lando.systems.ld60.game.combat.Strike;
import lando.systems.ld60.game.components.Fighter;
import lando.systems.ld60.game.components.Hurtable;
import lando.systems.ld60.game.movement.BrawlerMotion;

/**
 * The Atari joystick boss ({@link Atari}): stomps up to poking range of the player and faces it,
 * pokes with the stick when it is in reach, and whips the cord round on its rest timer whenever
 * the player is within the cord's longer reach. A heavy hit (the player's power swing, a pipe)
 * staggers it at once and knocks it out of a whip; a plain hit staggers it only every
 * {@link Props#staggerEvery} hits. Its {@code Hurtable} ({@link Atari#ROLE}) requests {@link #HURT}
 * through {@link #onHurt} and {@link #DEAD} when its health runs out.
 * <p>
 * The states steer through {@code Input} like any brain and play their clips as
 * {@code Fighter.move} so the {@code BrawlerPresentSystem} leaves them alone; the system's own
 * idle / walk pick is what plays otherwise, and {@link #dance} rides on top of the walk: an extra
 * hop on the height axis and a re-anchoring of the frames that carry the body sideways.
 */
public enum AtariBossState implements State<Entity> {
    /** Walks toward poking range of the nearest player and faces it; pokes in reach, whips when rested and in the cord's reach. */
    IDLE {
        @Override
        public void enter(StateMachine<Entity> m, Entity e) {
            if (m.previous() == null) rest(e); // at spawn; after that the rest is re-armed where a whip ends
        }

        @Override
        public State<Entity> update(StateMachine<Entity> m, Entity e, float dt) {
            dance(e, dt);
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

            var inDepth = Math.abs(dy) <= Props.depth;
            if (inDepth && Math.abs(dx) <= Props.whipReach && !timers.has(WHIP_REST)) return WHIP;
            if (inDepth && Math.abs(dx) <= Props.pokeReach && !timers.has(POKE_REST)) return POKE;
            return this;
        }

        @Override
        public void exit(StateMachine<Entity> m, Entity e) {
            settle(e);
        }
    },
    /** The stick poke, through; the hit box is the clip's window (the two level frames). */
    POKE {
        @Override
        public void enter(StateMachine<Entity> m, Entity e) {
            hold(e);
            Mover.get(e).stop();
            begin(e, Props.pokeMove);
        }

        @Override
        public State<Entity> update(StateMachine<Entity> m, Entity e, float dt) {
            hold(e);
            if (Props.pokeMove.playing(e)) return this;
            Timers.get(e).after(POKE_REST, MathUtils.random(Props.pokeRestMin, Props.pokeRestMax), rest -> {});
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
    /** The cord whipped round, through; the hit box is the clip's window (the two lashing frames). A heavy hit knocks it out ({@link #onHurt}). */
    WHIP {
        @Override
        public void enter(StateMachine<Entity> m, Entity e) {
            hold(e);
            Mover.get(e).stop();
            begin(e, Props.whipMove);
            AudioEvent.playSound(SoundType.SWING1); // TODO: a whip crack
        }

        @Override
        public State<Entity> update(StateMachine<Entity> m, Entity e, float dt) {
            hold(e);
            return Props.whipMove.playing(e) ? this : IDLE;
        }

        @Override
        public void exit(StateMachine<Entity> m, Entity e) {
            end(e);
            rest(e);
        }

        @Override
        public boolean interruptible(StateMachine<Entity> m, Entity e, State<Entity> by) {
            return by == HURT || by == DEAD;
        }
    },
    /** Staggered: no art for it, so it stands and shudders for {@link Props#staggerTime}, then back to it. */
    HURT {
        @Override
        public void enter(StateMachine<Entity> m, Entity e) {
            hold(e);
            Mover.get(e).stop();
            settle(e);
            Timers.get(e).after(STAGGER, Props.staggerTime, stagger -> {});
            AudioEvent.playSound(SoundType.BELL_LOW); // TODO: a stagger sound
        }

        @Override
        public State<Entity> update(StateMachine<Entity> m, Entity e, float dt) {
            hold(e);
            var stagger = Timers.get(e).get(STAGGER);
            if (stagger == null) return IDLE;
            shake(e, stagger.remainingSeconds() / stagger.durationSeconds()); // hardest at the hit, easing off
            return this;
        }

        @Override
        public void exit(StateMachine<Entity> m, Entity e) {
            shake(e, 0);
            Timers.get(e).cancel(STAGGER);
        }

        @Override
        public boolean interruptible(StateMachine<Entity> m, Entity e, State<Entity> by) {
            return by == DEAD;
        }
    },
    /** Health ran out: squashes flat onto the street (no death art: the animator eases to {@link Props#deathSquashX} x {@link Props#deathSquashY}); the body stays a while. */
    DEAD {
        @Override
        public void enter(StateMachine<Entity> m, Entity e) {
            Mover.get(e).stop();
            Input.get(e).reset();
            end(e);
            settle(e);
            var timers = Timers.get(e);
            timers.cancel(STAGGER);
            timers.cancel(WHIP_REST);
            timers.cancel(POKE_REST);
            var anim = Animator.get(e);
            if (anim != null) {
                anim.defaultScale.set(Props.scale * Props.deathSquashX, Props.scale * Props.deathSquashY); // the animation system eases the scale there
                anim.scaleReturnSpeed = Props.deathSquashSpeed;
            }
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

    // Timer names: the rests between moves, and the stagger
    static final String WHIP_REST = "whip";
    static final String POKE_REST = "poke";
    static final String STAGGER   = "stagger";

    /**
     * The boss's {@code Hurtable.onHurt}: a heavy hit (one of {@link Props#heavyHits}) staggers it
     * at once, out of a whip too; otherwise every {@link Props#staggerEvery}th hit staggers it while
     * it is idling. A poke or a stagger in progress is not interrupted.
     */
    public static void onHurt(Entity e, Strike strike) {
        var behavior = Behavior.get(e);
        if (behavior == null) return;
        var heavy = Props.heavyHits.contains(strike.hit().name());
        if (behavior.is(WHIP)) {
            if (heavy) behavior.request(HURT);
        } else if (behavior.is(IDLE) && (heavy || Hurtable.get(e).hits % Props.staggerEvery == 0)) {
            behavior.request(HURT);
        }
    }

    /** Arms the rest before the next whip; {@link #IDLE} whips when it has run out and the player is in the cord's reach. */
    static void rest(Entity e) {
        Timers.get(e).after(WHIP_REST, MathUtils.random(Props.whipRestMin, Props.whipRestMax), rest -> {});
    }

    static BrawlerMotion motion(Entity e) {
        return Mover.get(e).motion(BrawlerMotion.class);
    }

    /**
     * The dance, while the walk clip plays: {@link Props#danceHeight} more hop on the motion's height
     * axis ({@link Props#danceBeats} hops per walk cycle, on top of the hop drawn in the frames), and
     * the art re-anchored by {@link Props#walkDrift} for the frame showing, so the body stays over the
     * feet through the step's lunge. Standing, both are put back.
     * <p>
     * States run before the {@code AnimationSystem} advances the clip by {@code dt} and picks its
     * frame, so both are read at {@code time + dt}: the frame this tick will draw, not the last one
     * (anchoring for the last one pops the art for a tick at every frame change).
     */
    static void dance(Entity e, float dt) {
        var anim   = Animator.get(e);
        var motion = motion(e);
        if (anim == null || !motion.grounded) return;
        if (!anim.is(AnimAtari.WALK) || !anim.hasAnimation()) {
            settle(e);
            return;
        }
        var time     = anim.time + dt;
        var duration = anim.animation.getAnimationDuration();
        var phase    = (duration > 0) ? (time % duration) / duration : 0; // 0..1 along the walk cycle
        motion.z = Props.danceHeight * Math.abs(MathUtils.sin(MathUtils.PI * Props.danceBeats * phase));

        int frame = anim.animation.getKeyFrameIndex(time);
        anim.origin.x = Props.originX + ((frame < Props.walkDrift.length) ? Props.walkDrift[frame] : 0);
    }

    /** Feet flat on the floor, art anchored at its center column: the state of the art between dances and shudders. */
    static void settle(Entity e) {
        var motion = motion(e);
        if (motion.grounded) motion.z = 0;
        var anim = Animator.get(e);
        if (anim != null) anim.origin.x = Props.originX;
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

    /** Stands still: the steer {@link #IDLE} wrote this tick (or last) must not carry into a move. */
    static void hold(Entity e) {
        var input = Input.get(e);
        input.moveX = 0;
        input.moveY = 0;
        input.actionPressed = false;
    }

    /** Shudders the art sideways by up to {@link Props#shakeMax} frame pixels at {@code intensity} 0..1; 0 puts it back. */
    static void shake(Entity e, float intensity) {
        var anim = Animator.get(e);
        if (anim == null) return;
        anim.origin.x = Props.originX + ((intensity > 0) ? MathUtils.random(-1f, 1f) * intensity * Props.shakeMax : 0);
    }
}
