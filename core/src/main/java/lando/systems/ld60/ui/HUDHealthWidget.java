package lando.systems.ld60.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.zendogames.zenlib.ecs.Entity;
import lando.systems.ld60.Main;
import lando.systems.ld60.assets.GameShader;
import lando.systems.ld60.game.components.Health;

import java.util.function.Supplier;

public class HUDHealthWidget extends Widget {

    private final Supplier<Entity> entitySupplier;
    private final Texture whitePixel;

    private float displayPct = 1f;
    private final float lerpSpeed = 0.2f;

    /**
     * @param entitySupplier A function that returns the Entity to track (e.g. () -> screen.player)
     */
    public HUDHealthWidget(Supplier<Entity> entitySupplier) {
        this.entitySupplier = entitySupplier;
        this.whitePixel = Main.game.pixel;
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        Entity trackedEntity = entitySupplier.get();
        if (trackedEntity == null || !trackedEntity.isAlive()) {
            return;
        }

        float targetPct = 0f;
        Health health = Health.get(trackedEntity);
        if (health != null) {
            targetPct = MathUtils.clamp((float) health.current / health.max, 0f, 1f);
        }

        // Smoothly pull display percentage toward the actual target percentage
        if (displayPct > targetPct) {
            displayPct -= lerpSpeed * delta;
            if (displayPct < targetPct) displayPct = targetPct;
        } else if (displayPct < targetPct) {
            displayPct += lerpSpeed * delta;
            if (displayPct > targetPct) displayPct = targetPct;
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        Entity trackedEntity = entitySupplier.get();

        // Only draw if the entity is present and alive
        if (trackedEntity == null || !trackedEntity.isAlive()) {
            return;
        }

        Health health = Health.get(trackedEntity);
        if (health == null) return;

        float x = getX();
        float y = getY();
        float width = getWidth();
        float height = getHeight();

        ShaderProgram shader = GameShader.HEALTH_BAR.get();
        ShaderProgram prevShader = batch.getShader();

        float currentPct = MathUtils.clamp((float) health.current / health.max, 0f, 1f);

        // Apply shader and set the uniforms
        batch.setShader(shader);
        shader.setUniformf("u_healthPct", currentPct);
        shader.setUniformf("u_damagePct", displayPct);
        shader.setUniformf("u_size", width, height);

        // Draw a full-width white pixel; the shader will color and clip it based on the uniforms
        batch.setColor(1f, 1f, 1f, parentAlpha);
        batch.draw(whitePixel, x, y, width, height);

        // Restore the original shader and color
        batch.setShader(prevShader);
        batch.setColor(Color.WHITE);
    }
}
