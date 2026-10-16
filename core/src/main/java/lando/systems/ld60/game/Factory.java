package lando.systems.ld60.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.zendogames.zenlib.assets.ZenAnimHero;
import com.zendogames.zenlib.ecs.Entity;
import com.zendogames.zenlib.ecs.World;
import com.zendogames.zenlib.ecs.components.*;
import com.zendogames.zenlib.ecs.movement.platformer.PlatformerMotion;
import com.zendogames.zenlib.ecs.movement.platformer.PlatformerTuning;
import com.zendogames.zenlib.input.InputControls;
import com.zendogames.zenlib.tilemap.TiledProps;
import lando.systems.ld60.game.components.Health;
import lando.systems.ld60.game.components.Trigger;

public class Factory {

    /** Tile size: the unit every tuning is authored in. */
    public static final int UNIT = 16;

    // Tiled class names: spawner keys, one per factory method below
    public static final String PLAYER     = "player";
    public static final String BOUNCER    = "bouncer";
    public static final String JUMPTHRU   = "jumpthru";
    public static final String SPIKE      = "spike";
    public static final String TELEPORTER = "teleporter";

    private final World world;
    private final TextureRegion pixel;

    public Factory(World world, TextureRegion pixel) {
        this.world = world;
        this.pixel = pixel;
    }

    // ------------------------------------------------------------------------
    // From map objects: read the Tiled class members here, then build the plain way
    // ------------------------------------------------------------------------

    public Entity player(MapObject object) {
        int x = TiledProps.x(object);
        int y = TiledProps.y(object);
        int w = TiledProps.width(object);
        int cx = x + w / 2;
        return player(cx, y);
    }

    public Entity bouncer(MapObject object) {
        var region = TiledProps.region(object);
        int x = TiledProps.x(object);
        int y = TiledProps.y(object);
        var bounce = TiledProps.require(object, "bounce", Float.class);
        if (bounce <= 0) throw new GdxRuntimeException("bouncer %s: bounce must be > 0 tiles".formatted(TiledProps.describe(object)));
        return bouncer(region, x, y, bounce);
    }

    public Entity jumpthru(MapObject object) {
        int x = TiledProps.x(object);
        int y = TiledProps.y(object);
        int w = (object instanceof PolylineMapObject polyline)
                ? MathUtils.round(polyline.getPolyline().getLength())
                : TiledProps.width(object);
        if (w <= 0) throw new GdxRuntimeException("jumpthru %s: needs a polyline or a width".formatted(TiledProps.describe(object)));
        return jumpthru(x, y, w);
    }

    public Entity spike(MapObject object) {
        var region = TiledProps.region(object);
        int x = TiledProps.x(object);
        int y = TiledProps.y(object);
        var dir = TiledProps.enumOf(object, "direction", Direction.class);
        return spike(region, x, y, dir);
    }

    /**
     * {@code target} is a Tiled object reference: libGDX resolves it to the referenced {@code MapObject}
     * at the end of the load ({@code BaseTiledMapLoader.loadObjectProperty}), null when unset, which
     * {@code require} reports as missing. The target's position is the destination.
     */
    public Entity teleporter(MapObject object) {
        var region = TiledProps.region(object);
        int x = TiledProps.x(object);
        int y = TiledProps.y(object);
        var target = TiledProps.require(object, "target", MapObject.class);
        var dwell  = TiledProps.require(object, "dwell",  Float.class);
        var effect = new Trigger.Teleport(x, y, dwell);
        return trigger("teleporter", region, x, y, effect);
    }

    // ------------------------------------------------------------------------
    // Plain builders: usable from DebugActions.arm(...) at the pointer
    // ------------------------------------------------------------------------

    public Entity player(int x, int y) {
        var ledgeGrab = PlatformerTuning.Abilities.LedgeGrab.PUSH;
        var tuning = new PlatformerTuning(
                UNIT,                   // unit -> pixels, base unit that tuning params are authored in (often tile size)
                7, 0.1f, 0.1f,          // ground: speed (tiles/s), accel time, stop time
                8, 0.2f, 0.2f, 22,      // air: speed, accel time, stop time, max fall speed
                5, 0.38f,               // jump: apex height (tiles), time to peak
                2.5f, 2f,               // gravity multipliers: jump cut, fall
                0.1f, 0.12f,            // jump helpers: buffer time, coyote time
                new PlatformerTuning.Abilities(
                        6, 10, 0.2f,            // wall: slide speed, jump speed x, steer-lock time
                        ledgeGrab, 0.25f, 0.1f, // ledge: grab rule, reach, regrab time
                        20, 0.2f, 0.4f));       // dash: speed, time, cooldown
        var inputControls = InputControls.wasd(com.badlogic.gdx.Input.Keys.SPACE);
        return world.createEntity()
                .add(new Name("player"))
                .add(new Position(x, y))
                .add(new Health(3))
                .add(new Contacts())
                .add(new Animator(ZenAnimHero.IDLE, 8, 0))
                .add(new Behavior(ExamplePlayerState.NORMAL))
                .add(new Mover(new PlatformerMotion(tuning)))
                .add(new Input(inputControls))
                .add(Collider.capsule(GameMask.PLAYER, -4, 0, 8, 16)
                        .blockedBy(GameMask.SOLID)
                        .detects(GameMask.ENEMY, GameMask.TRIGGER, GameMask.HAZARD));
    }

    public Entity bouncer(TextureRegion region, int x, int y, float heightTiles) {
        return trigger("bouncer", region, x, y, new Trigger.Bounce(heightTiles));
    }

    public Entity jumpthru(int x, int y, int w) {
        var image = new Image(pixel, w, 1);
        image.tint.set(Color.SKY);
        return world.createEntity()
                .add(new Name("platform"))
                .add(new Position(x, y))
                .add(Collider.platform(GameMask.SOLID, w))
                .add(image);
    }

    public Entity spike(TextureRegion region, int x, int y, Direction dir) {
        var image = new Image(region, UNIT, UNIT);
        if (region == pixel) image.tint.set(Color.RED);
        // TODO: dir picks the sprite orientation once spikes have art; the hazard box is the full tile for now
        return world.createEntity()
                .add(new Name("spike"))
                .add(new Position(x, y))
                .add(Collider.rect(GameMask.HAZARD, 0, 0, UNIT, UNIT))
                .add(image);
    }

    public Entity trigger(String name, TextureRegion region, int x, int y, Trigger.Effect effect) {
        var image = new Image(region, UNIT, UNIT);
        image.tint.set(Color.ORANGE);
        return world.createEntity()
                .add(new Name("trigger:" + name))
                .add(new Position(x, y))
                .add(new Trigger(effect))
                .add(Collider.rect(GameMask.TRIGGER, 0, 0, UNIT, UNIT))
                .add(image);
    }
}
