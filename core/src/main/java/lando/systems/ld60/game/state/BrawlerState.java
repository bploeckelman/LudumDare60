package lando.systems.ld60.game.state;

import com.zendogames.zenlib.ecs.Entity;
import com.zendogames.zenlib.ecs.components.Animator;
import com.zendogames.zenlib.ecs.components.Input;
import com.zendogames.zenlib.ecs.components.Mover;
import com.zendogames.zenlib.ecs.components.SpineAnimator;
import com.zendogames.zenlib.state.State;
import com.zendogames.zenlib.state.StateMachine;
import lando.systems.ld60.game.anim.AnimClip;
import lando.systems.ld60.game.anim.Move;
import lando.systems.ld60.game.combat.Weapons;
import lando.systems.ld60.game.components.BrawlerAnimClips;
import lando.systems.ld60.game.components.Fighter;
import lando.systems.ld60.game.components.GameInput;
import lando.systems.ld60.game.components.Hurtable;
import lando.systems.ld60.game.movement.BrawlerMotion;

/**
 * The player's brawler: what the buttons do and what the fighter refuses while busy.
 * Everything the motion already knows (walk, run, jump, knockback) is animated by the
 * {@code BrawlerPresentSystem} from {@link BrawlerAnimClips}; these states only start
 * the moves the motion cannot know about, hold the controls while one plays, and own
 * the hurt and death. {@code GameStrikes} requests {@link #HURT} and {@link #DEAD}
 * through the fighter's {@link Hurtable}, so nothing here is reached for by name from
 * outside.
 */
