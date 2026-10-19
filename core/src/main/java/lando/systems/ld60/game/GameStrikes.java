package lando.systems.ld60.game;

import com.badlogic.gdx.graphics.Color;
import com.zendogames.zenlib.Log;
import com.zendogames.zenlib.ecs.Entity;
import com.zendogames.zenlib.ecs.components.*;
import lando.systems.ld60.events.CombatEvent;
import lando.systems.ld60.game.combat.Strike;
import lando.systems.ld60.game.components.BrawlerAnimClips;
import lando.systems.ld60.game.components.Health;
import lando.systems.ld60.game.components.Hitboxes;
import lando.systems.ld60.game.components.Hurtable;
import lando.systems.ld60.game.movement.BrawlerMotion;

/**
 * The one rule book for landed hits, driven by the target's {@link Hurtable}: turn to face the
 * attacker, a {@code BrawlerMotion} {@code launch} away from it scaled by the hurtable's knockback,
 * {@link Health} down by the attack's damage, the flash tint and immunity for its recover time,
 * then the hooks (a state request for a fighter with a {@code Behavior}, the callbacks for one
 * without). At zero health, once: the death tint, {@code vulnerable} off for good, the death hooks
 * and an optional despawn timer. Last, the attacker's own {@code Hitboxes.onStrike}, and the
 * strike goes out on {@link CombatEvent} for sound, shake and particles to pick up.
 * <p>
 * Nothing here names an entity type, a state enum or a timer of some factory's: all of that is
 * on the components the factory added.
 */
public class GameStrikes implements Strike.Responder {

    private static final String TAG = GameStrikes.class.getSimpleName();

    @Override
    public void strike(Strike strike) {
        var attacker = strike.attacker();
        var target   = strike.target();
        var attack   = strike.attack();
        var hurtable = Hurtable.get(target);
        var health   = Health.get(target);

        if (hurtable != null) {
            hit(target, strike, hurtable, health);
        }
        Log.info(TAG, "%s %s hit %s %s for %d%s",
                Name.of(attacker), strike.hit().name(), Name.of(target), strike.hurt().name(), attack.damage(),
                (health != null) ? ", %d/%d left".formatted(health.current, health.max) : "");

        var boxes = Hitboxes.get(attacker);
        if (boxes.onStrike != null) {
            boxes.onStrike.struck(attacker, strike);
        }
        CombatEvent.struck(strike);
    }

    private static void hit(Entity target, Strike strike, Hurtable hurtable, Health health) {
        var attack = strike.attack();
        var away   = strike.away();
        var boxes  = Hitboxes.get(target);
        var timers = timers(target);

        if (hurtable.turns) face(target, -away);
        if (hurtable.knockback > 0 && Mover.has(target) && Mover.get(target).motion instanceof BrawlerMotion motion) {
            motion.launch(Mover.get(target), away, attack.knockback() * hurtable.knockback, attack.height() * hurtable.knockback);
        }
        if (health != null) {
            health.current -= attack.damage();
        }
        hurtable.lastStrike = strike;
        hurtable.hits++;

        if (health != null && health.isDead()) {
            if (!hurtable.dead) die(target, strike, hurtable, boxes, timers);
            return;
        }

        // a hit that left it on the floor: the flinch clip, if its clips have one (it plays through, the present system does not own it)
        var clips = BrawlerAnimClips.get(target);
        if (clips != null && clips.flinch != null && Mover.has(target)
         && Mover.get(target).motion instanceof BrawlerMotion motion && motion.grounded) {
            clips.flinch.start(target, false);
        }

        if (hurtable.recoverTime > 0) {
            boxes.vulnerable = false;
            if (hurtable.flash != null) tint(target, hurtable.flash);
            timers.after("recover", hurtable.recoverTime, recover -> {
                var e = recover.entity();
                tint(e, Color.WHITE);
                Hitboxes.get(e).vulnerable = true;
            });
        }
        if (hurtable.hurtState != null && Behavior.has(target)) Behavior.get(target).request(hurtable.hurtState);
        if (hurtable.onHurt != null) hurtable.onHurt.struck(target, strike);
    }

    private static void die(Entity target, Strike strike, Hurtable hurtable, Hitboxes boxes, Timers timers) {
        hurtable.dead = true;
        boxes.vulnerable = false;
        timers.cancel("recover");
        if (hurtable.deathTint != null) tint(target, hurtable.deathTint);
        if (hurtable.deadState != null && Behavior.has(target)) Behavior.get(target).request(hurtable.deadState);
        if (hurtable.onDeath != null) hurtable.onDeath.struck(target, strike);
        if (hurtable.despawnAfter > 0) {
            timers.after("despawn", hurtable.despawnAfter, despawn -> despawn.entity().world().despawn(despawn.entity()));
        }
        CombatEvent.died(target, strike);
    }

    /** The target's timers, added on first use so a factory need not remember them. */
    private static Timers timers(Entity e) {
        var timers = Timers.get(e);
        if (timers == null) {
            timers = new Timers();
            e.add(timers);
        }
        return timers;
    }

    /** Turns the motion and the art toward {@code dir}; a system that copies motion facing to the art every tick agrees. */
    private static void face(Entity e, int dir) {
        if (Mover.has(e) && Mover.get(e).motion instanceof BrawlerMotion motion) motion.facing = dir;
        if (SpineAnimator.has(e)) SpineAnimator.get(e).facing = dir;
        if (Animator     .has(e)) Animator     .get(e).facing = dir;
    }

    private static void tint(Entity e, Color color) {
        if (SpineAnimator.has(e)) SpineAnimator.get(e).tint.set(color);
        if (Animator     .has(e)) Animator     .get(e).tint.set(color);
        if (Image        .has(e)) Image        .get(e).tint.set(color);
    }
}
