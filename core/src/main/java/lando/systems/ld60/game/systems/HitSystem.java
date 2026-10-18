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
import lando.systems.ld60.game.combat.HitboxSet;
import lando.systems.ld60.game.combat.Strike;
import lando.systems.ld60.game.components.Hitboxes;

import java.util.ArrayDeque;

/**
 * Resolves every fighter's {@link Hitboxes} for the tick, finds HIT boxes overlapping opposing HURT
 * boxes, and hands each new hit to the {@link Strike.Responder}. Runs at {@code PRESENT + 10}, after
 * the {@code AnimationSystem} and {@code SpineSystem} have advanced the clips and posed the bones,
 * so the boxes match the frame that is drawn. All strikes are found first, then responded to in
 * order, so a trade (two fighters hitting each other on the same tick) reports both.
 * <p>
 * <b>One strike per target per activation.</b> A window strikes each target once while it stays
 * open; it forgets when it closes or the clip changes, restarts or loops. A base box or a box added
 * from code strikes once while it stays out. Of an attacker's boxes from one source, the first to
 * overlap a target lands; of the target's HURT boxes, the first overlapped (resolve order) is the
 * one reported, so list a "head" before a "body" to let it win.
 * <p>
 * {@link #strikes} and {@link #log} are for the debug view: this tick's strikes, and one line per
 * strike, capped. Every pair of fighters is tested, no broadphase, the {@code CollisionWorld} stance.
 */
public class HitSystem extends IteratingSystem {

    public static final int PRIORITY = SystemOrder.PRESENT + 10;
    private static final int LOG_CAPACITY = 256;
    private static final String TAG = HitSystem.class.getSimpleName();

    public Strike.Responder responder;

    /** This tick's strikes, in detection order; valid until the next update. */
    public final Array<Strike> strikes = new Array<>(16);

    /** Strike log, oldest first. */
    public final ArrayDeque<String> log = new ArrayDeque<>(LOG_CAPACITY);
    public boolean logPaused = false;

    // Scratch for the overlap test: body-plane positions, and the zero offset the shapes' overlaps() takes
    private final PointI2 planeA = new PointI2();
    private final PointI2 planeB = new PointI2();
    private final PointI2 zero   = new PointI2();

    public HitSystem(Strike.Responder responder) {
        super(Family.all(Position.TYPE, Hitboxes.TYPE), PRIORITY);
        this.responder = (responder != null) ? responder : Strike.Responder.NONE;
    }

