package lando.systems.ld60.game;

import com.badlogic.gdx.graphics.Color;
import com.zendogames.zenlib.Log;
import com.zendogames.zenlib.ecs.Entity;
import com.zendogames.zenlib.ecs.components.*;
import lando.systems.ld60.game.combat.Strike;
import lando.systems.ld60.game.components.Health;
import lando.systems.ld60.game.components.Hitboxes;
import lando.systems.ld60.game.movement.BrawlerMotion;
import lando.systems.ld60.game.state.ExamplePlayerState;

/**
 * What a landed hit does in the examples: the target turns to face the attacker, is thrown away
 * from it (a {@code BrawlerMotion} {@code launch}, with the {@code HURT_SPINEBOY} state holding
 * the controls meanwhile), loses health, flashes red and cannot be hit again for {@link #RECOVER}
 * seconds; at zero health it greys out, stops attacking and stays unhittable.
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

        var away = strike.away();
        face(target, -away);
        if (Mover.has(target) && Mover.get(target).motion instanceof BrawlerMotion motion) {
            motion.launch(Mover.get(target), away, attack.knockback(), attack.height());
            if (Behavior.has(target)) {
                Behavior.get(target).machine.request(ExamplePlayerState.HURT_SPINEBOY);
            }
        }

        var health = Health.get(target);
        if (health != null) {
            health.current -= attack.damage();
        }
        Log.info(TAG, "%s %s hit %s %s for %d%s",
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

    /** Turns the motion and the art toward {@code dir}; a state that copies motion facing to the art every tick agrees. */
    private static void face(Entity e, int dir) {
        if (Mover.has(e) && Mover.get(e).motion instanceof BrawlerMotion motion) motion.facing = dir;
        if (SpineAnimator.has(e)) SpineAnimator.get(e).facing = dir;
        if (Animator     .has(e)) Animator     .get(e).facing = dir;
    }

    private static void tint(Entity e, Color color) {
        if (SpineAnimator.has(e)) SpineAnimator.get(e).tint.set(color);
        if (Animator     .has(e)) Animator     .get(e).tint.set(color);
    }
}
