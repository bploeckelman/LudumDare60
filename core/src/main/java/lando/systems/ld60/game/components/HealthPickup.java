package lando.systems.ld60.game.components;

import com.zendogames.zenlib.ecs.Component;
import com.zendogames.zenlib.ecs.ComponentType;
import com.zendogames.zenlib.ecs.Entity;

/**
 * Health lying on the floor: a {@code PICKUP} zone the player heals {@link #amount} by walking
 * over ({@code BrawlContacts} on ENTER, then the entity is gone). Dropped by {@code Drops}.
 */
public record HealthPickup(int amount) implements Component {

    public static final ComponentType<HealthPickup> TYPE = ComponentType.register(HealthPickup.class);
    public static HealthPickup get(Entity e) { return TYPE.get(e); }
    public static boolean      has(Entity e) { return TYPE.has(e); }
}
