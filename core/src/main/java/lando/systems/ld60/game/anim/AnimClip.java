package lando.systems.ld60.game.anim;

import com.zendogames.zenlib.assets.AnimType;
import com.zendogames.zenlib.ecs.Entity;
import com.zendogames.zenlib.ecs.components.Animator;
import com.zendogames.zenlib.ecs.components.SpineAnimator;

/**
 * Convenience wrapper for one animation an entity can play, whichever animator it has: a
 * Spine animation name for a {@link SpineAnimator}, an {@link AnimType} for a sprite
 * {@link Animator}. Systems and states hold {@code Clip}s so a plain sprite fighter
 * and a Spine fighter run the same code; a clip of the wrong kind for the entity's
 * animator is a no-op that reads as "not playing".
 * <pre>{@code
 * AnimClip.spine("run").play(e, true);     // switch if different, keep playing if the same
 * AnimClip.sprite(ZenAnimHero.HURT).start(e, false);   // restart from the first frame
 * if (clip.is(e) && AnimClip.complete(e)) ...
 * }</pre>
 * {@link #name()} is the key a {@code HitboxSet} window is authored under for either kind.
 */
public record AnimClip(String spine, AnimType sprite) {

    public static AnimClip spine(String name)    { return new AnimClip(name, null); }
    public static AnimClip sprite(AnimType type) { return new AnimClip(null, type); }

    /** The Spine animation name, or the sprite type's {@code name()}: what {@code Hitboxes} resolves windows by. */
    public String name() { return (spine != null) ? spine : sprite.name(); }

    /** True while this clip is the one on track 0 / in the animator. */
    public boolean is(Entity e) {
        var s = SpineAnimator.get(e);
        if (s != null) return (spine != null) && s.is(spine);
        var a = Animator.get(e);
        if (a != null) return (sprite != null) && a.is(sprite);
        return false;
    }

    /** Switches to this clip unless it is already playing. {@code loop} is for Spine; a sprite clip loops per its {@code AnimType}. */
    public void play(Entity e, boolean loop) {
        var s = SpineAnimator.get(e);
        if (s != null) { if (spine != null) s.play(spine, loop); return; }
        var a = Animator.get(e);
        if (a != null && sprite != null) a.play(sprite);
    }

    /** Switches to this clip and restarts it even if it is already playing. */
    public void start(Entity e, boolean loop) {
        var s = SpineAnimator.get(e);
        if (s != null) { if (spine != null) s.start(spine, loop); return; }
        var a = Animator.get(e);
        if (a != null && sprite != null) a.start(sprite);
    }

    /** Spine: plays after the current track 0 entry ends. Sprite: plays now (a sprite animator has no queue). */
    public void queue(Entity e, boolean loop) {
        var s = SpineAnimator.get(e);
        if (s != null) { if (spine != null) s.queue(spine, loop, 0); return; }
        var a = Animator.get(e);
        if (a != null && sprite != null) a.play(sprite);
    }

    /** The name of what is playing now (track 0 / the animator's type), null when nothing or unnamed. */
    public static String current(Entity e) {
        var s = SpineAnimator.get(e);
        if (s != null) {
            var track = s.state.getTrack(0);
            return (track == null) ? null : track.getAnimation().getName();
        }
        var a = Animator.get(e);
        return (a == null || a.type == null) ? null : a.type.name();
    }

    /** True when nothing is playing or the current clip has played through once (a looping clip never completes). */
    public static boolean complete(Entity e) {
        var s = SpineAnimator.get(e);
        if (s != null) return s.isComplete();
        var a = Animator.get(e);
        return a == null || a.isComplete();
    }

}
