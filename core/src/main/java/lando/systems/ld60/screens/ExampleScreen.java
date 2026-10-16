package lando.systems.ld60.screens;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.zendogames.zenlib.Log;
import com.zendogames.zenlib.ZenGame;
import com.zendogames.zenlib.assets.ZenAtlas;
import com.zendogames.zenlib.ecs.Entity;
import com.zendogames.zenlib.ecs.components.Tilemap;
import com.zendogames.zenlib.ecs.systems.*;
import com.zendogames.zenlib.render.CameraFollow;
import com.zendogames.zenlib.render.WorldView;
import com.zendogames.zenlib.tilemap.MapObjectSpawner;
import com.zendogames.zenlib.ui.DebugViews;
import lando.systems.ld60.assets.GameAtlas;
import lando.systems.ld60.assets.GameMap;
import lando.systems.ld60.game.Factory;
import lando.systems.ld60.game.GameContacts;
import lando.systems.ld60.game.GameMask;

public class ExampleScreen extends BaseScreen {

    private static final String TAG = ExampleScreen.class.getSimpleName();

    static final int VIEW_WIDTH  = 320;
    static final int VIEW_HEIGHT = 180;
    static final int MAP_WIDTH   = 480;
    static final int MAP_HEIGHT  = 480;
    static final int COLUMNS     = 5;
    static final int FRAME       = 16;   // the hero frames are 16x16

    final ContactSystem contactSystem;
    final CameraFollow cameraFollow;
    final MapObjectSpawner spawner;
    final Vector2 mapCenter;

    int scale = 2;
    Texture.TextureFilter filter = Texture.TextureFilter.Nearest;
    Entity player;
    Tilemap tilemap;

    public ExampleScreen(ZenGame game) {
        super(game, /*withCollision*/ true);
        this.mapCenter = new Vector2();
        this.contactSystem = new ContactSystem(collision, new GameContacts(world, factory));

        world.addSystem(new InputSystem(inputProcessor));
        world.addSystem(new SensorSystem(collision));
        world.addSystem(new TimerSystem());
        world.addSystem(new BehaviorSystem());
        world.addSystem(new LifetimeSystem());
        world.addSystem(new MovementSystem(collision));
        world.addSystem(contactSystem);
        addCommonSystems();

        // For consistency with library provided ZenAtlas starting filter
        GameAtlas.SPRITES.setFilter(filter, filter);

        this.cameraFollow = new CameraFollow(worldCamera);
        worldCamera.position.set(VIEW_WIDTH / 2f, VIEW_HEIGHT / 2f, 0f);

        this.spawner = new MapObjectSpawner()
                .on(Factory.PLAYER,     (map, obj) -> respawnPlayer(obj))
                .on(Factory.BOUNCER,    (map, obj) -> factory.bouncer(obj))
                .on(Factory.JUMPTHRU,   (map, obj) -> factory.jumpthru(obj))
                .on(Factory.SPIKE,      (map, obj) -> factory.spike(obj))
                .on(Factory.TELEPORTER, (map, obj) -> factory.teleporter(obj))
                .on(Factory.WALKER,     (map, obj) -> factory.walker(obj))
                .on(Factory.FLYER,      (map, obj) -> factory.flyer(obj))
                .on(Factory.JUMPER,     (map, obj) -> factory.jumper(obj))
                .on(Factory.LEAPER,     (map, obj) -> factory.leaper(obj))
                .on(Factory.SHOOTER,    (map, obj) -> factory.shooter(obj))
                .on(Factory.BALL,       (map, obj) -> factory.ball(obj))
                .on(Factory.MARKER,     (map, obj) -> {})
        ;

        buildLevel();

        debugViews.add(DebugViews.tilemaps(world));
        debugViews.add(DebugViews.movers(world));
        debugViews.add(DebugViews.contacts(contactSystem));
        debugViews.add(DebugViews.behaviors(world));
        debugViews.add(DebugViews.sensors(world));
        debugViews.add(DebugViews.timers(world));
        actions.add("reload map",     Input.Keys.M, this::reloadMap)
                .add("rebuild level", Input.Keys.F, this::buildLevel)
                .add("filter",        Input.Keys.F, this::toggleFilter)
                .add("scale",         Input.Keys.O, this::cycleScale)
                .add("pause",         Input.Keys.P, () -> {
                    var animSystem = world.getSystem(AnimationSystem.class);
                    animSystem.enabled = !animSystem.enabled;
                });
    }

    private void buildLevel() {
        world.clear();

        player = null;
        tilemap = new Tilemap(GameMap.EXAMPLE.get());
        tilemap.spawn(world, GameMask.SOLID, batch, worldCamera);
        spawner.spawnAll(tilemap);

        mapCenter.set(tilemap.width() / 2f, tilemap.height() / 2f);
        cameraFollow.bounds = new Rectangle(0, 0, tilemap.width(), tilemap.height());
        if (player != null) cameraFollow.follow(player, /*snap*/ true);

        Log.info(TAG, "built level: %dx%d tiles, %d layers, %d objects, hero %s",
                tilemap.cols, tilemap.rows, tilemap.layers.size, tilemap.objects.size, player == null ? "none" : player.toString());
    }

    private void toggleFilter() {
        filter = (filter == Texture.TextureFilter.Nearest) ? Texture.TextureFilter.Linear : Texture.TextureFilter.Nearest;
        ZenAtlas.SPRITES.setFilter(filter, filter);
        GameAtlas.SPRITES.setFilter(filter, filter);
        Log.info(TAG, "atlas filter %s", filter);
    }

    private void cycleScale() {
        scale = (scale >= 4) ? 1 : scale * 2;
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
        // TODO: reverts camera to follow center of map when target is dead (probably not relevant in this example)
        if (cameraFollow.target() instanceof CameraFollow.Target.Follow f && !f.entity().isAlive()) {
            cameraFollow.follow(mapCenter);
        }
        cameraFollow.update(dt);
    }

    private void respawnPlayer(MapObject object) {
        if (player != null && !player.isRemoving()) {
            world.despawn(player);
        }
        player = factory.player(object);
    }

    /** Save in Tiled, press M: the file is read again and the level rebuilt from it. */
    private void reloadMap() {
        GameMap.EXAMPLE.reload(assets);
        buildLevel();
    }
}
