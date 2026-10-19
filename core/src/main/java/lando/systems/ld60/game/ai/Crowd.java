package lando.systems.ld60.game.ai;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.zendogames.zenlib.ecs.Entity;
import com.zendogames.zenlib.ecs.components.Position;

/**
 * Keeps a mob from standing on one spot. Two pieces, both stateless, both read from positions
 * only, so any brain can use them:
 * <ul>
 * <li>{@link #separation}: a push away from every other fighter closer than {@code radius},
 *     stronger the closer it is. Add it to where the brain wants to go before steering.</li>
 * <li>{@link #rank}: how many other enemies are between this one and the target on its side of
 *     the street. The front one (rank 0) closes in and attacks, the others hang back one spacing
 *     per rank and shuffle forward when the one ahead dies. No slots to allocate or free.</li>
 * </ul>
 */
public final class Crowd {

    private Crowd() {}

    /**
     * Sum over other fighters within {@code radius} of the unit vector away from each, scaled by
     * how close it is (1 at the same spot, 0 at the radius). Two fighters on the exact same pixel
     * push along x by entity id, so they never cancel out.
     */
    public static Vector2 separation(Entity e, Array<Entity> others, float radius, Vector2 out) {
        out.setZero();
        var pos = Position.get(e);
        for (int i = 0; i < others.size; i++) {
            var other = others.get(i);
            if (other == e || !other.isAlive() || other.isRemoving()) continue;
            var op = Position.get(other);
            float dx = pos.x - op.x, dy = pos.y - op.y;
            float d = (float) Math.hypot(dx, dy);
            if (d >= radius) continue;
            if (d < 1f) { dx = (e.id < other.id) ? -1 : 1; dy = 0; d = 1; }
            float weight = 1f - d / radius;
            out.add(dx / d * weight, dy / d * weight);
        }
        return out;
    }

    /**
     * Other enemies (not {@code e}, not {@code target}, same {@code side} of {@code target} along x)
     * that are nearer to the target than {@code e} is. Dead and removing entities do not count.
     */
    public static int rank(Entity e, Entity target, int side, Array<Entity> others) {
        var pos = Position.get(e);
        var t   = Position.get(target);
        int mine = Math.abs(pos.x - t.x);
        int rank = 0;
        for (int i = 0; i < others.size; i++) {
            var other = others.get(i);
            if (other == e || other == target || !other.isAlive() || other.isRemoving()) continue;
            if (other.hasTag(lando.systems.ld60.game.GameTags.PLAYER)) continue;
            var op = Position.get(other);
            if (Integer.signum(op.x - t.x) != side) continue;
            if (Math.abs(op.x - t.x) < mine) rank++;
        }
        return rank;
    }
}
