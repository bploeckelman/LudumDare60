package lando.systems.ld60.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.zendogames.zenlib.assets.ZenAnimHero;
import com.zendogames.zenlib.collision.CollisionMask;
import com.zendogames.zenlib.collision.shapes.CollisionCaps;
import com.zendogames.zenlib.ecs.Entity;
import com.zendogames.zenlib.ecs.World;
import com.zendogames.zenlib.ecs.components.*;
import com.zendogames.zenlib.ecs.movement.Motion;
import com.zendogames.zenlib.ecs.movement.platformer.PlatformerMode;
import com.zendogames.zenlib.ecs.movement.platformer.PlatformerMotion;
import com.zendogames.zenlib.ecs.movement.platformer.PlatformerTuning;
import com.zendogames.zenlib.ecs.movement.topdown.TopDownMotion;
import com.zendogames.zenlib.ecs.movement.topdown.TopDownTuning;
import com.zendogames.zenlib.input.InputControls;
import com.zendogames.zenlib.render.RenderLayer;
import com.zendogames.zenlib.tilemap.TiledProps;
import lando.systems.ld60.assets.EmitterType;
import lando.systems.ld60.assets.GameSpine;
import lando.systems.ld60.game.components.*;
import lando.systems.ld60.game.movement.BrawlerMotion;
import lando.systems.ld60.game.movement.BrawlerTuning;
import lando.systems.ld60.game.movement.FlightMotion;
import lando.systems.ld60.game.state.ExampleEnemyState;
import lando.systems.ld60.game.state.ExamplePlayerState;
import lando.systems.ld60.particles.ParticleEffectParams;

public class Factory {

    /** Tile size: the unit every tuning is authored in. */
    public static final int UNIT = 16;

    // Tiled class names: spawner keys, one per factory method below
    public static final String MARKER     = "marker"; // point objects that other objects reference (teleporter / leaper target); spawns nothing
    public static final String PLAYER     = "player";
    public static final String BOUNCER    = "bouncer";
    public static final String JUMPTHRU   = "jumpthru";
    public static final String SPIKE      = "spike";
    public static final String TELEPORTER = "teleporter";
    public static final String WALKER     = "walker";
    public static final String FLYER      = "flyer";
    public static final String JUMPER     = "jumper";
    public static final String LEAPER     = "leaper";
    public static final String SHOOTER    = "shooter";
    public static final String BALL       = "ball";
    public static final String PATROL     = "patrol";       // simple enemy for overhead example

    /** Seconds a bullet flies before it expires on its own. */
    public static final float BULLET_LIFETIME = 3f;

    /**
     * The grounded enemies' movement: a slow walk that stops almost at once, barely steerable in
     * the air, the player's jump numbers so gravity matches, no jump helpers and no abilities.
     */
    static final PlatformerTuning ENEMY_TUNING = new PlatformerTuning(
            UNIT,
            3, 0.3f, 0.05f,         // ground: speed (tiles/s), accel time, stop time
            1, 0.5f, 0.3f, 22,      // air: speed, accel time, stop time, max fall speed
            5, 0.38f,               // jump: apex height (tiles), time to peak; only its gravity is used
            2.5f, 2f,               // gravity multipliers: jump cut, fall
            0f, 0f,                 // jump helpers: none, nothing presses jump
            PlatformerTuning.Abilities.NONE);

    /** The top-down enemies' movement: half the player's speed, a slow start, stops very quickly. */
    static final TopDownTuning PATROL_TUNING = new TopDownTuning(UNIT, 3, 0.3f, 0.05f);

    /** The scroller's brawlers, player and dummy: ten tiles a second along the street, five up it. */
    static final BrawlerTuning BRAWLER_TUNING = new BrawlerTuning(
            UNIT,    // authoring unit -> pixels
            10,      // x-axis speed (tiles/sec)
            5,       // y-axis speed (tiles/sec)
            0.1f,    // accel time
            0.08f,   // stop time
            2.5f,    // 'jump' height (tiles)
            0.35f);  // 'jump' time to peak (seconds)

    private final World world;
    private final TextureRegion pixel;

    public Factory(World world, TextureRegion pixel) {
        this.world = world;
        this.pixel = pixel;
    }

    // ------------------------------------------------------------------------
    // From map objects: read the Tiled class members here, then build the plain way
    // ------------------------------------------------------------------------

    public Entity platformerPlayer(MapObject object) {
        int x = TiledProps.x(object);
        int y = TiledProps.y(object);
        int w = TiledProps.width(object);
        int cx = x + w / 2;
        return platformerPlayer(cx, y);
    }

