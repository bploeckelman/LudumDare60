package lando.systems.ld60.game.combat;

import com.zendogames.zenlib.ecs.Entity;

/**
 * One landed hit for one tick: {@code attacker}'s {@code hit} box reached {@code target}'s
 * {@code hurt} box (the first of the target's boxes it overlaps, in the order they resolved).
 * {@code facing} is the attacker's; {@code dx} is the target's feet minus the attacker's along the
 * street and {@code dy} the attacker's floor line minus the target's. {@link #away()} is the
 * direction to throw the target.
 */
public record Strike(Entity attacker, Hitbox hit, Entity target, Hitbox hurt, int facing, int dx, int dy) {

    public Hitbox.Attack attack() { return hit.attack(); }

    /** -1/+1 away from the attacker: the side the target stands on, the attacker's facing when they share an x. */
    public int away() {
        return (dx != 0) ? Integer.signum(dx) : facing;
    }

    /** The game's half of strikes: called once per {@link Strike} after {@code HitSystem} has found them all for the tick. */
    @FunctionalInterface
    public interface Responder {

        void strike(Strike strike);

        Responder NONE = strike -> {};
    }
}
