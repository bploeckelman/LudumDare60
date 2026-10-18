package lando.systems.ld60.game.components;

import com.badlogic.gdx.graphics.Color;
import com.zendogames.zenlib.ecs.Component;
import com.zendogames.zenlib.ecs.ComponentType;
import com.zendogames.zenlib.ecs.Entity;
import com.zendogames.zenlib.state.State;
import lando.systems.ld60.game.GameStrikes;
import lando.systems.ld60.game.combat.Strike;

/**
 * What a landed hit does to this entity, authored where it is spawned so {@link GameStrikes}
 * never has to know the entity type: how long it flashes and is immune, how far it is thrown,
 * what happens when its {@link Health} runs out. Every field has a default a rank-and-file
 * enemy is happy with; a fighter with a {@code Behavior} names the states to request, a fighter
 * without one uses the callbacks, or neither.
 * <pre>{@code
 * entity.add(new Hurtable().despawnAfter(1.5f));                              // a thug: flash, fly, die, vanish
 * entity.add(new Hurtable().hurtState(HURT).deadState(DEAD).deathTint(null)); // the player: the states take over
 * entity.add(new Hurtable().knockback(0).onHurt((e, s) -> Timers.get(e).restart("enrage")));  // a boss with super armor
 * }</pre>
 * A target without this component can still be struck ({@code Hitboxes} says where); only the
 * attacker's {@code Hitboxes.onStrike} callback runs for it.
 */
public class Hurtable implements Component {

    public static final ComponentType<Hurtable> TYPE = ComponentType.register(Hurtable.class);
    public static Hurtable get(Entity e) { return TYPE.get(e); }
    public static boolean  has(Entity e) { return TYPE.has(e); }

    /** Seconds of {@link #flash} tint and immunity after a hit; 0 for none. */
    public float recoverTime = 0.3f;

    /** Scale on the attack's knockback and height; 0 is super armor (a boss that does not budge when hit). */
    public float knockback = 1f;

    /** Turns to face the attacker when hit. */
    public boolean turns = true;

    /** Tint while recovering; null leaves the art alone. */
    public Color flash = Color.RED;

    /** Tint once dead; null leaves the art to a state or a callback. */
    public Color deathTint = Color.DARK_GRAY;

    /** Seconds after death until the entity is despawned; 0 keeps the body around. */
    public float despawnAfter = 0f;

    /** States to request on the entity's {@code Behavior} when hit / when health runs out; null asks for no change. */
    public State<Entity> hurtState, deadState;

    /** Called after the hit is applied / after death is applied; null does nothing. */
    public Strike.Callback onHurt, onDeath;

    // ------------------------------------------------------------------------
    // Facts written by GameStrikes, read by states, timers and debug views
    // ------------------------------------------------------------------------

    /** The most recent hit taken; null until the first. */
    public Strike lastStrike;

    /** Hits taken so far. */
    public int hits;

    /** Health ran out; set once, with the death effects. */
    public boolean dead;

    public Hurtable recoverTime(float seconds)         { this.recoverTime  = seconds; return this; }
    public Hurtable knockback(float scale)             { this.knockback    = scale;   return this; }
    public Hurtable turns(boolean turns)               { this.turns        = turns;   return this; }
    public Hurtable flash(Color color)                 { this.flash        = color;   return this; }
    public Hurtable deathTint(Color color)             { this.deathTint    = color;   return this; }
    public Hurtable despawnAfter(float seconds)        { this.despawnAfter = seconds; return this; }
    public Hurtable hurtState(State<Entity> state)     { this.hurtState    = state;   return this; }
    public Hurtable deadState(State<Entity> state)     { this.deadState    = state;   return this; }
    public Hurtable onHurt(Strike.Callback callback)    { this.onHurt       = callback; return this; }
    public Hurtable onDeath(Strike.Callback callback)   { this.onDeath      = callback; return this; }
}