    public Entity overheadPlayer(MapObject object) {
        int x = TiledProps.x(object);
        int y = TiledProps.y(object);
        int w = TiledProps.width(object);
        // TODO: probably get height and set pos as cx,cy so origin is center instead of bottom-center
        int cx = x + w / 2;
        return overheadPlayer(cx, y);
    }

    public Entity bouncer(MapObject object) {
        var region = TiledProps.region(object);
        int x      = TiledProps.x(object);
        int y      = TiledProps.y(object);
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
        int x      = TiledProps.x(object);
        int y      = TiledProps.y(object);
        var dir    = TiledProps.enumOf(object, "direction", Direction.class);
        return spike(region, x, y, dir);
    }

    /**
     * {@code target} is a Tiled object reference: libGDX resolves it to the referenced {@code MapObject}
     * at the end of the load ({@code BaseTiledMapLoader.loadObjectProperty}), null when unset, which
     * {@code require} reports as missing. The target's position is the destination.
     */
    public Entity teleporter(MapObject object) {
        var region = TiledProps.region(object);
        int x      = TiledProps.x(object);
        int y      = TiledProps.y(object);
        var target = TiledProps.require(object, "target", MapObject.class);
        var dwell  = TiledProps.require(object, "dwell",  Float.class);
        var effect = new Trigger.Teleport(TiledProps.x(target), TiledProps.y(target), dwell);
        return trigger("teleporter", region, x, y, effect);
    }

    public Entity walker(MapObject object) {
        var region = TiledProps.region(object);
        int x      = TiledProps.x(object);
        int y      = TiledProps.y(object);
        var dir    = GameTiledProps.tileEnumOf(object, "direction", Direction.RIGHT, Direction.class);
        return walker(region, x, y, dir);
    }

    public Entity flyer(MapObject object) {
        var region    = TiledProps.region(object);
        int x         = TiledProps.x(object);
        int y         = TiledProps.y(object);
        var dir       = TiledProps.enumOf(object, "direction", Direction.class);
        var speed     = TiledProps.require(object, "speed",     Float.class);
        var range     = TiledProps.require(object, "range",     Float.class);
        var amplitude = TiledProps.require(object, "amplitude", Float.class);
        var period    = TiledProps.require(object, "period",    Float.class);
        if (period <= 0) throw new GdxRuntimeException("flyer %s: period must be > 0 seconds".formatted(TiledProps.describe(object)));
        return flyer(region, x, y, dir, speed, range, amplitude, period);
    }

    public Entity jumper(MapObject object) {
        var region    = TiledProps.region(object);
        int x         = TiledProps.x(object);
        int y         = TiledProps.y(object);
        var dir       = TiledProps.enumOf(object, "direction", Direction.class);
        var period    = TiledProps.require(object, "period", Float.class);
        var dist      = TiledProps.require(object, "dist",   Float.class);
        var height    = TiledProps.require(object, "height", Float.class);
        return jumper(region, x, y, dir, period, dist, height);
    }

     /** {@code target} is an object reference like the teleporter's: the leaper's second point. */
     public Entity leaper(MapObject object) {
         var region = TiledProps.region(object);
         int x      = TiledProps.x(object);
         int y      = TiledProps.y(object);
         var target = TiledProps.require(object, "target", MapObject.class);
         var period = TiledProps.require(object, "period", Float.class);
         var height = TiledProps.require(object, "height", Float.class);
         return leaper(region, x, y, TiledProps.x(target), TiledProps.y(target), period, height);
     }

     public Entity shooter(MapObject object) {
         var region   = TiledProps.region(object);
         int x        = TiledProps.x(object);
         int y        = TiledProps.y(object);
         var dir      = GameTiledProps.tileEnumOf(object, "direction", Direction.RIGHT, Direction.class);
         var interval = TiledProps.require(object, "interval", Float.class);
         var speed    = TiledProps.require(object, "speed",    Float.class);
         var gravity  = TiledProps.require(object, "gravity",  Boolean.class);
         return shooter(region, x, y, dir, interval, speed, gravity);
     }

     public Entity ball(MapObject object) {
         var region = TiledProps.region(object);
         int x      = TiledProps.x(object);
         int y      = TiledProps.y(object);
         var dir    = GameTiledProps.tileEnumOf(object, "direction", Direction.RIGHT, Direction.class);
         var speed  = TiledProps.require(object, "speed",  Float.class);
         var height = TiledProps.require(object, "height", Float.class);
         return ball(region, x, y, dir, speed, height);
     }

