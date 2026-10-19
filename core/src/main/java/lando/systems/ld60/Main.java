package lando.systems.ld60;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.kotcrab.vis.ui.VisUI;
import com.zendogames.zenlib.*;
import com.zendogames.zenlib.imgui.ImGuiGdx;
import com.zendogames.zenlib.screens.ZenScreen;
import com.zendogames.zenlib.screens.transition.ScreenTransitionEffect;
import lando.systems.ld60.assets.*;
import lando.systems.ld60.events.DistortionEvent;
import lando.systems.ld60.events.Signal;
import lando.systems.ld60.events.SignalListener;
import lando.systems.ld60.game.systems.AudioSystem;
import lando.systems.ld60.screens.LauncherScreen;

public class Main extends ZenGame implements SignalListener<DistortionEvent> {

    public static Main game;

    public Preferences prefs;
    FrameBuffer crtFramebuffer;
    Texture crtTexture;
    AudioSystem audioSystem;

    private float accum;
    private boolean imguiOnce;
    private ShaderProgram loadingShader;

    public Main() {
        Main.game = this;
        this.accum = 0;
        this.imguiOnce = true;
    }

    @Override
    protected void init() {
        prefs = Gdx.app.getPreferences(Config.preferencesName);

        String vert = Gdx.files.internal("shaders/default.vert").readString();
        String frag = Gdx.files.internal("shaders/loading.frag").readString();

        loadingShader = new ShaderProgram(vert,frag);
        if (!loadingShader.isCompiled()) {
            Log.error("Main", "Shader compilation failed: " + loadingShader.getLog());
        }

        GameFlags.INVULNERABILITY.set(prefs.getBoolean("invulnerability", false));
        //ZenFlag.LOG_DEBUG.enable();
        //ZenFlag.LOG_INPUT.enable();
        //ZenFlag.DEBUG_FRAME_POOL.enable();


        if (!VisUI.isLoaded()) {
            VisUI.load(VisUI.SkinScale.X2);
        }

        assets.register(GameAtlas.values());
        assets.register(AnimAtari.values());
        assets.register(AnimCassette.values());
        assets.register(AnimFootball.values());
        assets.register(AnimN64.values());
        assets.register(GameMap.values());
        assets.register(GameSpine.values());
        assets.register(GameFont.values());
        assets.register(GameShader.values());
        assets.register(MusicType.values());
        assets.register(SoundType.values());
        assets.register(GameUIAnim.values());
        assets.register(ParticleAnim.values());
        assets.register(PetAnim.values());

        Flags.register(GameFlags.values());

        crtFramebuffer = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), false);
        crtTexture = crtFramebuffer.getColorBufferTexture();
        audioSystem = new AudioSystem();

        DistortionEvent.signal.register(this);

        prodMode();
    }

    @Override
    public void dispose() {
        super.dispose();
        if (VisUI.isLoaded()) {
            VisUI.dispose();
        }
    }

    @Override
    public ZenConfig config() {
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
        if (imguiOnce && ImGuiGdx.isInitialized() && ZenFlag.SHOW_IMGUI.off() && GameFlags.RELEASE.off()) {
            imguiOnce = false;
            ZenFlag.SHOW_IMGUI.enable();
        }

        if (shakeDuration > 0) {
            shakeDuration -= Time.delta;

            if (shakeDuration <= 0) {
                shakeIntensity = 0f;
                shakeDuration = 0f;
                currentDistortion = 1.0f;
            } else {
// 1. Progress goes from 1.0 down to 0.0
                float progress = shakeDuration / maxDuration;

                // 2. Use a smooth curve to decay the amplitude (pow3Out gives a great spring-like settle)
                float decay = com.badlogic.gdx.math.Interpolation.pow3Out.apply(progress);

                float wobble = MathUtils.sin(accum * 20f);

                // 4. Combine them. (Use + to bulge outward, - to pinch inward)
                currentDistortion = 1.0f + (shakeIntensity * wobble * decay);
            }
        } else {
            currentDistortion = 1.0f;
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
        if (GameFlags.CRT_SHADER.on()) {
            batch.setShader(crtShader);
            crtShader.bind();
        }
        batch.begin();
        batch.setProjectionMatrix(windowCamera.combined);
        crtShader.setUniformf("u_time", accum);
        crtShader.setUniformf("u_kWarp", currentDistortion);
        batch.setColor(Color.WHITE);
        batch.draw(crtTexture, 0, windowCamera.viewportHeight, windowCamera.viewportWidth, -windowCamera.viewportHeight);
        batch.end();

        if (GameFlags.CRT_SHADER.on()) {
            batch.setShader(null);
        }

        ImGuiGdx.render(currentScreen::renderImGui);

    }

    private float shakeDuration = 0f;
    private float shakeIntensity = 0f;
    private float currentDistortion = 1f;
    private float maxDuration = 1f;

    @Override
    public void receive(Signal<DistortionEvent> signal, DistortionEvent event) {
        this.shakeIntensity = Math.max(this.shakeIntensity, event.intensity);
        this.shakeDuration = Math.max(this.shakeDuration, event.duration);
        this.maxDuration = Math.max(this.maxDuration, event.duration);
    }

    @Override
    public void setScreen(ZenScreen newScreen, ScreenTransitionEffect effect, boolean instant) {
        super.setScreen(newScreen, effect, instant);
        DistortionEvent.trigger();
    }

    @Override
    protected void renderLoading(float progress) {
        ScreenUtils.clear(Color.BLACK);
        batch.setProjectionMatrix(windowCamera.combined);
        batch.setShader(loadingShader);
        batch.begin();
        loadingShader.setUniformf("u_progress", progress); // e.g., 0.0f to 1.0f
    // Draw a simple fullscreen quad (the shader generates its own imagery)
        batch.draw(game.pixel, 0, windowCamera.viewportHeight, windowCamera.viewportWidth, -windowCamera.viewportHeight);
        batch.end();
        batch.setShader(null);
    }


    public void prodMode() {
        ZenFlag.GLOBAL.off();
        ZenFlag.FIXED_TIMESTEP.off();
        ZenFlag.LOG_INFO.off();
        ZenFlag.LOG_DEBUG.off();
        ZenFlag.LOG_WARN.off();
        ZenFlag.LOG_INPUT.off();
        ZenFlag.LOG_TIMERS.off();
        ZenFlag.LOG_STATE.off();
        ZenFlag.RENDER_INTERP.off();
        ZenFlag.FRAME_STEP.off();
        ZenFlag.DEBUG_ASSET_LOAD.off();
        ZenFlag.DEBUG_ECS.off();
        ZenFlag.DEBUG_FRAME_POOL.off();
        ZenFlag.DEBUG_RENDER.off();
        ZenFlag.SHOW_IMGUI.off();
    }
}
