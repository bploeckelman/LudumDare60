package lando.systems.ld60.game.components;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.zendogames.zenlib.ecs.ComponentType;
import com.zendogames.zenlib.ecs.Entity;
import com.zendogames.zenlib.ecs.components.Renderable;
import com.zendogames.zenlib.render.RenderLayer;
import lando.systems.ld60.Main;
import lando.systems.ld60.assets.GameShader;

public class FloatingHealthBar extends Renderable {

    public static final ComponentType<FloatingHealthBar> TYPE = ComponentType.register(FloatingHealthBar.class);
    public static FloatingHealthBar get(Entity e) { return TYPE.get(e); }
    public static boolean   has(Entity e) { return TYPE.has(e); }

    private final Entity owner;
    private final TextureRegion pixel;

    // Health tracking & animation (Now public for the System to mutate)
    public float targetPct = 1f;
    public float trailingPct = 1f;
    public float delayTimer = 0f;
    public float particleTimer = 0f;

    // Flags for systems/particle spawners to query
    public boolean isLerping = false;
    public boolean justFinishedLerping = false;

    public float delayBeforeCatchUp = 0.5f;
    public float lerpSpeed = .6f;

    public FloatingHealthBar(Entity owner, float yOffset) {
        this(owner, 50f, 4f, yOffset);
    }

    public FloatingHealthBar(Entity owner, float width, float height, float yOffset) {
        this.owner = owner;
        this.pixel = new TextureRegion(Main.game.pixel);

        this.size.set(width, height);
        this.layer = RenderLayer.OVERLAY;
        this.order = 100;
        this.origin.set(width / 2f, -yOffset);

        Health health = Health.get(owner);
        if (health != null && health.max > 0) {
            this.targetPct = MathUtils.clamp((float) health.current / health.max, 0f, 1f);
            this.trailingPct = targetPct;
        }
    }

    public float getTrailingWorldX(float entityWorldX) {
        float barLeft = entityWorldX - origin.x * scale.x;
        float barWidth = size.x * scale.x;
        return barLeft + (barWidth * trailingPct);
    }

    @Override
    public void render(Batch batch, float x, float y) {
        Health health = Health.get(owner);
        if (health == null || health.isDead()) {
            return;
        }

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
