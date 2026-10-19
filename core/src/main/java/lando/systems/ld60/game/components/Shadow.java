package lando.systems.ld60.game.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.zendogames.zenlib.ecs.ComponentType;
import com.zendogames.zenlib.ecs.Entity;
import com.zendogames.zenlib.ecs.components.Renderable;
import lando.systems.ld60.Main;
import lando.systems.ld60.game.combat.Hitbox;

/**
 * A dark ellipse on the floor under a brawler: where its feet are on the street even while the
 * art is lifted by a jump or a knockback, and how deep its hits reach. The ellipse is
 * {@code 2 * Hitbox.DEPTH} tall, so two fighters whose shadows touch top to bottom are within
 * depth of each other's boxes: line the shadows up and the punch lands. It never lifts, and it
 * sorts under everything else on the street, so a shadow is never painted over a fighter.
 * {@code BrawlerPresentSystem} writes {@link #height}; the shadow shrinks and fades a little with it.
 */
public class Shadow extends Renderable {

    public static final ComponentType<Shadow> TYPE = ComponentType.register(Shadow.class);
    public static Shadow  get(Entity e) { return TYPE.get(e); }
    public static boolean has(Entity e) { return TYPE.has(e); }

    /** Under every fighter's {@code order = -y}: the first thing drawn in the world layer. */
    public static final int ORDER = -0x8000;

    /** The ellipse is this much wider than the feet, so it shows past the shoes on both sides. */
    public static final float SPREAD = 1.5f;

    public static final Color COLOR    = new Color(0f, 0f, 0f, 0.45f);
    public static final Color IN_RANGE = new Color(0.7f, 0f, 0f, 0.55f);

    /** At this height the shadow is at its smallest and faintest. */
    public float fadeHeight = 48f;

    /** How far along the street a hit reaches, pixels: about a punch plus the target's body. */
    public float reach = 60f;

    /** An opponent is within {@link #reach} and within depth: the shadow shows {@link #IN_RANGE}. {@code HitSystem} writes it. */
    public boolean inRange;

    /** Height of the art above the floor, pixels (a jump's {@code z}). */
    public float height;

    /** Under feet {@code feetWidth} px across: {@link #SPREAD} times that wide, {@code 2 * Hitbox.DEPTH} px deep (the hit depth band). */
    public Shadow(float feetWidth) {
        this(feetWidth * SPREAD, 2 * Hitbox.DEPTH);
    }

    public Shadow(float width, float depth) {
        this.size.set(width, depth);
        this.origin.set(width / 2f, depth / 2f); // centered on the floor line
        this.order = ORDER;
        this.tint.set(COLOR);
    }

    @Override
    public void render(Batch batch, float x, float y) {
        var lift = MathUtils.clamp(height / fadeHeight, 0f, 1f);
        var s = 1f - 0.3f * lift;
        var shapes = Main.game.shapes; // draws through this same batch, inside the same begin / end
        var color = inRange ? IN_RANGE : tint;
        shapes.setColor(color.r, color.g, color.b, color.a * (1f - 0.5f * lift));
        shapes.filledEllipse(x, y, size.x * scale.x * s / 2f, size.y * scale.y * s / 2f);
    }
}
