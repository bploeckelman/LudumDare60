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
    /** Walks, jumps (the motion's action), attacks on attack, picks up on down + attack, throws on special. */
    NORMAL {
        @Override
        public State<Entity> update(StateMachine<Entity> m, Entity e, float dt) {
            var motion = Mover.get(e).motion(BrawlerMotion.class);
            if (motion.launched) return HURT; // thrown by something that did not ask (eg. a Hurtable without hurtState)

            var buttons = GameInput.get(e);
            if (buttons.attack.pressed) {
                if (Input.get(e).moveY < 0) { // down + attack: pickup what is underfoot, else a crouching swing is just a swing
                    var item = Weapons.under(e);
                    if (item != null && Weapons.pickUp(e, item)) {
                        return this;
                    }
                }
                return (attackOf(e) != null) ? ATTACK : this;
            }
            if (buttons.special.pressed && Weapons.throwHeld(e) != null) {
                return THROW;
            }
            return this;
        }
    },
    /** Stands and plays the held weapon's attack through; the hit boxes come from its windows. */
    ATTACK {
        @Override
        public void enter(StateMachine<Entity> m, Entity e) {
            Mover.get(e).stop();
            attackOf(e).start(e);
        }

        @Override
        public State<Entity> update(StateMachine<Entity> m, Entity e, float dt) {
            hold(e);
            return attackOf(e).playing(e) ? this : NORMAL;
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
