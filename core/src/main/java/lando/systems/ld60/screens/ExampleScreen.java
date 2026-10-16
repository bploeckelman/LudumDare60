package lando.systems.ld60.screens;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.zendogames.zenlib.Log;
import com.zendogames.zenlib.ZenGame;
import com.zendogames.zenlib.ecs.Entity;
import com.zendogames.zenlib.ecs.components.Tilemap;
import com.zendogames.zenlib.ecs.systems.*;
import com.zendogames.zenlib.render.CameraFollow;
import com.zendogames.zenlib.render.WorldView;
import com.zendogames.zenlib.screens.transition.ScreenTransitionEffectType;
import com.zendogames.zenlib.tilemap.MapObjectSpawner;
import com.zendogames.zenlib.ui.DebugViews;
import lando.systems.ld60.assets.GameAtlas;
import lando.systems.ld60.assets.GameMap;
import lando.systems.ld60.game.Factory;
import lando.systems.ld60.game.GameContacts;
import lando.systems.ld60.game.GameMask;

/**
 * What the example screens share: a Tiled map whose tagged tiles collide, the systems a moving,
 * colliding, behaving world needs, a camera that follows the player, and the debug views and
 * actions for poking at it. A subclass picks the map, registers which Tiled classes spawn what on
 * {@link #spawner}, then calls {@link #buildLevel()}. Everything named {@code Example*} is reference
 * material to copy from, not the game.
 */
public abstract class ExampleScreen extends BaseScreen {

    private static final String TAG = ExampleScreen.class.getSimpleName();

    static final int VIEW_WIDTH  = 320;
    static final int VIEW_HEIGHT = 180;

    final GameMap map;
    final ContactSystem contactSystem;
    final CameraFollow cameraFollow;
    final MapObjectSpawner spawner;
    final Vector2 mapCenter;

    Entity player;
    Tilemap tilemap;

