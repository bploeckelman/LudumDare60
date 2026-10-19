package lando.systems.ld60.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import lando.systems.ld60.Main;
import lando.systems.ld60.game.components.Health;
import lando.systems.ld60.screens.MapScreen;

public class PlayerHealthWidget extends Widget {

    private final MapScreen screen;
    private final Texture whitePixel;

    private float displayPct = 1f;
    private final float lerpSpeed = .5f;

    public PlayerHealthWidget(MapScreen screen) {
        this.screen = screen;
        this.whitePixel = Main.game.pixel;
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        float targetPct = 0f;
        if (screen.player != null && screen.player.isAlive()) {
            Health health = Health.get(screen.player);
            if (health != null) {
                targetPct = MathUtils.clamp((float) health.current / health.max, 0f, 1f);
            }
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
        float x = getX();
        float y = getY();
        float width = getWidth();
        float height = getHeight();

        // 1. Draw Background (Dark Gray/Black border)
        batch.setColor(0.1f, 0.1f, 0.1f, 0.8f * parentAlpha);
        batch.draw(whitePixel, x - 2, y - 2, width + 4, height + 4);

        // 2. Draw Trailing/Lost Health (Dark Red underlay)
        if (displayPct > 0f) {
            batch.setColor(0.6f, 0f, 0f, parentAlpha);
            batch.draw(whitePixel, x, y, width * displayPct, height);
        }

        // 3. Draw Current Actual Health (Green -> Yellow -> Red transition)
        if (screen.player != null && screen.player.isAlive()) {
            Health health = Health.get(screen.player);
            if (health != null) {
                float currentPct = MathUtils.clamp((float) health.current / health.max, 0f, 1f);

                // Math for hue rotation: 100% = Green, 50% = Yellow, 0% = Red
                float r = Math.min(1f, 2f * (1f - currentPct));
                float g = Math.min(1f, 2f * currentPct);
                float b = 0f;

                batch.setColor(r, g, b, parentAlpha);
                batch.draw(whitePixel, x, y, width * currentPct, height);
            }
        }

        // Reset color to white so we don't bleed tints into other UI elements
        batch.setColor(Color.WHITE);
    }
}
