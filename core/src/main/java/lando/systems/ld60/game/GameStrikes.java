package lando.systems.ld60.game;

import com.badlogic.gdx.graphics.Color;
import com.zendogames.zenlib.Log;
import com.zendogames.zenlib.ecs.Entity;
import com.zendogames.zenlib.ecs.components.*;
import lando.systems.ld60.game.combat.Strike;
import lando.systems.ld60.game.components.Health;
import lando.systems.ld60.game.components.Hitboxes;
import lando.systems.ld60.game.movement.BrawlerMotion;

/**
 * What a landed hit does in the examples: the target loses health, flashes red and cannot be hit
 * again for {@link #RECOVER} seconds; at zero health it greys out, stops attacking and stays
 * unhittable. Knockback waits for {@code BrawlerMotion}: {@code launch(mover, strike.facing(),
 * attack.knockback(), attack.height())} is the whole of it.
 */
public class GameStrikes implements Strike.Responder {

    private static final String TAG = GameStrikes.class.getSimpleName();

    /** Seconds of red tint and i-frames after a hit. */
    static final float RECOVER = 0.3f;

    @Override
    public void strike(Strike strike) {
        var target = strike.target();
        var attack = strike.attack();
        var boxes  = Hitboxes.get(target);
        var timers = Timers.get(target);
        if (timers == null) {
            timers = new Timers();
            target.add(timers);
        }

        if (Mover.has(target) && Mover.get(target).motion instanceof BrawlerMotion motion) {
            motion.facing = -strike.facing();  // face the attacker
            motion.launch(Mover.get(target), strike.facing(), attack.knockback(), attack.height());
        }

        var health = Health.get(target);
        if (health != null) {
            health.current -= attack.damage();
        }
        Log.info(TAG, "%s%s hit %s %s for %d%s",
                Name.of(strike.attacker()), strike.hit().name(),
                Name.of(target), strike.hurt().name(), attack.damage(),
                (health != null) ? ", %d/%d left".formatted(health.current, health.max) : "");

        boxes.vulnerable = false;
        if (health != null && health.isDead()) {
            tint(target, Color.DARK_GRAY);
            timers.cancel("attack");
            return;
        }
        tint(target, Color.RED);
        timers.after("recover", RECOVER, recover -> {
            tint(recover.entity(), Color.WHITE);
            Hitboxes.get(recover.entity()).vulnerable = true;
        });
    }

    private static void tint(Entity e, Color color) {
        if (SpineAnimator.has(e)) SpineAnimator.get(e).tint.set(color);
        if (Animator     .has(e)) Animator     .get(e).tint.set(color);
    }
}
