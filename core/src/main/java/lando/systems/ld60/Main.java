package lando.systems.ld60;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.ScreenUtils;
import com.kotcrab.vis.ui.VisUI;
import com.zendogames.zenlib.Time;
import com.zendogames.zenlib.ZenConfig;
import com.zendogames.zenlib.ZenFlag;
import com.zendogames.zenlib.ZenGame;
import com.zendogames.zenlib.imgui.ImGuiGdx;
import com.zendogames.zenlib.screens.ZenScreen;
import lando.systems.ld60.assets.GameFont;
import lando.systems.ld60.assets.GameMap;
import lando.systems.ld60.assets.GameAtlas;
import lando.systems.ld60.assets.GameMap;
import lando.systems.ld60.assets.GameSpine;
import lando.systems.ld60.screens.IntroScreen;
import lando.systems.ld60.screens.LauncherScreen;

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
        if (!VisUI.isLoaded()) {
            VisUI.load(VisUI.SkinScale.X2);
        }
        assets.register(GameMap.values());
        assets.register(GameAtlas.values());
        assets.register(GameSpine.values());
        assets.register(GameFont.values());
    }

    @Override
    public void dispose() {
        super.dispose();
        if (VisUI.isLoaded()) {
            VisUI.dispose();
        }
    }

    @Override public ZenConfig config() {
        return Config.zenConfig();
    }

    @Override
    protected ZenScreen createFirstScreen() {
        return new LauncherScreen(this);
    }

    @Override
    protected void update() {
        super.update();
        if (ImGuiGdx.isInitialized() && ZenFlag.SHOW_IMGUI.off()) {
            ZenFlag.SHOW_IMGUI.enable();
        }
    }

    @Override
    protected void draw() {
        ScreenUtils.clear(Color.DARK_GRAY);
        if (screenTransition.inProgress()) {
            screenTransition.render(batch, windowCamera);
        } else {
            currentScreen.renderOffscreenBuffers(batch);
            currentScreen.render(Time.delta);
        }
        ImGuiGdx.render(currentScreen::renderImGui);
    }
}
