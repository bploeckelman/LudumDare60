package lando.systems.ld60.game.components;

import com.zendogames.zenlib.ecs.Component;
import com.zendogames.zenlib.ecs.ComponentType;
import com.zendogames.zenlib.ecs.Entity;
import lando.systems.ld60.game.anim.Move;
import lando.systems.ld60.game.characters.Kind;
import lando.systems.ld60.game.characters.Role;
import lando.systems.ld60.game.characters.Wield;

/**
 * Which {@link Kind} and {@link Role} an entity was spawned as, so code that has only the entity
 * (weapon pickup, the attack state, a brain, a debug view) can ask what it is. {@link #move} is
 * the move the fighter is performing, set by whoever started it (a state, a brain).
 */
public class Fighter implements Component {

    public static final ComponentType<Fighter> TYPE = ComponentType.register(Fighter.class);
    public static Fighter get(Entity e) { return TYPE.get(e); }
    public static boolean has(Entity e) { return TYPE.has(e); }

    public final Kind kind;
    public final Role role;

    /** The move in progress, null between moves. */
    public Move move;

    public Fighter(Kind kind, Role role) {
        this.kind = kind;
        this.role = role;
    }

    /** How {@code e} uses what it holds now: its kind's wield for its held weapon; null for an entity that is not a fighter. */
    public static Wield wieldOf(Entity e) {
        var fighter = get(e);
        var held = Held.get(e);
        return (fighter == null || held == null) ? null : fighter.kind.wield(held.weapon);
    }
}
