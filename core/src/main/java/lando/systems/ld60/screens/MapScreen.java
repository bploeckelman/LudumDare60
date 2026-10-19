package lando.systems.ld60.screens;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.zendogames.zenlib.Log;
import com.zendogames.zenlib.ZenGame;
import com.zendogames.zenlib.collision.ContactResponder;
import com.zendogames.zenlib.ecs.Entity;
import com.zendogames.zenlib.ecs.components.Tilemap;
import com.zendogames.zenlib.ecs.systems.*;
import com.zendogames.zenlib.input.HeldKeys;
import com.zendogames.zenlib.render.CameraFollow;
import com.zendogames.zenlib.render.WorldView;
import com.zendogames.zenlib.screens.transition.ScreenTransitionEffectType;
import com.zendogames.zenlib.tilemap.MapObjectSpawner;
import com.zendogames.zenlib.ui.DebugViews;
import lando.systems.ld60.assets.GameAtlas;
import lando.systems.ld60.assets.GameMap;
import lando.systems.ld60.game.FactoryExample;
import lando.systems.ld60.game.GameContacts;
import lando.systems.ld60.game.GameMask;
import lando.systems.ld60.game.input.GameKeys;
import lando.systems.ld60.game.input.Gamepad;
import lando.systems.ld60.game.systems.GameInputSystem;
import lando.systems.ld60.ui.GameDebugViews;

/**
 * A screen built from a Tiled map: the map whose tagged tiles collide, the systems a moving,
 * colliding, behaving world needs, a camera that follows the player, and the debug views and
 * actions for poking at it. A subclass picks the map, registers which Tiled classes spawn what on
 * {@link #spawner}, then calls {@link #buildLevel()}; it adds its own systems before that and may
 * swap the contact rules with {@link #createContactResponder()}. Input is the keyboard or a gamepad
 * ({@link GameKeys}), so every mover reads one set of keys.
 */
public abstract class MapScreen extends BaseScreen {

    private static final String TAG = MapScreen.class.getSimpleName();

    static final int VIEW_WIDTH  = 320;
    static final int VIEW_HEIGHT = 180;

    final GameMap map;
    final Vector2 mapCenter;
    final HeldKeys keys;
    final ContactSystem contactSystem;
    public final CameraFollow cameraFollow;
    final MapObjectSpawner spawner;

    public Entity player;
    Tilemap tilemap;

    protected MapScreen(ZenGame game, GameMap map) {
        super(game, /*withCollision*/ true);
        this.map = map;
        this.mapCenter = new Vector2();
        this.keys = new GameKeys(inputProcessor, Gamepad.player());
        this.contactSystem = new ContactSystem(collision, createContactResponder());
        this.spawner = new MapObjectSpawner().on(FactoryExample.MARKER, (_, _) -> {}); // point objects other objects reference; nothing to spawn

        world.addSystem(new InputSystem(keys));
        world.addSystem(new GameInputSystem(keys));
        world.addSystem(new SensorSystem(collision));
        world.addSystem(new TimerSystem());
        world.addSystem(new BehaviorSystem());
        world.addSystem(new LifetimeSystem());
        world.addSystem(new MovementSystem(collision));
        world.addSystem(contactSystem);
        addCommonSystems();

        // For consistency with library provided ZenAtlas starting filter
        GameAtlas.SPRITES.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        this.cameraFollow = new CameraFollow(worldCamera);
        worldCamera.position.set(VIEW_WIDTH / 2f, VIEW_HEIGHT / 2f, 0f);

        debugViews.add(DebugViews.tilemaps(world));
        debugViews.add(DebugViews.movers(world));
        debugViews.add(DebugViews.contacts(contactSystem));
        debugViews.add(DebugViews.behaviors(world));
        debugViews.add(DebugViews.sensors(world));
        debugViews.add(DebugViews.timers(world));
        debugViews.add(GameDebugViews.particlePool(world));
        actions .add("back to menu",  Input.Keys.ESCAPE, () -> game.setScreen(new IntroScreen(game), ScreenTransitionEffectType.DOORWAY))
                .add("reload map",    Input.Keys.M, this::reloadMap)
                .add("rebuild level", Input.Keys.F, this::buildLevel)
                .add("pause",         Input.Keys.P, () -> {
                    var animSystem = world.getSystem(AnimationSystem.class);
                    animSystem.enabled = !animSystem.enabled;
                });

        addSettingButton();
    }

    @Override public void update(float dt) {
        if (!settingsUI.isVisible) { // Pause if settings are visible
            world.update(dt);
            afterWorldUpdate(dt);
        }
        uiStage.act(dt);

    }

    /** The rules for this screen's contacts; called from the constructor, so read no subclass fields. The examples' by default. */
    protected ContactResponder createContactResponder() {
        return new GameContacts(world, factory);
    }

    /** Clears the world and spawns the map again: its tile layers, then every object through {@link #spawner}. */
    protected void buildLevel() {
        world.clear();

        player = null;
        tilemap = new Tilemap(map.get());
        tilemap.spawn(world, GameMask.SOLID, batch, worldCamera);
        spawner.spawnAll(tilemap);

        mapCenter.set(tilemap.width() / 2f, tilemap.height() / 2f);
        cameraFollow.bounds = new Rectangle(0, 0, tilemap.width(), tilemap.height());
        if (player != null) cameraFollow.follow(player, /*snap*/ true);

        Log.info(TAG, "built level: %dx%d tiles, %d layers, %d objects, player %s",
                tilemap.cols, tilemap.rows, tilemap.layers.size, tilemap.objects.size, player == null ? "none" : player.toString());
    }

    /** The entity the camera follows; a previous one (a second player object on the map) is despawned. */
    protected void setPlayer(Entity spawned) {
        if (player != null && !player.isRemoving()) {
            world.despawn(player);
        }
        player = spawned;
    }

    /** Save in Tiled, press M: the file is read again and the level rebuilt from it. */
    private void reloadMap() {
        map.reload(assets);
        buildLevel();
    }

    @Override
    protected WorldView createWorldView() {
        var view = new WorldView(worldCamera, VIEW_WIDTH, VIEW_HEIGHT);
        view.clearColor.set(Color.DARK_GRAY);
        return view;
    }

    @Override
    protected void afterWorldUpdate(float dt) {
        // when the followed entity is gone, look at the map instead
        if (cameraFollow.target() instanceof CameraFollow.Target.Follow f && !f.entity().isAlive()) {
            cameraFollow.follow(mapCenter);
        }
        cameraFollow.update(dt);
    }
}
