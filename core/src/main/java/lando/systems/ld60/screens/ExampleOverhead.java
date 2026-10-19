package lando.systems.ld60.screens;

import com.zendogames.zenlib.ZenGame;
import com.zendogames.zenlib.collision.ContactResponder;
import lando.systems.ld60.assets.GameMap;
import lando.systems.ld60.game.FactoryExample;
import lando.systems.ld60.game.GameContacts;

/**
 * The top-down example: no gravity, 8-way walking between walls, a patrolling enemy.
 * Spikes, teleporters and the hurt response are the platformer's own, since none of
 * them depend on gravity.
 */
public class ExampleOverhead extends MapScreen {

    private final FactoryExample factoryExample;

    public ExampleOverhead(ZenGame game) {
        super(game, GameMap.EXAMPLE_TOP_DOWN);
        this.factoryExample = new FactoryExample(world, pixel);

        spawner .on(FactoryExample.PLAYER,     (map, obj) -> setPlayer(factoryExample.overheadPlayer(obj)))
                .on(FactoryExample.PATROL,     (map, obj) -> factoryExample.patrol(obj))
                .on(FactoryExample.SPIKE,      (map, obj) -> factoryExample.spike(obj))
                .on(FactoryExample.TELEPORTER, (map, obj) -> factoryExample.teleporter(obj))
        ;

        buildLevel();
    }

    @Override
    protected ContactResponder createContactResponder() {
        var responder = super.createContactResponder();
        if (responder instanceof GameContacts contacts) {
            contacts.factoryExample = factoryExample;
        }
        return responder;
    }
}
