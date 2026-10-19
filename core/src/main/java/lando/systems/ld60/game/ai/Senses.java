package lando.systems.ld60.game.ai;

import com.badlogic.gdx.utils.Array;
import com.zendogames.zenlib.ecs.Entity;
import com.zendogames.zenlib.ecs.Family;
import com.zendogames.zenlib.ecs.World;
import com.zendogames.zenlib.ecs.components.Position;
import lando.systems.ld60.game.GameTags;
import lando.systems.ld60.game.components.Fighter;

/** What a brain can look at: the live players and every fighter in the world. One per world, owned by the factory. */
public final class Senses {

    private final Family players;
    private final Family fighters;

    public Senses(World world) {
        this.players  = world.family(Family.all(GameTags.PLAYER, Position.TYPE));
        this.fighters = world.family(Family.all(Fighter.TYPE, Position.TYPE));
    }

    /** Every fighter, players included, the way a crowd sees it. */
    public Array<Entity> fighters() { return fighters.entities(); }

    /** The live player nearest to {@code e} by street distance, or null. */
    public Entity nearestPlayer(Entity e) {
        var pos = Position.get(e);
        Entity nearest = null;
        int best = Integer.MAX_VALUE;
        var entities = players.entities();
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
