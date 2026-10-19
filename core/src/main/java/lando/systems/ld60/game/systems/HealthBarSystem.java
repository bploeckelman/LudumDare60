package lando.systems.ld60.game.systems;

import com.badlogic.gdx.math.MathUtils;
import com.zendogames.zenlib.ecs.Entity;
import com.zendogames.zenlib.ecs.Family;
import com.zendogames.zenlib.ecs.IteratingSystem;
import com.zendogames.zenlib.ecs.SystemOrder;
import com.zendogames.zenlib.ecs.components.Position;
import com.zendogames.zenlib.ecs.systems.InputSystem;
import com.zendogames.zenlib.render.RenderLayer;
import lando.systems.ld60.assets.EmitterType;
import lando.systems.ld60.events.ParticleEvent;
import lando.systems.ld60.game.components.FloatingHealthBar;
import lando.systems.ld60.game.components.Health;
import lando.systems.ld60.particles.effects.HealthBarParticleEffect;


public class HealthBarSystem extends IteratingSystem  {

    public static final int PRIORITY =  SystemOrder.PRESENT -1 ;


    public HealthBarSystem() {
        // Only process entities that have all three required components
        super(Family.all(FloatingHealthBar.TYPE, Health.TYPE, Position.TYPE), PRIORITY );
    }

    @Override
    protected void process(Entity entity, float dt) {
        var bar = FloatingHealthBar.get(entity);
        var pos = Position.get(entity);

        var health = Health.get(entity);
        if (health == null || health.isDead()) {
            return;
        }

        if (health.current >= health.max && !bar.isLerping) {
            return;
        }

        bar.justFinishedLerping = false;

        float actualPct = MathUtils.clamp((float) health.current / health.max, 0f, 1f);

        // Health decreased: start delay, update target
        if (actualPct < bar.targetPct) {
            bar.targetPct = actualPct;
            bar.delayTimer = bar.delayBeforeCatchUp;
            bar.isLerping = true;
        } else if (actualPct > bar.targetPct) {
            // Health healed: snap or immediately raise both
            bar.targetPct = actualPct;
            bar.trailingPct = actualPct;
            bar.isLerping = false;
        }

        // Handle catch-up animation
        if (bar.trailingPct > bar.targetPct) {
            if (bar.delayTimer > 0f) {
                bar.delayTimer -= dt;
            } else {
                // Smoothly pull trailingPct down to targetPct
                bar.trailingPct -= bar.lerpSpeed * dt;

                // Snap once close enough to prevent infinite fractional tail
                if (bar.trailingPct - bar.targetPct < 0.002f) {
                    bar.trailingPct = bar.targetPct;
                    bar.isLerping = false;
                    bar.justFinishedLerping = true;
                } else {
                    bar.isLerping = true;
                    bar.particleTimer += dt;
                    if (bar.particleTimer > 0.08f && health.isAlive()) {
                        bar.particleTimer = 0f;
                        ParticleEvent.spawn(EmitterType.HEALTH_BAR_DAMAGE, new HealthBarParticleEffect.Params(bar.getTrailingWorldX(pos.x), pos.y + (-bar.origin.y)), RenderLayer.OVERLAY);
                    }
                }
            }
        } else {
            bar.trailingPct = bar.targetPct;
            bar.isLerping = false;
        }
    }
}
