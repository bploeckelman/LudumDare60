package lando.systems.ld60.game.characters;

import com.badlogic.gdx.math.Rectangle;
import com.zendogames.zenlib.ecs.Entity;
import com.zendogames.zenlib.ecs.components.Renderable;
import lando.systems.ld60.game.Factory;
import lando.systems.ld60.game.combat.Weapon;
import lando.systems.ld60.game.components.BrawlerAnimClips;
import lando.systems.ld60.game.movement.BrawlerTuning;

import javax.management.relation.Role;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.IntFunction;
import java.util.function.Supplier;

/**
 * What a fighter <em>is</em>, regardless of who controls it or whose side it is on: its rig and
 * clips, how it moves, how much it takes, where its feet are, what it fights with and how it
 * holds each weapon it can use. One instance per character ({@code Awol.KIND},
 * {@code Spineboy.KIND}), built by that character's static class, spawned by
 * {@code Factory.fighter(kind, role, ...)} with a {@link Role} saying player, thug or boss.
 * <p>
 * Nothing in here is a mask, a tag, a brain or a hurt policy: those vary with the role, and the
 * same kind is a thug today and the player tomorrow without touching this.
 *
 * @param name     the Tiled {@code enemy.kind} member and the entity's {@code Name}
 * @param tuning   how it moves (a role may override, a thug is slower than the player)
 * @param health   hit points at spawn
 * @param feet     the floor capsule, feet at the origin
 * @param animator its rig at the drawn size, facing the given way (a {@code SpineAnimator}, or an {@code Animator} for a sprite fighter)
 * @param clips    what the present system plays for what the motion does; a new instance per entity
 * @param unarmed  what it holds when it holds nothing (fists, a welded-on gun); must be in {@code wields}
 * @param wields   the weapons it can use, by weapon name, each with this rig's boxes and moves; a weapon not here cannot be picked up
 * @param dress    after spawn: event listeners, emitters, sounds; null for none
 */
public record Kind(
        String name,
        BrawlerTuning tuning,
        int health,
        Rectangle feet,
        IntFunction<Renderable> animator,
        Supplier<BrawlerAnimClips> clips,
        Weapon unarmed,
        Map<String, Wield> wields,
        BiConsumer<Factory, Entity> dress
) {
    public Kind {
        if (!wields.containsKey(unarmed.name())) {
            throw new IllegalArgumentException(name + ": no wield for its unarmed weapon '" + unarmed.name() + "'");
        }
    }

    /** This rig's boxes and moves for {@code weapon}, null when it cannot use it. */
    public Wield wield(Weapon weapon) { return wields.get(weapon.name()); }

    public boolean canWield(Weapon weapon) { return wields.containsKey(weapon.name()); }

    /** A copy with another rig source (headless tests, a skin variant). */
    public Kind animator(IntFunction<Renderable> animator) {
        return new Kind(name, tuning, health, feet, animator, clips, unarmed, wields, dress);
    }

    /** A copy with another spawn hook (null for none). */
    public Kind dress(BiConsumer<Factory, Entity> dress) {
        return new Kind(name, tuning, health, feet, animator, clips, unarmed, wields, dress);
    }
}
