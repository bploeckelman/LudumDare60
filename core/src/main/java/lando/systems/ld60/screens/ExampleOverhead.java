package lando.systems.ld60.screens;

import com.zendogames.zenlib.ZenGame;
import lando.systems.ld60.assets.GameMap;
import lando.systems.ld60.game.Factory;

/**
 * The top-down example: no gravity, 8-way walking between walls, a patrolling enemy.
 * Spikes, teleporters and the hurt response are the platformer's own, since none of
 * them depend on gravity.
 */
public class ExampleOverhead extends MapScreen {

    public ExampleOverhead(ZenGame game) {
        super(game, GameMap.EXAMPLE_TOP_DOWN);

        spawner .on(Factory.PLAYER,     (map, obj) -> setPlayer(factory.overheadPlayer(obj)))
                .on(Factory.PATROL,     (map, obj) -> factory.patrol(obj))
                .on(Factory.SPIKE,      (map, obj) -> factory.spike(obj))
                .on(Factory.TELEPORTER, (map, obj) -> factory.teleporter(obj))
        ;

        buildLevel();
    }
}
