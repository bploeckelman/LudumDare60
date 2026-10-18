package lando.systems.ld60.game.components;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.zendogames.zenlib.Time;
import com.zendogames.zenlib.ecs.Component;
import com.zendogames.zenlib.ecs.ComponentType;
import com.zendogames.zenlib.ecs.Entity;
import com.zendogames.zenlib.ecs.components.Position;
import com.zendogames.zenlib.ecs.components.Renderable;
import com.zendogames.zenlib.render.RenderLayer;
import lando.systems.ld60.Main;
import lando.systems.ld60.assets.EmitterType;
import lando.systems.ld60.assets.GameShader;
import lando.systems.ld60.events.ParticleEvent;
import lando.systems.ld60.particles.effects.CloneEffect;
import lando.systems.ld60.particles.effects.HealthBarParticleEffect;

public class FloatingHealthBar extends Renderable {

    public static final ComponentType<FloatingHealthBar> TYPE = ComponentType.register(FloatingHealthBar.class);
    public static FloatingHealthBar get(Entity e) { return TYPE.get(e); }
    public static boolean   has(Entity e) { return TYPE.has(e); }


    private final Entity owner;
    private final TextureRegion pixel;

    // Health tracking & animation
    private float targetPct = 1f;
    private float trailingPct = 1f;
    private float delayTimer = 0f;

    // Flags for systems/particle spawners to query
    public boolean isLerping = false;
    public boolean justFinishedLerping = false;

    public float delayBeforeCatchUp = 0.5f; // Seconds before the orange bar starts moving
    public float lerpSpeed = .6f;
    private float particleTimer = 0f; // NEW: Throttles block generation

    public FloatingHealthBar(Entity owner, float yOffset) {
        this(owner, 50f, 4f, yOffset);
    }

    public FloatingHealthBar(Entity owner, float width, float height, float yOffset) {
        this.owner = owner;
        this.pixel = new TextureRegion(Main.game.pixel);

        // Define dimensions and layer
        this.size.set(width, height);
        this.layer = RenderLayer.OVERLAY; // Draws on top of sprites
        this.order = 100;

        // Position offset: center horizontally, shift upward by yOffset
        // origin shifts rect(x, y): (x - origin.x, y - origin.y)
        this.origin.set(width / 2f, -yOffset);

        Health health = Health.get(owner);
        if (health != null && health.max > 0) {
            this.targetPct = MathUtils.clamp((float) health.current / health.max, 0f, 1f);
            this.trailingPct = targetPct;
        }
    }

    /**
     * Call every frame in a system or before render.
     * Updates the trailing damage bar toward the actual target health.
     */
    public void update(float dt) {
        justFinishedLerping = false;

        Health health = Health.get(owner);
        if (health == null) return;
        Position pos = Position.get(owner);
        if (pos == null) return;

        float actualPct = MathUtils.clamp((float) health.current / health.max, 0f, 1f);

        // Health decreased: start delay, update target
        if (actualPct < targetPct) {
            targetPct = actualPct;
            delayTimer = delayBeforeCatchUp;
            isLerping = true;
        } else if (actualPct > targetPct) {
            // Health healed: snap or immediately raise both
            targetPct = actualPct;
            trailingPct = actualPct;
            isLerping = false;
        }

        // Handle catch-up animation
        if (trailingPct > targetPct) {
            if (delayTimer > 0f) {
                delayTimer -= dt;
            } else {
                // Smoothly pull trailingPct down to targetPct
                trailingPct -= lerpSpeed * dt;

                // Snap once close enough to prevent infinite fractional tail
                if (trailingPct - targetPct < 0.002f) {
                    trailingPct = targetPct;
                    isLerping = false;
                    justFinishedLerping = true;
                } else {
                    isLerping = true;
                    particleTimer += dt;
                    if (particleTimer > 0.08f && health.isAlive()) {
                        particleTimer = 0f;
                        ParticleEvent.spawn(EmitterType.HEALTH_BAR_DAMAGE, new HealthBarParticleEffect.Params(getTrailingWorldX(pos.x), pos.y + (-origin.y)), RenderLayer.OVERLAY);
                    }
                }
            }
        } else {
            trailingPct = targetPct;
            isLerping = false;
        }
    }

    /**
     * Returns the world X position where the trailing damage chunk currently ends.
     * Useful for spawning particles at the right edge of the retreating bar!
     */
    public float getTrailingWorldX(float entityWorldX) {
        float barLeft = entityWorldX - origin.x * scale.x;
        float barWidth = size.x * scale.x;
        return barLeft + (barWidth * trailingPct);
    }

    @Override
    public void render(Batch batch, float x, float y) {
        // Run update directly on render tick to keep motion smooth
        update(Time.delta);

        Health health = Health.get(owner);
        if (health == null || health.isDead()) {
            return;
        }

        // Only draw when damaged or still lerping
        if (health.current >= health.max && !isLerping) {
            return;
        }

        var shader = GameShader.HEALTH_BAR.get();
        var prevShader = batch.getShader();

        batch.setShader(shader);
        shader.setUniformf("u_healthPct", targetPct);
        shader.setUniformf("u_damagePct", trailingPct);

        var rect = rect(x, y);
        batch.setColor(tint);
        batch.draw(pixel, rect.x, rect.y, rect.width, rect.height);

        batch.setShader(prevShader);
    }
}
