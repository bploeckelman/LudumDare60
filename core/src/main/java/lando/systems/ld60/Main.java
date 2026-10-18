package lando.systems.ld60;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.kotcrab.vis.ui.VisUI;
import com.zendogames.zenlib.Time;
import com.zendogames.zenlib.ZenConfig;
import com.zendogames.zenlib.ZenFlag;
import com.zendogames.zenlib.ZenGame;
import com.zendogames.zenlib.imgui.ImGuiGdx;
import com.zendogames.zenlib.screens.ZenScreen;
import lando.systems.ld60.assets.*;
import lando.systems.ld60.screens.IntroScreen;
import lando.systems.ld60.screens.LauncherScreen;

public class Main extends ZenGame {

    public static Main game;
    FrameBuffer crtFramebuffer;
    Texture crtTexture;
    private float accum;

    public Main() {
        Main.game = this;
        this.accum = 0;
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
        assets.register(GameShader.values());
        crtFramebuffer = new FrameBuffer(Pixmap.Format.RGBA8888, Config.windowWidth, Config.windowHeight, false);
        crtTexture = crtFramebuffer.getColorBufferTexture();
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
        accum += Time.delta;
        if (ImGuiGdx.isInitialized() && ZenFlag.SHOW_IMGUI.off()) {
            ZenFlag.SHOW_IMGUI.enable();
        }
    }

    @Override
    protected void draw() {
        crtFramebuffer.begin();
        if (screenTransition.inProgress()) {
            screenTransition.render(batch, windowCamera, crtFramebuffer.getFramebufferHandle());
        } else {
            currentScreen.renderOffscreenBuffers(batch);
            crtFramebuffer.begin();

            currentScreen.render(Time.delta);
        }

        crtFramebuffer.end();
        ScreenUtils.clear(Color.CLEAR);
        var crtShader = GameShader.CRT.get();
        batch.setShader(crtShader);
        crtShader.bind();
        batch.begin();
        batch.setProjectionMatrix(windowCamera.combined);
        crtShader.setUniformf("u_time", accum);
        batch.setColor(Color.WHITE);
        batch.draw(crtTexture, 0, windowCamera.viewportHeight, windowCamera.viewportWidth, -windowCamera.viewportHeight);
        batch.end();

        batch.setShader(null);

        ImGuiGdx.render(currentScreen::renderImGui);

    }
}
