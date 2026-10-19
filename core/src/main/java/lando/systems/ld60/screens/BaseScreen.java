package lando.systems.ld60.screens;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.VisImageButton;
import com.zendogames.zenlib.FramePool;
import com.zendogames.zenlib.Time;
import com.zendogames.zenlib.ZenFlag;
import com.zendogames.zenlib.ZenGame;
import com.zendogames.zenlib.assets.ZenAtlas;
import com.zendogames.zenlib.collision.CollisionDraw;
import com.zendogames.zenlib.collision.CollisionWorld;
import com.zendogames.zenlib.collision.SensorDraw;
import com.zendogames.zenlib.ecs.World;
import com.zendogames.zenlib.ecs.systems.AnimationSystem;
import com.zendogames.zenlib.ecs.systems.RenderSystem;
import com.zendogames.zenlib.ecs.systems.SpineSystem;
import com.zendogames.zenlib.input.ScreenInputProcessor;
import com.zendogames.zenlib.screens.ZenScreen;
import com.zendogames.zenlib.ui.DebugActions;
import com.zendogames.zenlib.ui.DebugViews;
import lando.systems.ld60.GameFlags;
import lando.systems.ld60.assets.GameUIAnim;
import lando.systems.ld60.events.*;
import lando.systems.ld60.game.Factory;
import lando.systems.ld60.game.components.Emitter;
import lando.systems.ld60.game.components.FloatingHealthBar;
import lando.systems.ld60.game.components.Shadow;
import lando.systems.ld60.game.components.Television;
import lando.systems.ld60.game.input.Gamepad;
import lando.systems.ld60.game.input.KeyEvents;
import lando.systems.ld60.game.systems.ParticleSystem;
import lando.systems.ld60.ui.ControlsUI;
import lando.systems.ld60.ui.MenuStage;
import lando.systems.ld60.ui.SettingsUI;
import lando.systems.ld60.utils.screenshake.ScreenShakeCameraController;
import lombok.Builder;

public class BaseScreen extends ZenScreen implements SignalListener<ShakeEvent> {

    /** Camera zoom steps for seeing more of a level; 1 is the screen's own view size. */
    static final float[] ZOOMS = { 1f, 1.5f, 2f, 3f, 4f, 6f };

    /** Overlays under {@code 2} are translucent so stacked outlines and whiskers do not hide the sprites. */
    static final float OVERLAY_ALPHA = 0.5f;
    static final Color COLLIDER_COLOR = new Color(1f, 0f, 1f, OVERLAY_ALPHA);

    final World world;
    final Factory factory;
    final CollisionWorld collision; // null when withCollision is false
    final TextureRegion pixel;
    final DebugActions actions;
    public ScreenShakeCameraController shakeController;
    public final ControlsUI controlsUI = new ControlsUI();

    public final SettingsUI settingsUI = new SettingsUI(controlsUI);

    /** {@link #uiStage} as the focus-navigable stage every screen gets from {@link #createUiStage()}. */
    public final MenuStage menu = (MenuStage) uiStage;

    /** The first connected controller read as the player's keys: the ECS reads it through {@code GameKeys}, the UI through {@link #padKeys}. */
    public final Gamepad pad = Gamepad.player();

    /** Sends the pad's key edges to {@link #uiStage}, so focus and buttons answer a controller like the keyboard. */
    final KeyEvents padKeys = new KeyEvents(pad, uiStage, pad.boundKeys());

    int zoomIndex = 0;

    protected BaseScreen(ZenGame game, boolean withCollision) {
        super(game);

        shakeController = new ScreenShakeCameraController(worldCamera);
        ShakeEvent.signal.register(this);
        this.pixel = new TextureRegion(game.pixel);
        this.world = new World();
        this.factory = new Factory(world);
        this.collision = withCollision ? new CollisionWorld(world) : null;
        this.actions = new DebugActions();
        this.debugViews.add(actions);                // 'Screen' section first, after the switcher Main pins
        this.debugViews.add(DebugViews.ecs(world));
        ZenAtlas.SPRITES.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        SensorDraw.TERRAIN.a = OVERLAY_ALPHA;
        SensorDraw.ONE_WAY.a = OVERLAY_ALPHA;
        SensorDraw.THREAT.a  = OVERLAY_ALPHA;
        actions.add("zoom out", Input.Keys.MINUS,  () -> zoom(+1))
               .add("zoom in",  Input.Keys.EQUALS, () -> zoom(-1));
        actions.add("crt shader", Input.Keys.C, GameFlags.CRT_SHADER::toggle);

        uiStage.addActor(settingsUI);
        uiStage.addActor(controlsUI);
        menu.addFocusableActor(settingsUI.crtShaderCheckBox);
        menu.addFocusableActor(settingsUI.invulnerabilityCheckBox);
        menu.addFocusableActor(settingsUI.musicSlider);
        menu.addFocusableActor(settingsUI.soundSlider);
        menu.addFocusableActor(settingsUI.closeButton);
        menu.setEscapeActor(settingsUI.closeButton); // Esc / Backspace / pad B close the window while it is up

        initializeUI();
    }

