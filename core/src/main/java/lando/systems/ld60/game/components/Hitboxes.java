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
 * entity's {@code Animator} or {@code SpineAnimator} (track 0) is playing, then reports every new
 * hit as a {@link Strike}.
 * <pre>{@code
 * entity.add(new Hitboxes(GameHitboxes.SPINEBOY, GameMask.PLAYER, GameMask.ENEMY)); // is PLAYER, hits ENEMY
 * Hitboxes.get(e).vulnerable = false;                                               // i-frames
 * }</pre>
 * {@link #facing} is copied from the animator each tick (an entity without one keeps what code
 * sets). {@link #lift} is the fighter's height above its floor line, which code that moves the art
 * up writes (a jump's {@code z}); every box rises with it while the floor line stays put.
 */
public class Hitboxes implements Component {

    public static final ComponentType<Hitboxes> TYPE = ComponentType.register(Hitboxes.class);
    public static Hitboxes get(Entity e) { return TYPE.get(e); }
    public static boolean  has(Entity e) { return TYPE.has(e); }

    /** One box out this tick: where it sits and whether it touched an opposing box. Read-only for game code. */
    public static final class Active {
        public Hitbox box;

        /** Offset of the box's anchor from the feet, pixels: the bone's position for a bone box, else zero. */
        public int x, y;

        /** Overlapped an opposing box this tick. */
        public boolean touching;
    }

    /** The authored boxes; swap it to change the whole table (a weapon pickup). */
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

    /** This tick's boxes in resolve order (base, then the open windows), one per name. */
    public final Array<Active> active = new Array<>(8);

    /**
     * Targets the boxes out now have struck. One activation strikes a target once: the list is
     * forgotten when the clip changes, restarts or loops, or a window opens or closes.
     */
    public final Array<Entity> struck = new Array<>(4);

    /** The clip and time the boxes were resolved for this tick, null when nothing is playing. */
    public String clip;
    public float clipTime;

    /** {@code HitSystem}'s: the track entry or animation the time came from, and the bit set of the clip's open windows, to see a new activation. */
    public Object clipEntry;
    public int open;

    private final Array<Active> spare = new Array<>(8);

    public Hitboxes(HitboxSet set, CollisionMask team, CollisionMask... targets) {
        this.set = set;
        this.team = team;
        this.targets = CollisionMask.of(targets);
    }

    /** The box out under {@code name} this tick, null when there is none. */
    public Active find(String name) {
        for (int i = 0; i < active.size; i++) {
            var slot = active.get(i);
            if (slot.box.name().equals(name)) return slot;
        }
        return null;
    }

    /** True when a HIT box is out this tick. */
    public boolean isAttacking() {
        for (int i = 0; i < active.size; i++) {
            if (active.get(i).box.isHit()) return true;
        }
        return false;
    }

    /** Forgets every target struck so far, so the boxes out now can hit them again: a multi-hit move driven from code. */
    public void rearm() {
        struck.clear();
    }

    // HitSystem bookkeeping: slots are pooled so a tick allocates nothing

    public void clearActive() {
        spare.addAll(active);
        active.clear();
    }

    public Active obtain() {
        return spare.isEmpty() ? new Active() : spare.pop();
    }
}
