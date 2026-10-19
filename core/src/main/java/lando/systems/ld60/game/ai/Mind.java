package lando.systems.ld60.game.ai;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.zendogames.zenlib.ecs.Entity;
import lando.systems.ld60.game.characters.Kind;

/**
 * What controls a fighter: the keyboard and the player's states, a brain of timers, a boss's own
 * state machine. {@link #attach} runs once at spawn, after every other component is on the entity
 * (so {@code Timers}, {@code Mover}, {@code Held} and the animator are there to use), and must add
 * the entity's {@code Input} (the motion reads it) plus whatever the mind itself needs: a
 * {@code Behavior}, a {@code GameInput}, timers. A mind may be shared by every entity of a role,
 * so per-entity state goes in components or the timer closures, never in fields.
 */
public interface Mind {

    void attach(Entity e, Kind kind, Senses senses);
}
