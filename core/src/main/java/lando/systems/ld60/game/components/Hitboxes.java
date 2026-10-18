package lando.systems.ld60.game.components;

import com.badlogic.gdx.utils.Array;
import com.zendogames.zenlib.collision.CollisionMask;
import com.zendogames.zenlib.ecs.Component;
import com.zendogames.zenlib.ecs.ComponentType;
import com.zendogames.zenlib.ecs.Entity;
import lando.systems.ld60.game.combat.Hitbox;
import lando.systems.ld60.game.combat.HitboxSet;
import lando.systems.ld60.game.combat.Strike;
import lando.systems.ld60.game.systems.HitSystem;

/**
 * A fighter's hit and hurt shapes, separate from its {@code Collider}: the collider stays the feet
 * that walls stop, these are the body that gets hit and the fists that do the hitting. The
 * {@link HitSystem} resolves {@link #active} every tick from the {@link #set} and the clip the
 * entity's {@code SpineAnimator} (track 0) or {@code Animator} is playing, plus anything added here
 * from code, then reports every new hit as a {@link Strike}.
 * <pre>{@code
 * entity.add(new Hitboxes(GameHitboxes.SPINEBOY, GameMask.PLAYER, GameMask.ENEMY)); // is PLAYER, hits ENEMY
 * Hitboxes.get(e).add(Hitbox.hit("shockwave", rect(-24, 0, 48, 8), QUAKE).depth(16), 0.15f);
 * Hitboxes.get(e).vulnerable = false;                                                 // i-frames
 * }</pre>
 * {@link #facing} is copied from the animator each tick (an entity without one keeps what code
 * sets). {@link #lift} is the fighter's height above its floor line, which code that moves the art
 * up writes (a jump's {@code z}); every box rises with it while the floor line stays put.
 */
public class Hitboxes implements Component {

    public static final ComponentType<Hitboxes> TYPE = ComponentType.register(Hitboxes.class);
    public static Hitboxes get(Entity e) { return TYPE.get(e); }
    public static boolean  has(Entity e) { return TYPE.has(e); }

    /** One box out this tick: where it came from, where it sits, and what it touched. Read-only for game code. */
    public static final class Active {
        public Hitbox box;

        /** What put it out: the {@link Hitbox} itself for a base box, its {@link HitboxSet.Window}, or its {@link Timed}. */
        public Object source;

        /** Offset of the box's anchor from the feet, pixels: the bone's position for a bone box, else zero. */
        public int x, y;

        /** Overlapped an opposing box this tick. */
        public boolean touching;

        /** HIT only: its source has struck someone since it opened. */
        public boolean connected;
    }

    /** A box added from code, out for {@link #remaining} more seconds (infinite until removed). */
    public static final class Timed {
        public final Hitbox box;
        public float remaining;

        Timed(Hitbox box, float seconds) {
            this.box = box;
            this.remaining = seconds;
        }
    }

    /** A target a source has already struck; forgotten when the source closes or the target dies. */
    public record Struck(Object source, Entity target) {}

    /** The authored boxes; null for an entity that only has boxes added from code. */
    public HitboxSet set;

    /** What this fighter is: other fighters whose {@link #targets} include it can hit it. */
    public final CollisionMask team;

    /** Bit set of the teams this fighter's HIT boxes can hit. */
    public final int targets;

    /** False: its HURT boxes are skipped (and drawn grey), for i-frames, a knockdown, a death. */
    public boolean vulnerable = true;

    /** -1 or +1; copied from the {@code SpineAnimator} or {@code Animator} each tick when there is one. */
    public int facing = 1;

    /** Height above the floor line in pixels; every box is raised by it. */
    public float lift;

    /** This tick's boxes in resolve order (base, windows, timed), one per name. */
    public final Array<Active> active = new Array<>(8);

    /** The clip and time the boxes were resolved for this tick, null when nothing is playing. */
    public String clip;
    public float clipTime;

    /** The track entry or animation the clip time came from, to see a restart; {@code HitSystem}'s alone. */
    public Object clipEntry;

    public final Array<Timed>  timed  = new Array<>(4);
    public final Array<Struck> struck = new Array<>(4);
    private final Array<Active> spare = new Array<>(8);

    public Hitboxes(HitboxSet set, CollisionMask team, CollisionMask... targets) {
        this.set = set;
        this.team = team;
        this.targets = CollisionMask.of(targets);
    }

    // ------------------------------------------------------------------------
    // Boxes from code: temporary regions, held weapons, effects
    // ------------------------------------------------------------------------

    /** Puts {@code box} out from the next {@code HitSystem} update until {@link #remove}; it replaces a box of the same name. */
    public Timed add(Hitbox box) {
        return add(box, Float.POSITIVE_INFINITY);
    }

    /** Puts {@code box} out from the next {@code HitSystem} update for {@code seconds}; it replaces a box of the same name. */
    public Timed add(Hitbox box, float seconds) {
        var entry = new Timed(box, seconds);
        timed.add(entry);
        return entry;
    }

    /** Takes back every box added from code under {@code name}. */
    public void remove(String name) {
        for (int i = timed.size - 1; i >= 0; i--) {
            if (timed.get(i).box.name().equals(name)) {
                timed.removeIndex(i);
            }
        }
    }

    /** Forgets every target struck so far, so the boxes out now can hit them again: a multi-hit move driven from code. */
    public void rearm() {
        struck.clear();
    }

    // ------------------------------------------------------------------------
    // Queries over this tick's boxes
    // ------------------------------------------------------------------------

    /** The box out under {@code name} this tick, null when there is none. */
    public Active find(String name) {
        for (int i = 0; i < active.size; i++) {
            var slot = active.get(i);
            if (slot.box.name().equals(name)) return slot;
        }
        return null;
    }

    /** True when a box named {@code name} is out this tick. */
    public boolean isOut(String name) {
        return find(name) != null;
    }

    /** True when a HIT box is out this tick. */
    public boolean isAttacking() {
        for (int i = 0; i < active.size; i++) {
            if (active.get(i).box.isHit()) return true;
        }
        return false;
    }

    // ------------------------------------------------------------------------
    // HitSystem bookkeeping
    // ------------------------------------------------------------------------

    public void clearActive() {
        spare.addAll(active);
        active.clear();
    }

    public Active obtain() {
        return spare.isEmpty() ? new Active() : spare.pop();
    }

    public boolean hasStruck(Object source, Entity target) {
        for (int i = 0; i < struck.size; i++) {
            var s = struck.get(i);
            if (s.source() == source && s.target() == target) return true;
        }
        return false;
    }

    public boolean hasStruckAny(Object source) {
        for (int i = 0; i < struck.size; i++) {
            if (struck.get(i).source() == source) return true;
        }
        return false;
    }

    public boolean isSourceOut(Object source) {
        for (int i = 0; i < active.size; i++) {
            if (active.get(i).source == source) return true;
        }
        return false;
    }
}
