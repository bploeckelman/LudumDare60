package lando.systems.ld60.events;

import com.zendogames.zenlib.ecs.Entity;
import lando.systems.ld60.game.combat.Strike;

/**
 * What {@code GameStrikes} publishes, for the feedback that is not a rule: a hit sound, a screen
 * shake when the player is struck, a burst of particles, a score. Subscribers register like the
 * other signals ({@code CombatEvent.signal.register(this)}) and switch on the subclass.
 */
public abstract class CombatEvent {

    public static final Signal<CombatEvent> signal = new Signal<>();

    public static void struck(Strike strike)         { signal.dispatch(new Struck(strike)); }
    public static void died(Entity e, Strike strike) { signal.dispatch(new Died(e, strike)); }


    /** A hit landed; {@code strike.target()} may be dead by now (then a {@link Died} follows). */
    public static final class Struck extends CombatEvent {
        public final Strike strike;
        private Struck(Strike strike) { this.strike = strike; }
    }

    /** {@code entity}'s health ran out from {@code strike}. */
    public static final class Died extends CombatEvent {
        public final Entity entity;
        public final Strike strike;
        private Died(Entity entity, Strike strike) { this.entity = entity; this.strike = strike; }
    }
}
