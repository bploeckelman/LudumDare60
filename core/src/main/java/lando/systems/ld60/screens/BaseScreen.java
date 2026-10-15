package lando.systems.ld60.screens;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
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
import com.zendogames.zenlib.input.ScreenInputProcessor;
import com.zendogames.zenlib.screens.ZenScreen;
import com.zendogames.zenlib.ui.DebugActions;
import com.zendogames.zenlib.ui.DebugViews;
import lando.systems.ld60.game.Factory;

public class BaseScreen extends ZenScreen {

    static final int UNIT = Factory.UNIT;
    static final int WALL = 8;

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

    int zoomIndex = 0;

    protected BaseScreen(ZenGame game, boolean withCollision) {
        super(game);
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
    }

    /** The world's systems after the game-specific ones every subclass adds; call at the end of the constructor. */
    protected void addCommonSystems() {
        world.addSystem(new AnimationSystem());
        world.addSystem(new RenderSystem(batch));
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

    @Override public void update(float dt) {
        world.update(dt);
        afterWorldUpdate(dt);
        super.update(dt);
    }

    /** Between the world's tick and the camera update in {@code ZenScreen.update}: move the camera here. */
    protected void afterWorldUpdate(float dt) {}

    @Override
    public void renderWorld(SpriteBatch batch) {
        batch.begin();
        world.render(Time.delta);
        if (collision != null && ZenFlag.DEBUG_RENDER.on()) {
            CollisionDraw.all(shapes, collision, COLLIDER_COLOR, 1f, world.alpha());
            SensorDraw.all(shapes, world, 1f, world.alpha());
        }
        batch.end();
    }

    @Override public void dispose() {
        world.dispose();
        super.dispose();
    }
}
