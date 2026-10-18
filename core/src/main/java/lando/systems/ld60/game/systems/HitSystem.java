package lando.systems.ld60.game.systems;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.github.tommyettinger.gdcrux.PointI2;
import com.zendogames.zenlib.ecs.Entity;
import com.zendogames.zenlib.ecs.Family;
import com.zendogames.zenlib.ecs.IteratingSystem;
import com.zendogames.zenlib.ecs.SystemOrder;
import com.zendogames.zenlib.ecs.components.Animator;
import com.zendogames.zenlib.ecs.components.Name;
import com.zendogames.zenlib.ecs.components.Position;
import com.zendogames.zenlib.ecs.components.SpineAnimator;
import lando.systems.ld60.game.combat.Hitbox;
import lando.systems.ld60.game.combat.Strike;
import lando.systems.ld60.game.components.Hitboxes;

/**
 * Resolves every fighter's {@link Hitboxes} for the tick, finds HIT boxes overlapping opposing HURT
 * boxes, and hands each new hit to the {@link Strike.Responder}. Runs at {@code PRESENT + 10}, after
 * the {@code AnimationSystem} and {@code SpineSystem} have advanced the clips and posed the bones,
 * so the boxes match the frame that is drawn. All strikes are found first, then responded to in
 * order, so a trade (two fighters hitting each other on the same tick) reports both.
 * <p>
 * <b>One strike per target per activation.</b> An activation is the set of boxes out for one
 * stretch of a clip: it ends, and the struck targets are forgotten, when the clip changes, restarts
 * or loops, or a window opens or closes. So a window strikes each target once while it stays open
 * and a two-hit move is two windows. Of the target's HURT boxes, the first overlapped (resolve
 * order) is the one reported, so list a "head" before a "body" to let it win.
 * <p>
 * {@link #strikes} holds this tick's strikes for a debug view. Every pair of fighters is tested,
 * no broadphase, the {@code CollisionWorld} stance.
 */
public class HitSystem extends IteratingSystem {

    public static final int PRIORITY = SystemOrder.PRESENT + 10;
    private static final String TAG = HitSystem.class.getSimpleName();

    public Strike.Responder responder;

    /** This tick's strikes, in detection order; valid until the next update. */
    public final Array<Strike> strikes = new Array<>(16);

    // Scratch for the overlap test: body-plane positions, and the zero offset the shapes' overlaps() takes
    private final PointI2 planeA = new PointI2();
    private final PointI2 planeB = new PointI2();
    private final PointI2 zero   = new PointI2();

    public HitSystem(Strike.Responder responder) {
        super(Family.all(Position.TYPE, Hitboxes.TYPE), PRIORITY);
        this.responder = (responder != null) ? responder : Strike.Responder.NONE;
    }

    /** Every entity with {@link Position} and {@link Hitboxes}, a live view; what the overlay walks. */
    public Family fighters() {
        return family();
    }

    @Override
    public void update(float dt) {
        strikes.clear();
        super.update(dt); // resolve, one fighter at a time

        var fighters = family().entities();
        for (int i = 0; i < fighters.size; i++) {
            var attacker = fighters.get(i);
            if (!Hitboxes.get(attacker).isAttacking()) continue;
            for (int j = 0; j < fighters.size; j++) {
                if (i != j) detect(attacker, fighters.get(j));
            }
        }
        for (int i = 0; i < strikes.size; i++) {
            responder.strike(strikes.get(i));
        }
    }

    // ------------------------------------------------------------------------
    // Resolve: which boxes are out this tick, and where
    // ------------------------------------------------------------------------

