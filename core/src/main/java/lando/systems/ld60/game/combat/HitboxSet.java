package lando.systems.ld60.game.combat;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * A fighter type's hitboxes over its animations: the {@link #base} boxes it always has, and per
 * animation ("clip", the Spine animation name, or the sprite {@code AnimType}'s {@code name()})
 * the {@link Window}s of time in which more boxes are out. One instance is shared by every entity
 * of the type; swap {@code Hitboxes.set} to change the whole table (a weapon pickup).
 * <p>
 * Each tick a fighter's boxes are resolved in order: base, then the windows of the playing clip
 * that contain its time, then the boxes added from code ({@code Hitboxes.add}). <b>A box replaces
 * an earlier one with the same name</b>, so a crouch clip can shrink "body" for its length and a
 * window can swap "fist" for a bigger one late in a swing.
 * <pre>{@code
 * new HitboxSet()
 *     .base(Hitbox.hurt("body", rect(-8, 0, 16, 44)))
 *     .clip("crouch", Hitbox.hurt("body", rect(-8, 0, 16, 24)))
 *     .window("punch", 0.10f, 0.22f, Hitbox.hit("fist", rect(10, 30, 14, 8), JAB));
 * }</pre>
 * Plain data, built in code for now; the shape a future editor reads and writes.
 */
public class HitboxSet {

    /**
     * Boxes out from {@code start} (inclusive) to {@code end} (exclusive) seconds into a clip. Each
     * window is one activation: it strikes a target at most once while it stays open (see
     * {@code HitSystem}), so a two-hit move is two windows.
     */
    public record Window(float start, float end, Hitbox... boxes) {
        public Window {
            if (end <= start) throw new GdxRuntimeException("Window: end %.3f must be after start %.3f".formatted(end, start));
        }
        public boolean contains(float time) { return time >= start && time < end; }
    }

    /** Out in every clip unless a window box of the same name replaces it. */
    public final Array<Hitbox> base = new Array<>();

    /** Clip name to its windows, in authoring order. */
    public final ObjectMap<String, Array<Window>> clips = new ObjectMap<>();

    public HitboxSet base(Hitbox... boxes) {
        base.addAll(boxes);
        return this;
    }

    /** Boxes out from {@code start} to {@code end} seconds into {@code clip}. */
    public HitboxSet window(String clip, float start, float end, Hitbox... boxes) {
        var windows = clips.get(clip);
        if (windows == null) {
            windows = new Array<>();
            clips.put(clip, windows);
        }
        windows.add(new Window(start, end, boxes));
        return this;
    }

    /** Boxes out for the whole of {@code clip}, however long it plays. */
    public HitboxSet clip(String clip, Hitbox... boxes) {
        return window(clip, 0f, Float.POSITIVE_INFINITY, boxes);
    }

    /** The windows authored for {@code clip}; null when it has none. */
    public Array<Window> windows(String clip) {
        return (clip == null) ? null : clips.get(clip);
    }
}
