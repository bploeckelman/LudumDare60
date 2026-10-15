package lando.systems.ld60.screens;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.zendogames.zenlib.FramePool;
import com.zendogames.zenlib.Log;
import com.zendogames.zenlib.ZenGame;
import com.zendogames.zenlib.assets.ZenAnimHero;
import com.zendogames.zenlib.assets.ZenAtlas;
import com.zendogames.zenlib.ecs.Entity;
import com.zendogames.zenlib.ecs.components.Animator;
import com.zendogames.zenlib.ecs.components.Name;
import com.zendogames.zenlib.ecs.components.Position;
import com.zendogames.zenlib.ecs.components.Tilemap;
import com.zendogames.zenlib.ecs.systems.*;
import com.zendogames.zenlib.render.CameraFollow;
import com.zendogames.zenlib.render.WorldView;
import com.zendogames.zenlib.tilemap.MapObjectSpawner;
import com.zendogames.zenlib.tilemap.TiledProps;
import com.zendogames.zenlib.ui.DebugViews;
import lando.systems.ld60.assets.GameAtlas;
import lando.systems.ld60.assets.GameMap;
import lando.systems.ld60.game.GameContacts;
import lando.systems.ld60.game.GameMask;

import java.util.List;

public class ExampleScreen extends BaseScreen {

    private static final String TAG = ExampleScreen.class.getSimpleName();

    static final int VIEW_WIDTH  = 320;
    static final int VIEW_HEIGHT = 180;
    static final int MAP_WIDTH   = 480;
    static final int MAP_HEIGHT  = 480;
    static final int COLUMNS     = 5;
    static final int FRAME       = 16;   // the hero frames are 16x16

    final Array<Entity> cells = new Array<>();
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
        this.contactSystem = new ContactSystem(collision, new GameContacts());

        world.addSystem(new InputSystem(inputProcessor));
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
                .on("player",   this::spawnPlayer)
                .on("bouncer",  this::spawnBouncer)
                .on("jumpthru", this::spawnJumpthru)
                .on("spike",    this::spawnSpike)
        ;

        buildLevel();

        debugViews.add(DebugViews.tilemaps(world));
        debugViews.add(DebugViews.movers(world));
        debugViews.add(DebugViews.contacts(contactSystem));
        debugViews.add(DebugViews.behaviors(world));
        debugViews.add(DebugViews.sensors(world));
        debugViews.add(DebugViews.timers(world));
        actions.add("filter", Input.Keys.F, this::toggleFilter)
                .add("scale",  Input.Keys.S, this::cycleScale)
                .add("pause",  Input.Keys.P, () -> {
                    var animSystem = world.getSystem(AnimationSystem.class);
                    animSystem.enabled = !animSystem.enabled;
                });
    }

    private void buildLevel() {
        world.clear();
        layoutGrid();
        factory.libgdx(0, 0, 378 / 4, 63 / 4);

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

    /** Grid pitch in world units: a frame and a half at the current scale. */
    private int pitch() {
        return FRAME * scale * 3 / 2;
    }

    /** One entity per {@link ZenAnimHero}, rows top to bottom, each frame standing on the lower third of its cell. */
    private void layoutGrid() {
        cells.clear();

        var anims = ZenAnimHero.values();
        var rows  = (anims.length + COLUMNS - 1) / COLUMNS;
        var pitch = pitch();
        var left  = (VIEW_WIDTH  - COLUMNS * pitch) / 2;
        var top   = (VIEW_HEIGHT + rows    * pitch) / 2;

        for (int i = 0; i < anims.length; i++) {
            var anim = anims[i];
            var col  = i % COLUMNS;
            var row  = i / COLUMNS;
            var x    = left + col * pitch + pitch / 2;
            var y    = top  - (row + 1) * pitch + pitch / 3;

            // Origin at the frame's bottom center, like the hero, so the scale grows about the feet
            var animator = new Animator(anim, FRAME / 2f, 0);
            animator.defaultScale.set(scale, scale);
            animator.scale.set(scale, scale);

            var entity = world.createEntity()
                    .add(new Name(anim.name().toLowerCase()))
                    .add(new Position(x, y))
                    .add(animator);
            cells.add(entity);
        }
        Log.debug(TAG, "laid out %d animations at %dx", anims.length, scale);
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

    /** Names in window pixels under each cell, so the font stays legible at any world scale. */
    @Override
    protected void renderUI() {
        super.renderUI();

        var font   = game.defaultFont;
        var layout = game.layout;
        var below  = pitch() / 4f;
        batch.setProjectionMatrix(windowCamera.combined);
        batch.begin();
        for (int i = 0; i < cells.size; i++) {
            var e   = cells.get(i);
            var pos = Position.get(e);
            var at  = worldView.project(pos.x, pos.y - below, FramePool.vec2(0, 0));
            layout.setText(font, Name.of(e), Color.WHITE, 0, Align.center, false);
            font.draw(batch, layout, at.x, at.y);
        }
        batch.end();
    }

    // ------------------------------------------------------------------------
    // Spawns for the map's object classes
    // ------------------------------------------------------------------------

    private void spawnPlayer(Tilemap map, MapObject object) {
        if (player != null && !player.isRemoving()) {
            // Schedule the current player entity for removal before creating a new one
            world.despawn(player);
        }

        int x = TiledProps.x(object);
        int y = TiledProps.y(object);
        player = factory.examplePlayer(x, y);
    }

    private void spawnBouncer(Tilemap map, MapObject object) {
        int x = TiledProps.x(object);
        int y = TiledProps.y(object);
        var h = TiledProps.get(object, "height", 8f, Float.class);
        if (h <= 0) {
            Log.warn(TAG, "bouncer %s has no width, skipped", TiledProps.describe(object));
            return;
        }
        factory.bouncer(pixel, x, y, h);
    }

    private void spawnJumpthru(Tilemap map, MapObject object) {
        int x = TiledProps.x(object);
        int y = TiledProps.y(object);
        int w = (object instanceof PolylineMapObject polyline)
                ? MathUtils.round(polyline.getPolyline().getLength())
                : TiledProps.width(object);
        if (w <= 0) {
            Log.warn(TAG, "jumpthru %s has no width, skipped", TiledProps.describe(object));
            return;
        }
        factory.jumpthru(pixel, x, y, w);
    }

    private void spawnSpike(Tilemap map, MapObject object) {
        int x = TiledProps.x(object);
        int y = TiledProps.y(object);
        var dir = TiledProps.get(object, "dir", "up", String.class);
        var allowedDirs = List.of("up", "down", "left", "right");
        if (!allowedDirs.contains(dir)) {
            Log.warn(TAG, "spike %s has invalid direction (allowed: [%s]), skipped", TiledProps.describe(object), String.join(", ", allowedDirs));
            return;
        }
        factory.spike(pixel, x, y, dir);
    }
}
