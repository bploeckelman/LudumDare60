package lando.systems.ld60.game.ai;

import com.badlogic.gdx.utils.Array;
import com.zendogames.zenlib.ecs.Entity;
import com.zendogames.zenlib.ecs.Family;
import com.zendogames.zenlib.ecs.World;
import com.zendogames.zenlib.ecs.components.Position;
import lando.systems.ld60.game.GameTags;
import lando.systems.ld60.game.components.Fighter;

/**
 * What a brain can look at: the live players and every fighter in the world. One per world, owned
 * by the factory; the static {@link #nearestPlayerTo} and {@link #fightersIn} are the
 * same views for a state, which has only its entity (the world keeps one family per filter).
 */
public final class Senses {

    private static final Family.Builder PLAYERS  = Family.all(GameTags.PLAYER, Position.TYPE);
    private static final Family.Builder FIGHTERS = Family.all(Fighter.TYPE, Position.TYPE);

    private final Family players;
    private final Family fighters;

    public Senses(World world) {
        this.players  = world.family(PLAYERS);
        this.fighters = world.family(FIGHTERS);
    }

    /** Every fighter, players included, the way a crowd sees it. */
    public Array<Entity> fighters() { return fighters.entities(); }

    /** Every fighter in {@code world}. */
    public static Array<Entity> fightersIn(World world) { return world.family(FIGHTERS).entities(); }

    /** The live player nearest to {@code e} by street distance, or null. */
    public Entity nearestPlayer(Entity e) {
        return nearest(e, players.entities());
    }

    /** The live player nearest to {@code e} in its world, or null. */
    public static Entity nearestPlayerTo(Entity e) {
        return nearest(e, e.world().family(PLAYERS).entities());
    }

    private static Entity nearest(Entity e, Array<Entity> entities) {
        var pos = Position.get(e);
        var nearest = (Entity) null;
        int best = Integer.MAX_VALUE;
        for (int i = 0; i < entities.size; i++) {
            var player = entities.get(i);
            if (player == e || !player.isAlive() || player.isRemoving()) continue;
            var p = Position.get(player);
            int dist = Math.abs(p.x - pos.x) + Math.abs(p.y - pos.y);
            if (dist < best) { best = dist; nearest = player; }
        }
        return nearest;
    }
}
