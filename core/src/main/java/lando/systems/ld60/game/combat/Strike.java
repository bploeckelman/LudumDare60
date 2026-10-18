package lando.systems.ld60.game.combat;

import com.zendogames.zenlib.ecs.Entity;

/**
 * One landed hit for one tick: {@code attacker}'s {@code hit} box reached {@code target}'s
 * {@code hurt} box (the first of the target's boxes it overlaps, in the order they resolved).
 * {@code facing} is the attacker's, so "away from the attacker" is {@code facing} for the target;
 * {@code dy} is the attacker's floor line minus the target's.
 */
public record Strike(Entity attacker, Hitbox hit, Entity target, Hitbox hurt, int facing, int dy) {

    public Hitbox.Attack attack() { return hit.attack(); }

    /** The game's half of strikes: called once per {@link Strike} after {@code HitSystem} has found them all for the tick. */
    @FunctionalInterface
    public interface Responder {

        void strike(Strike strike);

        Responder NONE = strike -> {};
    }
}
