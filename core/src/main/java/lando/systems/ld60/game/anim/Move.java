package lando.systems.ld60.game.anim;

import com.zendogames.zenlib.ecs.Entity;

import java.util.function.Consumer;

/**
 * An attack or other one-shot performance: the {@link AnimClip} to play through once, plus
 * whatever else has to happen as it starts (spineboy levels his gun on a second track).
 * The hit boxes that go with it are in the fighter's {@code HitboxSet} under the clip's name.
 *
 * @param clip    played once, not looping
 * @param onStart extra setup when the move starts; null for none
 */
public record Move(AnimClip clip, Consumer<Entity> onStart) {

    public static Move of(AnimClip clip)                           { return new Move(clip, null); }
    public static Move of(AnimClip clip, Consumer<Entity> onStart) { return new Move(clip, onStart); }

    public void start(Entity e) {
        clip.start(e, false);
        if (onStart != null) {
            onStart.accept(e);
        }
    }

    /** True from {@link #start} until the clip has played through. */
    public boolean playing(Entity e) {
        return clip.is(e) && AnimClip.complete(e);
    }
}
