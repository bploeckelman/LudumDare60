package lando.systems.ld60.screens;

import com.zendogames.zenlib.ZenGame;
import com.zendogames.zenlib.collision.ContactResponder;
import lando.systems.ld60.assets.GameMap;
import lando.systems.ld60.game.Factory;
import lando.systems.ld60.game.FactoryExample;
import lando.systems.ld60.game.GameContacts;

/** The platformer example: gravity, jumps, ramps, one-way platforms and every enemy kind in {@link Factory}. */
public class ExamplePlatformer extends MapScreen {

    private final FactoryExample factoryExample;

    public ExamplePlatformer(ZenGame game) {
        super(game, GameMap.EXAMPLE_PLATFORMER);
        this.factoryExample = new FactoryExample(world, pixel);

        // Tiled class -> what it spawns; an unknown class throws listing these, so a typo in Tiled fails loudly
        spawner .on(FactoryExample.PLAYER,     (map, obj) -> setPlayer(factoryExample.platformerPlayer(obj)))
                .on(FactoryExample.BOUNCER,    (map, obj) -> factoryExample.bouncer(obj))
                .on(FactoryExample.JUMPTHRU,   (map, obj) -> factoryExample.jumpthru(obj))
                .on(FactoryExample.SPIKE,      (map, obj) -> factoryExample.spike(obj))
                .on(FactoryExample.TELEPORTER, (map, obj) -> factoryExample.teleporter(obj))
                .on(FactoryExample.WALKER,     (map, obj) -> factoryExample.walker(obj))
                .on(FactoryExample.FLYER,      (map, obj) -> factoryExample.flyer(obj))
                .on(FactoryExample.JUMPER,     (map, obj) -> factoryExample.jumper(obj))
                .on(FactoryExample.LEAPER,     (map, obj) -> factoryExample.leaper(obj))
                .on(FactoryExample.SHOOTER,    (map, obj) -> factoryExample.shooter(obj))
                .on(FactoryExample.BALL,       (map, obj) -> factoryExample.ball(obj))
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
