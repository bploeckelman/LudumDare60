package lando.systems.ld60.game.components;

import com.zendogames.zenlib.ecs.Component;
import com.zendogames.zenlib.ecs.ComponentType;
import com.zendogames.zenlib.ecs.Entity;

/**
 * The N64 boss's shot in flight: where it is going and how fast. The entity's {@code Position} is
 * the point on the floor the projectile art is drawn from (the same origin and scale as the boss,
 * so the pack starts exactly where the shoot clip showed it); {@link #landX}/{@link #landY} is
 * where that point stops so the fall frames put the pack down on {@link #targetX}/{@link #targetY},
 * the spot the player stood on when the shot was fired. {@code RumblePakState} flies and drops it;
 * {@code N64.explode} does the rest.
 */
public class RumblePak implements Component {

    public static final ComponentType<RumblePak> TYPE = ComponentType.register(RumblePak.class);
    public static RumblePak get(Entity e) { return TYPE.get(e); }
    public static boolean   has(Entity e) { return TYPE.has(e); }

    /** Where the pack comes down: the blast's center. */
    public final int targetX, targetY;

    /** Where the entity stops flying, so the fall frames end on the target. */
    public final int landX, landY;

    /** Pixels per second along the floor. */
    public final float speed;

    /** Exact position; {@code Position} is the rounded copy. */
    public float x, y;

    public RumblePak(int targetX, int targetY, int landX, int landY, float speed, float x, float y) {
        this.targetX = targetX;
        this.targetY = targetY;
        this.landX   = landX;
        this.landY   = landY;
        this.speed   = speed;
        this.x = x;
        this.y = y;
    }
}
