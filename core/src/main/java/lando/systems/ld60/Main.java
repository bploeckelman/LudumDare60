package lando.systems.ld60;

import com.zendogames.zenlib.ZenConfig;
import com.zendogames.zenlib.ZenFlag;
import com.zendogames.zenlib.ZenGame;
import com.zendogames.zenlib.imgui.ImGuiGdx;
import com.zendogames.zenlib.screens.ZenScreen;
import lando.systems.ld60.assets.SpriteAtlas;
import lando.systems.ld60.screens.TestScreen;

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
        assets.register(SpriteAtlas.values());
    }

    @Override public ZenConfig config() {
        return Config.zenConfig();
    }

    @Override
    protected ZenScreen createFirstScreen() {
        return new TestScreen(this);
    }

    @Override
    protected void update() {
        super.update();
        if (ImGuiGdx.isInitialized() && ZenFlag.SHOW_IMGUI.off()) {
            ZenFlag.SHOW_IMGUI.enable();
        }
    }
}