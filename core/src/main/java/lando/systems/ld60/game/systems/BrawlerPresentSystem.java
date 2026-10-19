package lando.systems.ld60.game.systems;

import com.zendogames.zenlib.ecs.Entity;
import com.zendogames.zenlib.ecs.Family;
import com.zendogames.zenlib.ecs.IteratingSystem;
import com.zendogames.zenlib.ecs.SystemOrder;
import com.zendogames.zenlib.ecs.components.*;
import lando.systems.ld60.game.anim.AnimClip;
import lando.systems.ld60.game.components.*;
import lando.systems.ld60.game.movement.BrawlerMotion;

/**
 * Presents every {@code BrawlerMotion} mover, right before the animators run ({@code PRESENT - 1}):
 * lifts the art and the hit boxes by the motion's height, faces the art the way the motion faces,
 * depth-sorts by the floor position (lower on the street draws in front), and for a fighter with
 * {@link BrawlerAnimClips} picks the clip: dead, thrown, airborne, else idle / walk / run by speed.
 * Any {@code Renderable} is lifted and sorted; a thrown item drawn with an {@code Image} rides
 * the same arc as a fighter.
 * <p>
 * The clip pick leaves a clip it does not own alone until it completes (a state's attack), and
 * leaves a fighter's {@code Fighter.move} alone for as long as the state keeps it set (a looping
 * hold), so states never fight the system for track 0: they play their move, the system resumes after.
 */
public class BrawlerPresentSystem extends IteratingSystem {

    public static final int PRIORITY = SystemOrder.PRESENT - 1;

    public BrawlerPresentSystem() {
        super(Family.all(Position.TYPE, Mover.TYPE), PRIORITY);
    }

    @Override
    protected void process(Entity e, float dt) {
        var mover = Mover.get(e);
        if (!(mover.motion instanceof BrawlerMotion motion)) return;

        if (Hitboxes.has(e)) Hitboxes.get(e).lift   = motion.z;
        if (Shadow  .has(e)) Shadow  .get(e).height = motion.z;

        var order = -Position.get(e).y; // lower on the street draws in front of higher
        if (SpineAnimator.has(e)) {
            var anim = SpineAnimator.get(e);
            anim.facing = motion.facing;
            lift(anim, motion, order);
        } else if (Animator.has(e)) {
            var anim = Animator.get(e);
            anim.facing = motion.facing;
            lift(anim, motion, order);
        } else if (Image.has(e)) {
            lift(Image.get(e), motion, order);
        }

        var clips = BrawlerAnimClips.get(e);
        if (clips != null) {
            present(e, clips, mover, motion);
        }
    }

    private static void lift(Renderable r, BrawlerMotion motion, int order) {
        r.origin.y = -motion.z / r.scale.y; // lift the art by its height; scale.y is never zero
        r.order    = order;
    }

    /** The clip for what the motion is doing, played only when the current clip is ours (or done). */
    private static void present(Entity e, BrawlerAnimClips clips, Mover mover, BrawlerMotion motion) {
        var current = AnimClip.current(e);
        if (!clips.owns(current) && !AnimClip.complete(e)) {
            return; // a state's move plays through
        }

        var fighter = Fighter.get(e);
        if (fighter != null && fighter.move != null && fighter.move.clip().is(e)) {
            return; // a move the state still holds, looping or done
        }

        var dead = Hurtable.has(e) && Hurtable.get(e).dead;
        if (dead) {
            var clip = clips.deadOrHurt();
            if (!clip.is(e)) {
                clip.start(e, false); // once; holds its last frame
            }
            return;
        }
        if (motion.launched && !motion.grounded) {
            clips.hurtOrJump().play(e, false);
            return;
        }
        if (!motion.grounded) {
            if (!clips.jump.is(e)) {
                clips.jump.start(e, false);
                matchHangTime(e, motion);
            }
            return;
        }
        if (motion.landed && clips.stop != null) {
            clips.stop.start(e, false);
            clips.idle.queue(e, true);
            return;
        }

        var speed = (float) Math.hypot(mover.xVel, mover.yVel);
        var want  = (speed == 0) ? clips.idle : (speed >= clips.runSpeed) ? clips.run : clips.walk;
        if (want.is(e)) return;
        if (want == clips.idle && clips.stop != null) {
            if (clips.stop.is(e) && !AnimClip.complete(e)) return; // idle is queued behind it
            if (clips.run.is(e)) {
                clips.stop.start(e, false);
                clips.idle.queue(e, true);
                return;
            }
        }
        want.play(e, true);
    }

    /** Spine: run the jump clip at the jump's speed, so its tuck and landing poses line up with take-off and touchdown. */
    private static void matchHangTime(Entity e, BrawlerMotion motion) {
        var spine = SpineAnimator.get(e);
        if (spine == null) return;

        var entry = spine.state.getTrack(0);
        if (entry == null) return;

        var hang = motion.tuning.hangTime(motion.tuning.jumpHeight());
        if (hang > 0) entry.setTimeScale(entry.getAnimation().getDuration() / hang);
    }
}
