package lando.systems.ld60;

import com.zendogames.zenlib.ZenConfig;
import com.zendogames.zenlib.ZenFlag;
import com.zendogames.zenlib.ZenGame;
import com.zendogames.zenlib.imgui.ImGuiGdx;
import com.zendogames.zenlib.screens.ZenScreen;
import lando.systems.ld60.assets.GameMap;
import lando.systems.ld60.assets.GameAtlas;
import lando.systems.ld60.screens.ExampleScreen;

public class Main extends ZenGame {

    public static Main game;

    public Main() {
        Main.game = this;
    }

    @Override
    protected void init() {
        //ZenFlag.LOG_DEBUG.enable();
        //ZenFlag.LOG_INPUT.enable();
        //ZenFlag.DEBUG_FRAME_POOL.enable();
        assets.register(GameMap.values());
        assets.register(GameAtlas.values());
        // test
    }

    @Override public ZenConfig config() {
        return Config.zenConfig();
    }

    @Override
    protected ZenScreen createFirstScreen() {
        return new ExampleScreen(this);
    }

    @Override
    protected void update() {
        super.update();
        if (ImGuiGdx.isInitialized() && ZenFlag.SHOW_IMGUI.off()) {
            ZenFlag.SHOW_IMGUI.enable();
        }
    }
}