    /** Every entity with {@link Position} and {@link Hitboxes}, a live view; what the overlay and the debug view walk. */
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
        for (int i = 0; i < fighters.size; i++) {
            forget(Hitboxes.get(fighters.get(i)));
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

        // What is playing: Spine track 0, else a sprite Animator; with neither only base and timed boxes are out
        String clip = null;
        Object entry = null;
        float time = 0;
        var spine = SpineAnimator.get(e);
        var sprite = Animator.get(e);
        if (spine != null) {
            boxes.facing = spine.facing;
            var track = spine.state.getTrack(0);
            if (track != null) {
                clip  = track.getAnimation().getName();
                time  = track.getAnimationTime(); // wrapped for a loop, clamped at the end otherwise
                entry = track;
            }
        } else if (sprite != null) {
            boxes.facing = sprite.facing;
            if (sprite.hasAnimation()) {
                clip  = (sprite.type != null) ? sprite.type.name() : null;
                time  = clipTime(sprite.animation, sprite.time);
                entry = sprite.animation;
            }
        }

        // A new clip, a restart or a loop closes every window, even one whose time range is open again at once
        if (entry != boxes.clipEntry || time < boxes.clipTime) {
            forgetWindows(boxes);
        }
        boxes.clip = clip;
        boxes.clipTime = time;
        boxes.clipEntry = entry;

        boxes.clearActive();
        if (boxes.set != null) {
            for (var box : boxes.set.base) {
                put(e, boxes, box, box);
            }
            var windows = boxes.set.windows(clip);
            if (windows != null) {
                for (var window : windows) {
                    if (!window.contains(time)) continue;
                    for (var box : window.boxes()) {
                        put(e, boxes, box, window);
                    }
                }
            }
        }
        for (int i = 0; i < boxes.timed.size; i++) {
            var timed = boxes.timed.get(i);
            if (timed.remaining <= 0) {
                boxes.timed.removeIndex(i--);
                continue;
            }
            put(e, boxes, timed.box, timed);
            timed.remaining -= dt;
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
    private void put(Entity e, Hitboxes boxes, Hitbox box, Object source) {
        var slot = boxes.find(box.name());
        if (slot == null) {
            slot = boxes.obtain();
            boxes.active.add(slot);
        }
        slot.box = box;
        slot.source = source;
        slot.touching = false;
        slot.connected = false;
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
        var dy = aPos.y - tPos.y;
        for (int i = 0; i < a.active.size; i++) {
            var hit = a.active.get(i);
            if (!hit.box.isHit()) continue;

            Hitboxes.Active first = null;
            for (int j = 0; j < t.active.size; j++) {
                var hurt = t.active.get(j);
                if (!hurt.box.isHurt() || !touch(a, aPos, hit, t, tPos, hurt, dy)) continue;
                hit.touching = true;
                hurt.touching = true;
                if (first == null) first = hurt;
            }
            if (first == null || a.hasStruck(hit.source, target)) continue;

            a.struck.add(new Hitboxes.Struck(hit.source, target));
            var strike = new Strike(attacker, hit.box, target, first.box, a.facing, dy);
            strikes.add(strike);
            record(strike);
        }
    }

    /** Floor lines within the two depths, then the shapes in the body plane: x along the street, y the height above each floor line. */
    private boolean touch(Hitboxes a, Position aPos, Hitboxes.Active hit, Hitboxes t, Position tPos, Hitboxes.Active hurt, int dy) {
        if (Math.abs(dy) >= hit.box.depth() + hurt.box.depth()) return false;
        planeA.set(aPos.x + hit.x,  MathUtils.round(a.lift) + hit.y);
        planeB.set(tPos.x + hurt.x, MathUtils.round(t.lift) + hurt.y);
        return hit.box.shape(a.facing).overlaps(hurt.box.shape(t.facing), planeA, planeB, zero);
    }

    // ------------------------------------------------------------------------
    // Memory: who each open source has struck
    // ------------------------------------------------------------------------

    /** Drops what closed sources and dead targets struck, then marks the boxes whose source has connected. */
    private static void forget(Hitboxes boxes) {
        for (int i = boxes.struck.size - 1; i >= 0; i--) {
            var s = boxes.struck.get(i);
            if (!s.target().isAlive() || !boxes.isSourceOut(s.source())) {
                boxes.struck.removeIndex(i);
            }
        }
        for (int i = 0; i < boxes.active.size; i++) {
            var slot = boxes.active.get(i);
            slot.connected = slot.box.isHit() && boxes.hasStruckAny(slot.source);
        }
    }

    private static void forgetWindows(Hitboxes boxes) {
        for (int i = boxes.struck.size - 1; i >= 0; i--) {
            if (boxes.struck.get(i).source() instanceof HitboxSet.Window) {
                boxes.struck.removeIndex(i);
            }
        }
    }

    private void record(Strike strike) {
        if (logPaused) return;
        if (log.size() >= LOG_CAPACITY) log.pollFirst();
        log.addLast(format(strike));
    }

    public String format(Strike strike) {
        var attack = strike.attack();
        return "%6d  %s.%s -> %s.%s  dmg %d  push %.1f/%.1f  dy %+d".formatted(
                world.step.tick,
                Name.of(strike.attacker()), strike.hit().name(),
                Name.of(strike.target()), strike.hurt().name(),
                attack.damage(), attack.knockback(), attack.height(), strike.dy());
    }
}