public enum BrawlerState implements State<Entity> {
    /** Walks, jumps (the motion's action), attacks on a tap of attack, picks up on a tap over an item, kicks on down + tap, winds up the power move on attack held, throws on special. */
    NORMAL {
        @Override
        public State<Entity> update(StateMachine<Entity> m, Entity e, float dt) {
            var motion = Mover.get(e).motion(BrawlerMotion.class);
            if (motion.launched) return HURT; // thrown by something that did not ask (eg. a Hurtable without hurtState)

            var buttons = GameInput.get(e);
            var wield   = Fighter.wieldOf(e);
            var power   = (wield != null) ? wield.power() : null;
            if (power != null && buttons.attack.held && buttons.attack.heldTime >= POWER_HOLD) { // held on past a tap: wind up, swing on the release
                return CHARGE;
            }
            var tap = (power != null) ? buttons.attack.released : buttons.attack.pressed; // a wield with a hold move acts on the release
            if (tap) {
                var item = Weapons.under(e); // attack over an item: pick it up instead of swinging
                if (item != null && Weapons.pickUp(e, item)) {
                    return this;
                }
                if (Input.get(e).moveY < 0 && wield != null && wield.low() != null) { // down + attack: the low move (a kick) if this wield has one
                    return begin(e, wield.low());
                }
                var move = attackOf(e);
                return (move != null) ? begin(e, move) : this;
            }
            if (buttons.special.pressed && Weapons.throwHeld(e) != null) {
                return THROW;
            }
            return this;
        }
    },
    /**
     * Attack held past {@link #POWER_HOLD}: the wield's power move wound up and parked at its wind-up,
     * left / right still turns the fighter to aim it, and the release lets it go ({@link #ATTACK} plays
     * the rest of the clip). Only the release leaves, apart from a hit.
     */
    CHARGE {
        @Override
        public void enter(StateMachine<Entity> m, Entity e) {
            Mover.get(e).stop();
            var wield = Fighter.wieldOf(e);
            if (wield != null && wield.power() != null) {
                wield.power().move().start(e);
                AnimClip.freeze(e, wield.power().windup());
            }
        }

        @Override
        public State<Entity> update(StateMachine<Entity> m, Entity e, float dt) {
            var motion = Mover.get(e).motion(BrawlerMotion.class);
            if (motion.launched) return HURT;

            var input = Input.get(e);
            if (input.moveX != 0) motion.facing = Integer.signum(input.moveX); // aim: turn, no walking
            var released = GameInput.get(e).attack.released;
            hold(e);
            if (!released) return this;

            var wield = Fighter.wieldOf(e);
            if (wield == null || wield.power() == null) return NORMAL;
            AnimClip.resume(e);
            return begin(e, wield.power().move()); // ATTACK plays the rest of the clip
        }

        /**
         * Any way out but the release (a hit, death) drops the parked clip: the present system
         * animates nothing while a move is under way, and a parked one never ends.
         */
        @Override
        public void exit(StateMachine<Entity> m, Entity e) {
            var fighter = Fighter.get(e);
            if (fighter != null && fighter.move != null) return; // the release handed the clip to ATTACK
            AnimClip.clear(e, 0.1f);
        }
    },
    /** Stands and plays the chosen move ({@code Fighter.move}) through; the hit boxes come from its windows. */
    ATTACK {
        @Override
        public void enter(StateMachine<Entity> m, Entity e) {
            Mover.get(e).stop();
            var move = current(e);
            // unless it is already under way ({@link #CHARGE} wound it up)
            if (move != null && !move.playing(e)) {
                move.start(e);
            }
        }

        @Override
        public State<Entity> update(StateMachine<Entity> m, Entity e, float dt) {
            hold(e);
            var move = current(e);
            return (move != null && move.playing(e)) ? this : NORMAL;
        }

        @Override
        public void exit(StateMachine<Entity> m, Entity e) {
            // clear the move so it can't outlive its state
            var fighter = Fighter.get(e);
            if (fighter != null) fighter.move = null;
        }
    },
    /** The weapon left the hand on the way in ({@code NORMAL}); plays the throw clip (the attack's) through. */
    THROW {
        @Override
        public void enter(StateMachine<Entity> m, Entity e) {
            Mover.get(e).stop();
            var clips = BrawlerAnimClips.get(e);
            if (clips != null && clips.throwing != null) {
                clips.throwing.start(e, false);
            } else {
                var move = attackOf(e);
                if (move != null) {
                    move.clip().start(e, false);
                }
            }
        }

        @Override
        public State<Entity> update(StateMachine<Entity> m, Entity e, float dt) {
            hold(e);
            return AnimClip.complete(e) ? NORMAL : this;
        }
    },
    /** Thrown by a hit (the motion was launched by {@code GameStrikes}): no controls until it lands and slides to rest. */
    HURT {
        @Override
        public void enter(StateMachine<Entity> m, Entity e) {
            squash(e, 1.2f, 0.8f);
        }

        @Override
        public State<Entity> update(StateMachine<Entity> m, Entity e, float dt) {
            hold(e);
            return Mover.get(e).motion(BrawlerMotion.class).launched ? this : NORMAL;
        }

        @Override
        public boolean interruptible(StateMachine<Entity> m, Entity e, State<Entity> by) {
            return by == DEAD; // runs its course; a second hit re-launches the mover without re-entering
        }
    },
    /** Health ran out: controls off for good; the present system plays the death clip and holds it. */
    DEAD {
        @Override
        public void enter(StateMachine<Entity> m, Entity e) {
            Mover.get(e).stop();
            Input.get(e).reset();
            GameInput.get(e).reset();
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

    /** Attack held this long winds up the wield's power move ({@link #CHARGE}); released sooner, it is a tap (the jab, the kick, a pickup). */
    public static final float POWER_HOLD = 0.3f;

    /** Records {@code move} as the one {@link #ATTACK} plays. */
    static State<Entity> begin(Entity e, Move move) {
        var fighter = Fighter.get(e);
        if (fighter != null) fighter.move = move;
        return ATTACK;
    }

    /** The move in progress, else the basic attack (an entity without a {@code Fighter}). */
    static Move current(Entity e) {
        var fighter = Fighter.get(e);
        return (fighter != null && fighter.move != null) ? fighter.move : attackOf(e);
    }

    /** The held weapon's attack, else the clips' basic attack, else null (a fighter that cannot attack). */
    static Move attackOf(Entity e) {
        var move = Weapons.attackOf(e);
        if (move == null && BrawlerAnimClips.has(e)) {
            move = BrawlerAnimClips.get(e).attack;
        }
        return move;
    }

    /**
     * The keyboard wrote {@code Input} and {@code GameInput} already (INPUT runs before DECIDE): stand still,
     * no jump, and no button edge for anything that reads them later this tick or on the way back to
     * {@link #NORMAL}, until the move is over. Held state stays, so a button held across the move
     * does not press again when it ends.
     */
    static void hold(Entity e) {
        var input = Input.get(e);
        input.moveX = 0;
        input.moveY = 0;
        input.actionPressed = false;
        var buttons = GameInput.get(e);
        if (buttons != null) {
            buttons.clearFrameEvents();
        }
    }

    static void squash(Entity e, float sx, float sy) {
        if (SpineAnimator.has(e)) { var a = SpineAnimator.get(e); a.scale.set(a.defaultScale.x * sx, a.defaultScale.y * sy); }
        else if (Animator.has(e)) { var a = Animator     .get(e); a.scale.set(a.defaultScale.x * sx, a.defaultScale.y * sy); }
    }
}
