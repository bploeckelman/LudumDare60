package lando.systems.ld60.screens;

import com.zendogames.zenlib.ZenGame;
import lando.systems.ld60.assets.GameMap;
import lando.systems.ld60.game.Factory;

/** The platformer example: gravity, jumps, ramps, one-way platforms and every enemy kind in {@link Factory}. */
public class ExamplePlatformer extends ExampleScreen {

    public ExamplePlatformer(ZenGame game) {
        super(game, GameMap.EXAMPLE_PLATFORMER);

        // Tiled class -> what it spawns; an unknown class throws listing these, so a typo in Tiled fails loudly
        spawner .on(Factory.PLAYER,     (map, obj) -> setPlayer(factory.platformerPlayer(obj)))
                .on(Factory.BOUNCER,    (map, obj) -> factory.bouncer(obj))
                .on(Factory.JUMPTHRU,   (map, obj) -> factory.jumpthru(obj))
                .on(Factory.SPIKE,      (map, obj) -> factory.spike(obj))
                .on(Factory.TELEPORTER, (map, obj) -> factory.teleporter(obj))
                .on(Factory.WALKER,     (map, obj) -> factory.walker(obj))
                .on(Factory.FLYER,      (map, obj) -> factory.flyer(obj))
                .on(Factory.JUMPER,     (map, obj) -> factory.jumper(obj))
                .on(Factory.LEAPER,     (map, obj) -> factory.leaper(obj))
                .on(Factory.SHOOTER,    (map, obj) -> factory.shooter(obj))
                .on(Factory.BALL,       (map, obj) -> factory.ball(obj))
        ;
        buildLevel();
    }
}
