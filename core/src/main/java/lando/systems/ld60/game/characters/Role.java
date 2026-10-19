package lando.systems.ld60.game.characters;

import com.zendogames.zenlib.ecs.Tag;
import lando.systems.ld60.game.GameMask;
import lando.systems.ld60.game.ai.Mind;
import lando.systems.ld60.game.components.Hurtable;
import lando.systems.ld60.game.movement.BrawlerTuning;

import java.util.function.Supplier;

/**
 * Who a fighter is <em>for</em>: the side it is on, who it can hit, what controls it, what a
 * hit does to it. The player, a thug and a boss are roles; any {@link Kind} can take any of
 * them. Instances live in {@link Roles}.
 *
 * @param name      for logs
 * @param tag       the entity tag ({@code GameTags.PLAYER}, {@code ENEMY}, {@code BOSS})
 * @param body      the floor collider's mask
 * @param detects   what the floor collider reports (pickups, triggers)
 * @param team      the hit boxes' team
 * @param targets   the teams its hit boxes can hit
 * @param tuning    a movement override for this role, null to use the kind's
 * @param hurtable  what a landed hit does: the states to request, or flash / fly / die / vanish; a new instance per entity
 * @param mind      what controls it: the keyboard and the player's states, a brain of timers, a boss's own state machine
 * @param healthBar a floating bar over its head
 */
public record Role(
        String name,
        Tag tag,
        GameMask body,
        GameMask[] detects,
        GameMask team,
        GameMask[] targets,
        BrawlerTuning tuning,
        Supplier<Hurtable> hurtable,
        Mind mind,
        boolean healthBar
) {
    /** The same role with another mind: a thug with a different brain, a boss with its own states. */
    public Role mind(Mind mind) {
        return new Role(name, tag, body, detects, team, targets, tuning, hurtable, mind, healthBar);
    }

    /** The same role with or without the floating bar (headless tests have no pixel to draw it with). */
    public Role healthBar(boolean healthBar) {
        return new Role(name, tag, body, detects, team, targets, tuning, hurtable, mind, healthBar);
    }
}
