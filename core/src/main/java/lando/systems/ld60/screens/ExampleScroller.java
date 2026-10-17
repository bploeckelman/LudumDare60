package lando.systems.ld60.screens;

import com.zendogames.zenlib.ZenGame;
import lando.systems.ld60.assets.GameMap;
import lando.systems.ld60.game.Factory;

public class ExampleScroller extends ExampleScreen {

    public ExampleScroller(ZenGame game) {
        super(game, GameMap.EXAMPLE_SCROLLER);

        spawner.on(Factory.PLAYER, (map, obj) -> setPlayer(factory.overheadPlayer(obj)));

        buildLevel();
    }
}
