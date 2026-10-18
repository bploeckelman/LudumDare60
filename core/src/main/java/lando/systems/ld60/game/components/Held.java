package lando.systems.ld60.game.components;

import com.zendogames.zenlib.ecs.Component;
import com.zendogames.zenlib.ecs.ComponentType;
import com.zendogames.zenlib.ecs.Entity;
import lando.systems.ld60.game.combat.Weapon;

/**
 * What a fighter is holding: {@link #weapon} now, {@link #unarmed} to fall back to when it is
 * thrown, dropped or breaks, and {@link #hits} landed with the current one toward its durability.
 * {@code Weapons} does the swapping (and keeps {@code Hitboxes.set} in step); states and AI only read.
 */
public class Held implements Component {

    public static final ComponentType<Held> TYPE = ComponentType.register(Held.class);
    public static Held    get(Entity e) { return TYPE.get(e); }
    public static boolean has(Entity e) { return TYPE.has(e); }

    public final Weapon unarmed;
    public Weapon weapon;
    public int hits;

    public Held(Weapon unarmed) {
        this.unarmed = unarmed;
        this.weapon  = unarmed;
        this.hits    = 0;
    }

    public boolean armed()    { return weapon != unarmed; }
    public boolean canThrow() { return armed() && weapon.throwable(); }
}