    protected ExampleScreen(ZenGame game, GameMap map) {
        super(game, /*withCollision*/ true);
        this.map = map;
        this.mapCenter = new Vector2();
        this.contactSystem = new ContactSystem(collision, new GameContacts(world, factory));
        this.spawner = new MapObjectSpawner()
                .on(Factory.MARKER, (m, obj) -> {}); // point objects other objects reference; nothing to spawn

        world.addSystem(new InputSystem(inputProcessor));
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
        actions .add("back to menu",  Input.Keys.ESCAPE, () -> game.setScreen(new IntroScreen(game), ScreenTransitionEffectType.DOORWAY))
                .add("reload map",    Input.Keys.M, this::reloadMap)
                .add("rebuild level", Input.Keys.F, this::buildLevel)
                .add("pause",         Input.Keys.P, () -> {
                    var animSystem = world.getSystem(AnimationSystem.class);
                    animSystem.enabled = !animSystem.enabled;
                });
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
//
//
//
//package lando.systems.ld60.screens;
//
//import com.badlogic.gdx.Input;
//import com.badlogic.gdx.graphics.Color;
//import com.badlogic.gdx.graphics.Texture;
//import com.badlogic.gdx.maps.MapObject;
//import com.badlogic.gdx.math.Rectangle;
//import com.badlogic.gdx.math.Vector2;
//import com.zendogames.zenlib.Log;
//import com.zendogames.zenlib.ZenGame;
//import com.zendogames.zenlib.assets.ZenAtlas;
//import com.zendogames.zenlib.ecs.Entity;
//import com.zendogames.zenlib.ecs.components.Tilemap;
//import com.zendogames.zenlib.ecs.systems.*;
//import com.zendogames.zenlib.render.CameraFollow;
//import com.zendogames.zenlib.render.WorldView;
//import com.zendogames.zenlib.tilemap.MapObjectSpawner;
//import com.zendogames.zenlib.ui.DebugViews;
//import lando.systems.ld60.assets.GameAtlas;
//import lando.systems.ld60.assets.GameMap;
//import lando.systems.ld60.game.Factory;
//import lando.systems.ld60.game.GameContacts;
//import lando.systems.ld60.game.GameMask;
//
//public class ExampleScreen extends BaseScreen {
//
//    private static final String TAG = ExampleScreen.class.getSimpleName();
//
//    static final int VIEW_WIDTH  = 320;
//    static final int VIEW_HEIGHT = 180;
//    static final int MAP_WIDTH   = 480;
//    static final int MAP_HEIGHT  = 480;
//    static final int COLUMNS     = 5;
//    static final int FRAME       = 16;   // the hero frames are 16x16
//
//    final ContactSystem contactSystem;
//    final CameraFollow cameraFollow;
//    final MapObjectSpawner spawner;
//    final Vector2 mapCenter;
//
//    int scale = 2;
//    Texture.TextureFilter filter = Texture.TextureFilter.Nearest;
//    Entity player;
//    Tilemap tilemap;
//
//    public ExampleScreen(ZenGame game) {
//        super(game, /*withCollision*/ true);
//        this.mapCenter = new Vector2();
//        this.contactSystem = new ContactSystem(collision, new GameContacts(world, factory));
//
//        world.addSystem(new InputSystem(inputProcessor));
//        world.addSystem(new SensorSystem(collision));
//        world.addSystem(new TimerSystem());
//        world.addSystem(new BehaviorSystem());
//        world.addSystem(new LifetimeSystem());
//        world.addSystem(new MovementSystem(collision));
//        world.addSystem(contactSystem);
//        addCommonSystems();
//
//        // For consistency with library provided ZenAtlas starting filter
//        GameAtlas.SPRITES.setFilter(filter, filter);
//
//        this.cameraFollow = new CameraFollow(worldCamera);
//        worldCamera.position.set(VIEW_WIDTH / 2f, VIEW_HEIGHT / 2f, 0f);
//
//        this.spawner = new MapObjectSpawner()
//                .on(Factory.PLAYER,     (map, obj) -> respawnPlayer(obj))
//                .on(Factory.BOUNCER,    (map, obj) -> factory.bouncer(obj))
//                .on(Factory.JUMPTHRU,   (map, obj) -> factory.jumpthru(obj))
//                .on(Factory.SPIKE,      (map, obj) -> factory.spike(obj))
//                .on(Factory.TELEPORTER, (map, obj) -> factory.teleporter(obj))
//                .on(Factory.WALKER,     (map, obj) -> factory.walker(obj))
//                .on(Factory.FLYER,      (map, obj) -> factory.flyer(obj))
//                .on(Factory.JUMPER,     (map, obj) -> factory.jumper(obj))
//                .on(Factory.LEAPER,     (map, obj) -> factory.leaper(obj))
//                .on(Factory.SHOOTER,    (map, obj) -> factory.shooter(obj))
//                .on(Factory.BALL,       (map, obj) -> factory.ball(obj))
//                .on(Factory.MARKER,     (map, obj) -> {})
//        ;
//
//        buildLevel();
//
//        debugViews.add(DebugViews.tilemaps(world));
//        debugViews.add(DebugViews.movers(world));
//        debugViews.add(DebugViews.contacts(contactSystem));
//        debugViews.add(DebugViews.behaviors(world));
//        debugViews.add(DebugViews.sensors(world));
//        debugViews.add(DebugViews.timers(world));
//        actions.add("reload map",     Input.Keys.M, this::reloadMap)
//                .add("rebuild level", Input.Keys.F, this::buildLevel)
//                .add("filter",        Input.Keys.F, this::toggleFilter)
//                .add("scale",         Input.Keys.O, this::cycleScale)
//                .add("pause",         Input.Keys.P, () -> {
//                    var animSystem = world.getSystem(AnimationSystem.class);
//                    animSystem.enabled = !animSystem.enabled;
//                });
//    }
//
//    private void buildLevel() {
//        world.clear();
//
//        player = null;
//        tilemap = new Tilemap(GameMap.EXAMPLE.get());
//        tilemap.spawn(world, GameMask.SOLID, batch, worldCamera);
//        spawner.spawnAll(tilemap);
//
//        mapCenter.set(tilemap.width() / 2f, tilemap.height() / 2f);
//        cameraFollow.bounds = new Rectangle(0, 0, tilemap.width(), tilemap.height());
//        if (player != null) cameraFollow.follow(player, /*snap*/ true);
//
//        Log.info(TAG, "built level: %dx%d tiles, %d layers, %d objects, hero %s",
//                tilemap.cols, tilemap.rows, tilemap.layers.size, tilemap.objects.size, player == null ? "none" : player.toString());
//    }
//
//    private void toggleFilter() {
//        filter = (filter == Texture.TextureFilter.Nearest) ? Texture.TextureFilter.Linear : Texture.TextureFilter.Nearest;
//        ZenAtlas.SPRITES.setFilter(filter, filter);
//        GameAtlas.SPRITES.setFilter(filter, filter);
//        Log.info(TAG, "atlas filter %s", filter);
//    }
//
//    private void cycleScale() {
//        scale = (scale >= 4) ? 1 : scale * 2;
//        buildLevel();
//    }
//
//    @Override
//    protected WorldView createWorldView() {
//        var view = new WorldView(worldCamera, VIEW_WIDTH, VIEW_HEIGHT);
//        view.clearColor.set(Color.DARK_GRAY);
//        return view;
//    }
//
//    @Override
//    protected void afterWorldUpdate(float dt) {
//        // TODO: reverts camera to follow center of map when target is dead (probably not relevant in this example)
//        if (cameraFollow.target() instanceof CameraFollow.Target.Follow f && !f.entity().isAlive()) {
//            cameraFollow.follow(mapCenter);
//        }
//        cameraFollow.update(dt);
//    }
//
//    private void respawnPlayer(MapObject object) {
//        if (player != null && !player.isRemoving()) {
//            world.despawn(player);
//        }
//        player = factory.player(object);
//    }
//
//    /** Save in Tiled, press M: the file is read again and the level rebuilt from it. */
//    private void reloadMap() {
//        GameMap.EXAMPLE.reload(assets);
//        buildLevel();
//    }
//}
