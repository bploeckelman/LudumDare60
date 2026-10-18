package lando.systems.ld60.game.components;

import com.zendogames.zenlib.ecs.Component;
import com.zendogames.zenlib.ecs.ComponentType;
import com.zendogames.zenlib.ecs.Entity;
import lando.systems.ld60.game.combat.Weapon;

/**
 * A weapon lying on the floor. The entity's {@code PICKUP} collider is the zone a fighter must
 * stand in; a fighter with {@code Held} takes it by pressing attack there ({@code Weapons.under}),
 * or on touch when {@link #onTouch} is set (a contact responder calls {@code Weapons.pickUp}).
 */
public record Pickup(Weapon weapon, boolean onTouch) implements Component {

    public static final ComponentType<Pickup> TYPE = ComponentType.register(Pickup.class);
    public static Pickup  get(Entity e) { return TYPE.get(e); }
    public static boolean has(Entity e) { return TYPE.has(e); }

    public Pickup(Weapon weapon) {
        this(weapon, /*onTouch*/ false);
    }
}
