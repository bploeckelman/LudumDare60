package lando.systems.ld60.game.components;

import com.zendogames.zenlib.ecs.Component;
import com.zendogames.zenlib.ecs.ComponentType;
import com.zendogames.zenlib.ecs.Entity;

/**
 * What a trigger zone does to a player that enters it, applied by {@code GameContacts}.
 * The zone itself is the {@code TRIGGER} collider; this is its effect, from the map's
 * {@code trigger} class ({@code effect}, {@code height}, {@code dwell}, {@code target}).
 * Every effect fires on the overlap's ENTER. In the future it may be worth setting up
 * different activation modes like touch, center, inside.
 */
public record Trigger(Effect effect) implements Component {

    public static final ComponentType<Trigger> TYPE = ComponentType.register(Trigger.class);
    public static Trigger get(Entity e) { return TYPE.get(e); }
    public static boolean has(Entity e) { return TYPE.has(e); }

    public sealed interface Effect permits Log, Bounce, Teleport {}

    /** Nothing beyond the usual contact log line. */
    public record Log() implements Effect {}

    /** Launches the hero straight up {@code height} tiles. */
    public record Bounce(float height) implements Effect {}

    /** Moves the hero to {@code (x, y)} after {@code dwell} seconds inside; leaving first cancels it. */
    public record Teleport(int x, int y, float dwell) implements Effect {}
}