    @Override
    protected void process(Entity e, float dt) {
        var boxes = Hitboxes.get(e);

        // What is playing: a sprite Animator, or Spine track 0; with neither only the base boxes are out
        String clip = null;
        Object entry = null;
        float time = 0;
        var sprite = Animator.get(e);
        var spine  = SpineAnimator.get(e);
        if (sprite != null) {
            boxes.facing = sprite.facing;
            if (sprite.hasAnimation()) {
                clip  = (sprite.type != null) ? sprite.type.name() : null;
                time  = clipTime(sprite.animation, sprite.time);
                entry = sprite.animation;
            }
        } else if (spine != null) {
            boxes.facing = spine.facing;
            var track = spine.state.getTrack(0);
            if (track != null) {
                clip  = track.getAnimation().getName();
                time  = track.getAnimationTime(); // wrapped for a loop, clamped at the end otherwise
                entry = track;
            }
        }

        // The boxes out now: base, then the windows of the clip that contain its time; a later box replaces an earlier one of its name
        boxes.clearActive();
        int open = 0;
        if (boxes.set != null) {
            for (var box : boxes.set.base) {
                put(e, boxes, box);
            }
            var windows = boxes.set.windows(clip);
            if (windows != null) {
                for (int i = 0; i < windows.size; i++) {
                    var window = windows.get(i);
                    if (!window.contains(time)) continue;
                    open |= 1 << i;
                    for (var box : window.boxes()) {
                        put(e, boxes, box);
                    }
                }
            }
        }

        // A new activation forgets who was struck: a new clip, a restart or loop (time went back), a window opened or closed
        if (entry != boxes.clipEntry || time < boxes.clipTime || open != boxes.open) {
            boxes.struck.clear();
        }
        boxes.clip      = clip;
        boxes.clipTime  = time;
        boxes.clipEntry = entry;
        boxes.open      = open;
        for (int i = boxes.struck.size - 1; i >= 0; i--) {
            if (!boxes.struck.get(i).isAlive()) boxes.struck.removeIndex(i);
        }
    }

    /** A sprite animation's time within one play: wrapped for the looping modes, as {@code getKeyFrame} reads it. */
    private static float clipTime(Animation<?> animation, float time) {
        var duration = animation.getAnimationDuration();
        return switch (animation.getPlayMode()) {
            case NORMAL, REVERSED -> time;
            default -> (duration > 0) ? time % duration : 0;
        };
    }

    /** Puts {@code box} out in place of an earlier one with the same name, or after the rest. */
    private void put(Entity e, Hitboxes boxes, Hitbox box) {
        var slot = boxes.find(box.name());
        if (slot == null) {
            slot = boxes.obtain();
            boxes.active.add(slot);
        }
        slot.box = box;
        slot.touching = false;
        slot.x = 0;
        slot.y = 0;
        if (box.bone() == null) return;

        // The bone was posed this tick at the entity position (lifted, if the art is lifted); keep the lift out so it is added once
        var spine = SpineAnimator.get(e);
        var bone  = (spine != null) ? spine.skeleton.findBone(box.bone()) : null;
        if (bone == null) {
            throw new GdxRuntimeException(TAG + ": %s box '%s' is on bone '%s', which %s has not got".formatted(
                    box.kind(), box.name(), box.bone(), (spine == null) ? "a fighter without a SpineAnimator" : Name.of(e)));
        }
        var pose = bone.getAppliedPose();
        var pos  = Position.get(e);
        slot.x = MathUtils.round(pose.getWorldX() - pos.x);
        slot.y = MathUtils.round(pose.getWorldY() - pos.y - boxes.lift);
    }

    // ------------------------------------------------------------------------
    // Detect: HIT against HURT, in the body plane and within depth
    // ------------------------------------------------------------------------

    private void detect(Entity attacker, Entity target) {
        if (!target.isAlive() || target.isRemoving()) return;
        var a = Hitboxes.get(attacker);
        var t = Hitboxes.get(target);
        if (!t.team.in(a.targets) || !t.vulnerable) return;

        var aPos = Position.get(attacker);
        var tPos = Position.get(target);
        int dx = tPos.x - aPos.x;
        int dy = aPos.y - tPos.y;
        Hitbox hit = null, hurt = null;
        for (int i = 0; i < a.active.size; i++) {
            var h = a.active.get(i);
            if (!h.box.isHit()) continue;
            for (int j = 0; j < t.active.size; j++) {
                var u = t.active.get(j);
                if (!u.box.isHurt() || !touch(a, aPos, h, t, tPos, u, dy)) continue;
                h.touching = true;
                u.touching = true;
                if (hit == null) { hit = h.box; hurt = u.box; }
            }
        }
        if (hit == null || a.struck.contains(target, true)) return;

        a.struck.add(target);
        strikes.add(new Strike(attacker, hit, target, hurt, a.facing, dx, dy));
    }

    /** Floor lines within the two depths, then the shapes in the body plane: x along the street, y the height above each floor line. */
    private boolean touch(Hitboxes a, Position aPos, Hitboxes.Active hit, Hitboxes t, Position tPos, Hitboxes.Active hurt, int dy) {
        if (Math.abs(dy) >= hit.box.depth() + hurt.box.depth()) return false;
        planeA.set(aPos.x + hit.x,  MathUtils.round(a.lift) + hit.y);
        planeB.set(tPos.x + hurt.x, MathUtils.round(t.lift) + hurt.y);
        return hit.box.shape(a.facing).overlaps(hurt.box.shape(t.facing), planeA, planeB, zero);
    }
}