     public Entity patrol(MapObject object) {
         var region = TiledProps.region(object);
         int x      = TiledProps.x(object);
         int y      = TiledProps.y(object);
         var dir    = TiledProps.enumOf(object, "direction", Direction.class);
         // TODO: send position as center x,y instead?
         return patrol(region, x, y, dir);
     }

    // ------------------------------------------------------------------------
    // Plain builders: usable from DebugActions.arm(...) at the pointer
    // ------------------------------------------------------------------------

    public Entity platformerPlayer(int x, int y) {
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

    public Entity overheadPlayer(int x, int y) {
        var controls = InputControls.wasd(com.badlogic.gdx.Input.Keys.SPACE);
        var tuning = new TopDownTuning(
                UNIT,    // authoring unit -> pixels
                6,       // speed (tiles/sec, same for every direction)
                0.08f,   // accel time
                0.08f);  // stop time

        return world.createEntity()
                .add(new Name("player"))
                .add(new Position(x, y))
                .add(new Health(3))
                .add(new Contacts())
                .add(new Input(controls))
                .add(new Animator(ZenAnimHero.IDLE, 8, 0))
                .add(new Behavior(ExamplePlayerState.NORMAL))
                .add(new Mover(new TopDownMotion(tuning)))
                .add(Collider.rect(GameMask.PLAYER, -4, 0, 8, 8) // feet: sprite's top half may overlap a wall behind it
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
        return world.createEntity()
                .add(new Name("trigger:" + name))
                .add(new Position(x, y))
                .add(new Trigger(effect))
                .add(Collider.rect(GameMask.TRIGGER, 0, 0, UNIT, UNIT))
                .add(image);
    }

    public Entity pop(int x, int y) {
        var animator = new Animator(ZenAnimHero.EFFECT_LAND);
        var duration = animator.animation.getAnimationDuration();
        animator.origin.set(8, 0);
        return world.createEntity()
                .add(new Name("pop"))
                .add(new Position(x, y))
                .add(new Lifetime(duration))
                .add(animator);
    }

    /** The Spine example: spineboy standing at {@code (x, y)} (feet), running on track 0. */
    public Entity spineboy(int x, int y) {
        return world.createEntity()
                .add(new Name("spineboy"))
                .add(new Position(x, y))
                .add(new SpineAnimator(GameSpine.SPINEBOY, "run"));
    }

    public Entity spineboyScroller(int x, int y) {
        var controls = InputControls.wasd(com.badlogic.gdx.Input.Keys.SPACE); // the motion's button: jump
        var buttons  = new GameInput(com.badlogic.gdx.Input.Keys.ENTER);      // the states' buttons: attack
        return world.createEntity()
                .add(new Name("spineboy"))
                .add(new Position(x, y))
                .add(new Timers())
                .add(new Health(3))
                .add(new Contacts())
                .add(new Input(controls))
                .add(buttons)
                .add(new SpineAnimator(GameSpine.SPINEBOY, "idle"))
                .add(new Behavior(ExamplePlayerState.NORMAL_SPINEBOY))
                .add(new Mover(new BrawlerMotion(BRAWLER_TUNING)))
                .add(Collider.capsule(GameMask.PLAYER, new Rectangle(-18, 0, 36, 12), CollisionCaps.Axis.HORIZONTAL) // only covers feet: sprite's top half may overlap a wall behind it
                        .blockedBy(GameMask.SOLID)
                        .detects(GameMask.ENEMY, GameMask.TRIGGER, GameMask.HAZARD))
                .add(new Hitboxes(GameHitboxes.SPINEBOY, GameMask.PLAYER, GameMask.ENEMY)); // the body that gets hit, the shot that hits
    }

    /** TEMP: Something to hit in the scroller: a spineboy that stands where it is put, facing {@code facing}, and shoots every two seconds. */
    public Entity dummy(int x, int y, int facing) {
        var timers = new Timers();
        var anim = new SpineAnimator(GameSpine.SPINEBOY, "idle");
        anim.defaultScale.set(0.5f, 0.5f); // the scroller's spineboy size, as ExampleScroller.spawnPlayer sets it
        anim.scale.set(0.5f, 0.5f);
        anim.facing = facing;

        var entity = world.createEntity()
                .add(new Name("dummy"))
                .add(new Position(x, y))
                .add(new Health(5))
                .add(new Mover(new BrawlerMotion(BRAWLER_TUNING)))
                .add(new Hitboxes(GameHitboxes.SPINEBOY, GameMask.ENEMY, GameMask.PLAYER))
                .add(timers)
                .add(anim);

        timers.every("attack", 2f, attack -> ExamplePlayerState.shoot(SpineAnimator.get(attack.entity())));

        return entity;
    }

    // ------------------------------------------------------------------------
    // Enemies: ENEMY (or HAZARD) colliders the player detects; GameContacts does the hurting
    // ------------------------------------------------------------------------

    public Entity walker(TextureRegion region, int x, int y, Direction dir) {
        var input = new Input();
        input.moveX = (dir == Direction.LEFT) ? -1 : 1;

        var whisker = new Sensor.Whisker(-90, UNIT / 2f + 1, 0);
        var sensor  = new Sensor(UNIT / 2f + 4, CollisionMask.of(GameMask.SOLID), 0, whisker);
        sensor.facing = input.moveX;

        return world.createEntity()
                .tag(GameTags.ENEMY)
                .add(new Name("walker"))
                .add(new Position(x, y))
                .add(new Image(region, UNIT, UNIT))
                .add(new Behavior(ExampleEnemyState.WALK))
                .add(new Mover(new PlatformerMotion(ENEMY_TUNING)))
                .add(Collider.rect(GameMask.ENEMY, 0, 0, UNIT, UNIT).blockedBy(GameMask.SOLID))
                .add(input)
                .add(sensor);
    }

    public Entity flyer(TextureRegion region, int x, int y, Direction dir, float speed, float range, float amplitude, float period) {
         var motion = new FlightMotion(x, dir.dx, speed * UNIT, range * UNIT, amplitude * UNIT, period);
         return world.createEntity()
                 .tag(GameTags.ENEMY)
                 .add(new Name("flyer"))
                 .add(new Position(x, y))
                 .add(new Image(region, UNIT, UNIT))
                 .add(new Mover(motion))
                 .add(Collider.rect(GameMask.ENEMY, 0, 0, UNIT, UNIT));
    }

    public Entity jumper(TextureRegion region, int x, int y, Direction dir, float period, float dist, float height) {
         var motion = new PlatformerMotion(ENEMY_TUNING);
         motion.facing = (dir == Direction.LEFT) ? -1 : 1;

         var mover = new Mover(motion);
         mover.onHitX = (ent, mov, hitDir) -> mov.motion(PlatformerMotion.class).facing = -hitDir;

         var timers = new Timers();
         var entity = world.createEntity()
                 .tag(GameTags.ENEMY)
                 .add(new Name("jumper"))
                 .add(new Position(x, y))
                 .add(new Image(region, UNIT, UNIT))
                 .add(Collider.rect(GameMask.ENEMY, 0, 0, UNIT, UNIT).blockedBy(GameMask.SOLID))
                 .add(mover)
                 .add(timers);

         timers.every("hop", period, hop -> {
             var mov = Mover.get(hop.entity());
             var mot = mov.motion(PlatformerMotion.class);
             if (!mot.grounded) return;
             mot.launch(mov, mot.facing, dist, height, /*lock*/ 1f);
         });
         return entity;
    }

     public Entity leaper(TextureRegion region, int x, int y, int targetX, int targetY, float period, float height) {
         var timers = new Timers();
         var entity = world.createEntity()
                 .tag(GameTags.ENEMY)
                 .add(new Name("leaper"))
                 .add(new Position(x, y))
                 .add(new Image(region, UNIT, UNIT))
                 .add(new Mover(new PlatformerMotion(ENEMY_TUNING)))
                 .add(Collider.rect(GameMask.ENEMY, 0, 0, UNIT, UNIT).blockedBy(GameMask.SOLID))
                 .add(timers);

         timers.every("leap", period, leap -> {
             var pos = Position.get(leap.entity());
             var mov = Mover.get(leap.entity());
             var mot = mov.motion(PlatformerMotion.class);
             if (!mot.grounded) return;
             int dCurrent = Math.abs(pos.x - x)       + Math.abs(pos.y - y);
             int dTarget  = Math.abs(pos.x - targetX) + Math.abs(pos.y - targetY);
             var nearSpawn = dCurrent <= dTarget;
             int destX = nearSpawn ? targetX : x;
             int destY = nearSpawn ? targetY : y;
             leap(mov, mot, destX - pos.x, destY - pos.y, height);
         });
         return entity;
     }

     static void leap(Mover mover, PlatformerMotion motion, int dx, int dy, float apex) {
         var tuning = motion.tuning;
         var h = Math.max(dy, 0) / tuning.unit() + apex;
         var v0 = tuning.impulseForHeight(h);
         var g = tuning.gravity();
         var t = (-v0 - (float) Math.sqrt(v0 * v0 + 2f * g * dy)) / g;
         var hang = tuning.hangTime(h);
         var dir = (dx < 0) ? -1 : 1;
         var dist = Math.abs(dx) * hang / (t * tuning.unit());
         motion.launch(mover, dir, dist, h, /*lock*/ (t + 0.1f) / hang);
     }

     public Entity shooter(TextureRegion region, int x, int y, Direction dir, float interval, float speed, boolean gravity) {
         var timers = new Timers();

         var entity = world.createEntity()
                 .tag(GameTags.ENEMY)
                 .add(new Name("shooter"))
                 .add(new Position(x, y))
                 .add(new Image(region, UNIT, UNIT))
                 .add(Collider.rect(GameMask.ENEMY, 0, 0, UNIT, UNIT))
                 .add(timers);

         timers.every("shoot", interval, shoot -> {
             var pos = Position.get(shoot.entity());
             bullet(pos.x + UNIT / 2 + dir.dx * UNIT,
                    pos.y + UNIT / 2 + dir.dy * UNIT,
                    dir.dx * speed * UNIT,
                    dir.dy * speed * UNIT,
                    gravity);
         });
         return entity;
     }

     public Entity bullet(int x, int y, float xVel, float yVel, boolean gravity) {
         var image = new Image(pixel, 4, 4);
         image.origin.set(2, 2);
         image.tint.set(Color.YELLOW);

         var motion = gravity ? new PlatformerMotion(PlatformerTuning.ballistic(UNIT), PlatformerMode.BALLISTIC) : Motion.NONE;
         var mover  = new Mover(motion, xVel, yVel);
         var pop    = (Mover.HitCallback) (ent, mov, hitDir) -> {
             var pos = Position.get(ent);
             pop(pos.x - (int) image.origin.x, pos.y - (int) image.origin.y);
             world.despawn(ent);
         };
         mover.onHitX = pop;
         mover.onHitY = pop;

         return world.createEntity()
                 .tag(GameTags.PROJECTILE)
                 .add(new Name("bullet"))
                 .add(new Position(x, y))
                 .add(new Lifetime(BULLET_LIFETIME))
                 .add(Collider.circ(GameMask.HAZARD, 0, 0, 2).blockedBy(GameMask.SOLID))
                 .add(image)
                 .add(mover);
     }

     public Entity ball(TextureRegion region, int x, int y, Direction dir, float speed, float height) {
         var image = new Image(region, UNIT, UNIT);
         image.origin.set(UNIT / 2f, UNIT / 2f);
         var tuning = PlatformerTuning.ballistic(UNIT);
         var mover  = new Mover(new PlatformerMotion(tuning, PlatformerMode.BALLISTIC), dir.dx * speed * UNIT, 0);
         mover.onHitX = (ent, mov, hitDir) -> mov.xVel = -hitDir * speed * UNIT;
         mover.onHitY = (ent, mov, hitDir) -> { if (hitDir < 0) mov.yVel = tuning.impulseForHeight(height); };

         return world.createEntity()
                 .tag(GameTags.PROJECTILE)
                 .add(new Name("ball"))
                 .add(new Position(x, y))
                 .add(Collider.circ(GameMask.ENEMY, UNIT / 2f, UNIT / 2f, UNIT / 2f).blockedBy(GameMask.SOLID))
                 .add(image)
                 .add(mover);
     }

     public Entity patrol(TextureRegion region, int x, int y, Direction dir) {
         var input = new Input();
         input.moveX = dir.dx;
         input.moveY = dir.dy;

         return world.createEntity()
                 .tag(GameTags.ENEMY)
                 .add(new Name("patrol"))
                 .add(new Position(x, y))
                 .add(new Image(region, UNIT, UNIT))
                 .add(new Behavior(ExampleEnemyState.PATROL))
                 .add(new Mover(new TopDownMotion(PATROL_TUNING)))
                 .add(Collider.rect(GameMask.ENEMY, 0, 0, UNIT, UNIT).blockedBy(GameMask.SOLID))
                 .add(input);
     }

     public Entity emitter(EmitterType type, ParticleEffectParams params) {
         return emitter(type, params, RenderLayer.WORLD);
     }

     public Entity emitter(EmitterType type, ParticleEffectParams params, RenderLayer layer) {
         var emitter = new Emitter(type, params);
         emitter.layer = layer;
         return world.createEntity()
                 .add(new Name("emitter-" + type.name().toLowerCase()))
                 .add(emitter);
     }
}