    /** Every screen's UI stage is a {@link MenuStage}; one with no focusable actors passes every key through untouched. */
    @Override
    protected Stage createUiStage() {
        return new MenuStage(new ScreenViewport(windowCamera), batch);
    }

    /** The world's systems after the game-specific ones every subclass adds; call at the end of the constructor. */
    protected void addCommonSystems() {
        world.addSystem(new AnimationSystem());
        world.addSystem(new SpineSystem());
        world.addSystem(new ParticleSystem(factory));
        world.addSystem(new RenderSystem(batch,
                Emitter.TYPE, FloatingHealthBar.TYPE, Television.TYPE, Shadow.TYPE));
    }

    /** Steps {@link #zoomIndex} through {@link #ZOOMS} and applies it to the world camera. */
    protected void zoom(int steps) {
        zoomIndex = MathUtils.clamp(zoomIndex + steps, 0, ZOOMS.length - 1);
        worldCamera.zoom = ZOOMS[zoomIndex];
        worldCamera.update();
    }

    @Override
    protected ScreenInputProcessor createInputProcessor() {
        return new ScreenInputProcessor(this) {
            @Override protected boolean onTouchDown(int sx, int sy, int pointer, int button) {
                return actions.click((int) this.pointer.x, (int) this.pointer.y);
            }
            @Override protected boolean onKeyDown(int keycode) {
                return actions.key(keycode);
            }
            @Override protected boolean onScrolled(float amountX, float amountY) {
                zoom(amountY > 0 ? +1 : -1); // wheel down zooms out
                return true;
            }
            @Override protected boolean debugKey(int keycode) {
                if (keycode == Input.Keys.NUM_9) {
                    world.step.requestStep();
                    return true;
                }
                return super.debugKey(keycode);
            }
        };
    }

    @Override
    public void alwaysUpdate(float delta) {
        shakeController.update(delta);
    }

    @Override public void update(float dt) {
        padKeys.update();
        world.update(dt);
        afterWorldUpdate(dt);
        super.update(dt);
    }

    /** Between the world's tick and the camera update in {@code ZenScreen.update}: move the camera here. */
    protected void afterWorldUpdate(float dt) {}

    @Override
    protected void renderWorld(Batch batch) {
        batch.setProjectionMatrix(shakeController.getCombinedMatrix());
        batch.begin();
        world.render(Time.delta);
        if (collision != null && ZenFlag.DEBUG_RENDER.on()) {
            CollisionDraw.all(shapes, collision, COLLIDER_COLOR, 1f, world.alpha());
            SensorDraw.all(shapes, world, 1f, world.alpha());
        }
        batch.end();
    }

    @Override
    public void receive(Signal<ShakeEvent> signal, ShakeEvent event) {
        // Add the requested trauma to the controller
        shakeController.addTrauma(event.trauma);
    }

    @Override public void dispose() {
        ShakeEvent.signal.remove(this);
        world.dispose();
        super.dispose();
    }

    @Override
    public void initializeUI() {}

    /**
     * Adds a "Settings" button to the user interface of the screen.
     * The button is placed in the bottom-right corner of the screen and allows the user to toggle
     * the visibility of the settings user interface when clicked.
     *
     * Functionality:
     * - Creates a button using a `TextureRegionDrawable` for visual representation.
     * - Configures the button size and position relative to the current viewport dimensions.
     * - Adds an event listener to handle the click action, which toggles the settings UI visibility.
     * - Adds the button to the stage for rendering and interaction.
     */
    public void addSettingButton() {
        var buttonSize  = 50f;
        var settingsPos = FramePool.vec2(windowCamera.viewportWidth - buttonSize, 0);

        var gearRegion = GameUIAnim.GEAR.get().getKeyFrame(0f);
        var gearDrawable = new TextureRegionDrawable(gearRegion);
        var settingsButton = new VisImageButton(gearDrawable, "Settings");
        settingsButton.setSize(buttonSize, buttonSize);
        settingsButton.setPosition(settingsPos.x, settingsPos.y);
        settingsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                settingsUI.toggle();
            }
        });

        uiStage.addActor(settingsButton);
    }
}
