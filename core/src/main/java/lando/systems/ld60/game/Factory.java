package lando.systems.ld60.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.zendogames.zenlib.Log;
import com.zendogames.zenlib.assets.ZenAnimHero;
import com.zendogames.zenlib.collision.CollisionMask;
import com.zendogames.zenlib.collision.shapes.CollisionCaps;
import com.zendogames.zenlib.ecs.Entity;
import com.zendogames.zenlib.ecs.Family;
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
import lando.systems.ld60.assets.SoundType;
import lando.systems.ld60.events.AudioEvent;
import lando.systems.ld60.game.anim.AnimClip;
import lando.systems.ld60.game.combat.Weapon;
import lando.systems.ld60.game.combat.Weapons;
import lando.systems.ld60.game.components.*;
import lando.systems.ld60.game.input.GameControls;
import lando.systems.ld60.game.movement.BrawlerMotion;
import lando.systems.ld60.game.movement.BrawlerTuning;
import lando.systems.ld60.game.movement.FlightMotion;
import lando.systems.ld60.game.state.BrawlerState;
import lando.systems.ld60.game.state.ExampleEnemyState;
import lando.systems.ld60.game.state.ExamplePlayerState;
import lando.systems.ld60.particles.ParticleEffectParams;
import lando.systems.ld60.particles.effects.FootStepDustCloudEffect;

import java.util.List;

public class Factory {

    /** Tile size: the unit every tuning is authored in. */
    public static final int UNIT = 16;

    // Tiled class names: spawner keys, one per factory method below
    public static final String MARKER     = "marker"; // point objects that other objects reference (teleporter / leaper target); spawns nothing
    public static final String PLAYER     = "player";
    public static final String DUMMY      = "dummy";
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
    public static final String WALL       = "wall";
    public static final String ENEMY      = "enemy";  // scroller: a thug (member 'kind' picks which, 'weapon' arms it)
    public static final String PICKUP     = "pickup"; // scroller: a weapon on the floor (member 'weapon')
    public static final String TELEVISION = "television"; // scroller: a region where the game world framebuffer is rendered back into for a "watch yourself play" effect

    public static final List<String> ENEMY_KINDS = List.of("thug", /*TEMP: testing*/ "awol");

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
    public static final BrawlerTuning BRAWLER_PLAYER_TUNING = new BrawlerTuning(
            UNIT,    // authoring unit -> pixels
            10,      // x-axis speed (tiles/sec)
            5,       // y-axis speed (tiles/sec)
            0.1f,    // accel time
            0.08f,   // stop time
            2.5f,    // 'jump' height (tiles)
            0.35f);  // 'jump' time to peak (seconds)

    /** The scroller's brawlers, thug: slower than player so they don't overwhelm immediately. */
    public static final BrawlerTuning BRAWLER_THUG_TUNING = new BrawlerTuning(
            UNIT,    // authoring unit -> pixels
            5,       // x-axis speed (tiles/sec)
            2,       // y-axis speed (tiles/sec)
            0.1f,    // accel time
            0.08f,   // stop time
            2.5f,    // 'jump' height (tiles)
            0.35f);  // 'jump' time to peak (seconds)

    /** Every live player, for the enemies' AI to look for. */
    private final Family players;

    private final World world;
    private final TextureRegion pixel;

    public Factory(World world, TextureRegion pixel) {
        this.world = world;
        this.pixel = pixel;
        this.players = world.family(Family.all(GameTags.PLAYER, Position.TYPE));
    }

    // ------------------------------------------------------------------------
    // From map objects: read the Tiled class members here, then build the plain way
    // ------------------------------------------------------------------------

    /** The scroller's player object: feet at the bottom center. */
    public Entity hero(MapObject object) {
        int x = TiledProps.x(object);
        int y = TiledProps.y(object);
        int w = TiledProps.width(object);
        return hero(x + w / 2, y);
    }

    /** The scroller's rank and file: {@code kind} picks the build (only "thug") so far), {@code weapon} (optional) arms it. */
    public Entity enemy(MapObject object) {
        int x      = TiledProps.x(object);
        int y      = TiledProps.y(object);
        int w      = TiledProps.width(object);
        var kind   = TiledProps.get(object, "kind", "thug", String.class);
        var weapon = TiledProps.get(object, "weapon", "", String.class);
        if (!ENEMY_KINDS.contains(kind)) {
            throw new GdxRuntimeException("enemy %s: unknown kind '%s', kinds available: %s"
                    .formatted(TiledProps.describe(object), kind, ENEMY_KINDS));
        }
        var enemy = switch (kind) {
            case "thug" -> thug(x + w / 2, y, -1);
            case "awol" -> awol(x + w / 2, y, -1);
            default -> {
                throw new GdxRuntimeException("enemy %s: unknown kind '%s', kinds available: %s"
                        .formatted(TiledProps.describe(object), kind, ENEMY_KINDS));
            }
        };
        if (!weapon.isEmpty()) {
            Weapons.arm(enemy, GameWeapons.byName(weapon));
        }
        return enemy;
    }

    /** A weapon on the floor; {@code weapon} names one of the {@link GameWeapons#ALL}. */
    public Entity pickup(MapObject object) {
        int x = TiledProps.x(object);
        int y = TiledProps.y(object);
        int w = TiledProps.width(object);
        var weapon = GameWeapons.byName(TiledProps.require(object, "weapon", String.class));
        return pickup(weapon, x + w / 2, y);
    }

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

     /** A rectangle drawn over the background art in Tiled: a static SOLID box that colliders can't enter. */
     public Entity wall(MapObject object) {
         int x = TiledProps.x(object);
         int y = TiledProps.y(object);
         int w = TiledProps.width(object);
         int h = TiledProps.height(object);
         if (w <= 0 || h <= 0) {
             throw new GdxRuntimeException("wall %s: draw it as a rectangle with a width and height".formatted(TiledProps.describe(object)));
         }
         return world.createEntity()
                 .add(new Name("wall"))
                 .add(new Position(x, y))
                 .add(Collider.rect(GameMask.SOLID, 0, 0, w, h));
     }

     public Entity television(MapObject object, TelevisionFeed televisions) {
         int x = TiledProps.x(object);
         int y = TiledProps.y(object);
         int w = TiledProps.width(object);
         int h = TiledProps.height(object);
         return television(televisions, x, y, w, h);
     }

    // ------------------------------------------------------------------------
    // The scroller: brawlers are components all the way down; only the player has states
    // ------------------------------------------------------------------------

    /** spineboy at the scroller's size: load scale 0.25 in {@link GameSpine}, half size again per entity, about 85 px tall. */
    protected SpineAnimator spineboyAnimator(int facing) {
        var anim = new SpineAnimator(GameSpine.SPINEBOY, "idle");
        anim.defaultScale.set(0.5f, 0.5f);
        anim.scale.set(0.5f, 0.5f);
        anim.facing = facing;
        return anim;
    }

    /** spineboy's clips for what the motion does; one instance per entity (components are not shared). */
    static BrawlerAnimClips spineboyClips() {
        return new BrawlerAnimClips(
                AnimClip.spine("idle"),
                AnimClip.spine("walk"),
                AnimClip.spine("run"),
                AnimClip.spine("jump"),
                BRAWLER_PLAYER_TUNING.maxSpeedX() * 0.5f)
                .stop(AnimClip.spine("run-to-idle"))
                .dead(AnimClip.spine("death"));
    }

    /** The awol rig's clips: 'knockback' while thrown through the air, 'death' once and hold; no run-to-idle clip, so no stop. */
    static BrawlerAnimClips awolClips() {
        return new BrawlerAnimClips(
                AnimClip.spine("idle"),
                AnimClip.spine("walk"),
                AnimClip.spine("run"),
                AnimClip.spine("jump"),
                BRAWLER_THUG_TUNING.maxSpeedX())
                .hurt(AnimClip.spine("knockback"))
                .dead(AnimClip.spine("death"));
    }

    /** The awol rig's flinch-in-place for a hit that keeps it on the floor (a lift plays {@code knockback} from the present system instead). */
    static final AnimClip AWOL_FLINCH = AnimClip.spine("hurt");

    static SpineAnimator awolAnimator(int facing) {
        var anim = new SpineAnimator(GameSpine.AWOL, "idle");
        anim.defaultScale.set(0.35f, 0.35f);
        anim.scale.set(0.35f, 0.35f);
        anim.facing = facing;
        return anim;
    }

    /** The player: a brawler on the keyboard (or pad) with the states that read its buttons; hits ask for HURT and DEAD through its Hurtable. */
    public Entity hero(int x, int y) {
        var hurtable = new Hurtable()
                .hurtState(BrawlerState.HURT)
                .deadState(BrawlerState.DEAD)
                .deathTint(null); // the death clip is the look
        var boxes = new Hitboxes(GameWeapons.BLASTER.set(), GameMask.PLAYER, GameMask.ENEMY);
        boxes.onStrike = (e, strike) -> Weapons.wear(e);
        return world.createEntity()
                .tag(GameTags.PLAYER)
                .add(new Name("hero"))
                .add(new Position(x, y))
                .add(new Timers())
                .add(new Health(3))
                .add(new Contacts())
                .add(new Input(GameControls.PLAYER))
                .add(GameControls.playerButtons())
                .add(spineboyAnimator(1))
                .add(spineboyClips())
                .add(new Held(GameWeapons.BLASTER))
                .add(hurtable)
                .add(new Behavior(BrawlerState.NORMAL))
                .add(new Mover(new BrawlerMotion(BRAWLER_PLAYER_TUNING)))
                .add(Collider.capsule(GameMask.PLAYER, new Rectangle(-18, 0, 36, 12), CollisionCaps.Axis.HORIZONTAL) // only covers feet: sprite's top half may overlap a wall behind it
                        .blockedBy(GameMask.SOLID)
                        .detects(GameMask.TRIGGER, GameMask.PICKUP))
                .add(boxes);
    }

    /**
     * A thug: the rank and file, with no states at all. A {@code Timers} beat is its brain (close in
     * on the nearest player, shoot when lined up, grab a weapon it stands on), its {@link Hurtable}
     * says what hits do (flash, fly, die, vanish after a moment, and stop thinking), the
     * {@code BrawlerPresentSystem} animates it from its clips. Everything else is a component.
     */
    public Entity thug(int x, int y, int facing) {
        var timers = new Timers();
        var motion = new BrawlerMotion(BRAWLER_THUG_TUNING);
        motion.facing = facing;
        var hurtable = new Hurtable()
                .despawnAfter(1.5f)
                .onDeath((e, strike) -> { timers.cancel("think"); Input.get(e).reset(); });
        var thug = world.createEntity()
                .tag(GameTags.ENEMY)
                .add(new Name("thug"))
                .add(new Position(x, y))
                .add(new Health(5))
                .add(new Contacts())
                .add(new Input())
                .add(spineboyAnimator(facing))
                .add(spineboyClips())
                .add(new Held(GameWeapons.BLASTER))
                .add(hurtable)
                .add(new Mover(motion))
                .add(Collider.capsule(GameMask.ENEMY, new Rectangle(-18, 0, 36, 12), CollisionCaps.Axis.HORIZONTAL)
                        .blockedBy(GameMask.SOLID)
                        .detects(GameMask.PICKUP))
                .add(new Hitboxes(GameWeapons.BLASTER.set(), GameMask.ENEMY, GameMask.PLAYER))
                .add(timers);

        thug.add(new FloatingHealthBar(thug, 90f));

        timers.every("think", 0.25f, think -> {
            var e      = think.entity();
            var input  = Input.get(e);
            var target = nearestPlayer(e);
            if (target == null) { input.moveX = 0; input.moveY = 0; return; }

            var item = Weapons.under(e);
            if (item != null) Weapons.pickUp(e, item);

            var pos  = Position.get(e);
            var tPos = Position.get(target);
            int dx = tPos.x - pos.x;
            int dy = tPos.y - pos.y;
            int reach = 3 * UNIT;
            input.moveX = (Math.abs(dx) > reach) ? Integer.signum(dx) : 0;
            input.moveY = (Math.abs(dy) > 6)     ? Integer.signum(dy) : 0;
            if (input.moveX == 0 && input.moveY == 0 && Math.abs(dx) <= reach + UNIT / 2) {
                if (dx != 0) motion.facing = Integer.signum(dx);
                var attack = Weapons.attackOf(e);
                if (attack != null && !attack.playing(e) && motion.grounded && !motion.launched) {
                    attack.start(e);
                }
            }
        });

        return thug;
    }

    public Entity awol(int x, int y, int facing) {
        var timers = new Timers();
        var motion = new BrawlerMotion(BRAWLER_THUG_TUNING);
        motion.facing = facing;
        var hurtable = new Hurtable()
                .despawnAfter(1.5f)
                .onHurt((e, strike) -> { if (motion.grounded) AWOL_FLINCH.start(e, false); }) // shoved along the floor: flinch; the present system leaves a clip it doesn't own alone until it completes
                .onDeath((e, strike) -> { timers.cancel("think"); Input.get(e).reset(); });

        var anim = awolAnimator(facing);
        var entity = world.createEntity()
                .tag(GameTags.ENEMY)
                .add(new Name("awol"))
                .add(new Position(x, y))
                .add(new Health(3))
                .add(new Contacts())
                .add(new Input())
                .add(new Held(GameWeapons.AWOL_FIST))
                .add(new Mover(motion))
                .add(Collider.capsule(GameMask.ENEMY, new Rectangle(-18, 0, 36, 12), CollisionCaps.Axis.HORIZONTAL)
                        .blockedBy(GameMask.SOLID)
                        .detects(GameMask.PICKUP))
                .add(new Hitboxes(GameWeapons.AWOL_FIST.set(), GameMask.ENEMY, GameMask.PLAYER))
                .add(anim)
                .add(awolClips())
                .add(hurtable)
                .add(timers);

        entity.add(new FloatingHealthBar(entity, 90f));

        // The rig's events: "footstep" goes to a dust emitter; the rest are logged and play stand-in sounds until there are real ones
        emitter(EmitterType.DUST, new FootStepDustCloudEffect.Params(entity));
        anim.onEvent(event -> {
            var name = event.getData().getName();
            Log.info("awol", "event '%s' at %.2fs of '%s'", name, event.getTime(), AnimClip.current(entity));
            switch (name) {
                case "swing" -> AudioEvent.playSound(SoundType.BELL_HIGH);
                case "thud"  -> AudioEvent.playSound(SoundType.BELL_LOW);
                default -> {} // "jump", "hurt": a landed hit is better heard from CombatEvent, which knows whether a glove connected
            }
        });

        timers.every("think", 0.25f, think -> {
            var e      = think.entity();
            var input  = Input.get(e);
            var target = nearestPlayer(e);
            if (target == null) { input.moveX = 0; input.moveY = 0; return; }

            var item = Weapons.under(e);
            if (item != null) Weapons.pickUp(e, item);

            var pos  = Position.get(e);
            var tPos = Position.get(target);
            int dx = tPos.x - pos.x;
            int dy = tPos.y - pos.y;
            int reach = 3 * UNIT;
            input.moveX = (Math.abs(dx) > reach) ? Integer.signum(dx) : 0;
            input.moveY = (Math.abs(dy) > 6)     ? Integer.signum(dy) : 0;
            if (input.moveX == 0 && input.moveY == 0 && Math.abs(dx) <= reach + UNIT / 2) {
                if (dx != 0) motion.facing = Integer.signum(dx);
                var attack = Weapons.attackOf(e);
                if (attack != null && !attack.playing(e) && motion.grounded && !motion.launched) {
                    attack.start(e);
                }
            }
        });

        return entity;
    }

    /** A weapon lying on the floor at {@code (x, y)}. */
    public Entity pickup(Weapon weapon, int x, int y) {
        return Weapons.spawnPickup(world, weapon, x, y);
    }

    /** The live player nearest to {@code e} along the street, or null. */
    private Entity nearestPlayer(Entity e) {
        var pos = Position.get(e);

        var nearest = (Entity) null;
        int best = Integer.MAX_VALUE;

        var entities = players.entities();
        for (int i = 0; i < entities.size; i++) {
            var playerEntity = entities.get(i);
            if (playerEntity == e || playerEntity.isRemoving()) continue;

            var playerPos = Position.get(playerEntity);
            int dist = Math.abs(playerPos.x - pos.x) + Math.abs(playerPos.y - pos.y);
            if (dist < best) {
                best = dist;
                nearest = playerEntity;
            }
        }
        return nearest;
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
        var hitboxes = new Hitboxes(GameHitboxes.SPINEBOY, GameMask.PLAYER, GameMask.ENEMY);
        hitboxes.onStrike = (e, strike) -> Weapons.wear(e);

        return world.createEntity()
                .add(new Name("spineboy"))
                .add(new Position(x, y))
                .add(new Timers())
                .add(new Health(5))
                .add(new Contacts())
                .add(new Input(GameControls.PLAYER))
                .add(GameControls.playerButtons())
                .add(new Held(GameWeapons.BLASTER))
                .add(new SpineAnimator(GameSpine.SPINEBOY, "idle"))
                .add(new Behavior(BrawlerState.NORMAL))
                .add(new Mover(new BrawlerMotion(BRAWLER_PLAYER_TUNING)))
                .add(Collider.capsule(GameMask.PLAYER, new Rectangle(-18, 0, 36, 12), CollisionCaps.Axis.HORIZONTAL) // only covers feet: sprite's top half may overlap a wall behind it
                        .blockedBy(GameMask.SOLID)
                        .detects(GameMask.ENEMY, GameMask.TRIGGER, GameMask.HAZARD, GameMask.PICKUP))
                .add(hitboxes) // the body that gets hit, the shot that hits
                .add(new Hurtable()
                        .hurtState(BrawlerState.HURT)
                        .deadState(BrawlerState.DEAD)
                        .deathTint(Color.WHITE));
    }

    /** TEMP: Something to hit in the scroller: a spineboy that stands where it is put, facing {@code facing}, and shoots every two seconds. */
    public Entity dummy(int x, int y, int facing) {
        var timers = new Timers();
        var anim = new SpineAnimator(GameSpine.SPINEBOY, "idle");
        anim.defaultScale.set(0.5f, 0.5f); // the scroller's spineboy size, as ExampleScroller.spawnPlayer sets it
        anim.scale.set(0.5f, 0.5f);
        anim.facing = facing;
        var noneKey = com.badlogic.gdx.Input.Keys.UNKNOWN;
        var noneControls = new InputControls(noneKey, noneKey, noneKey, noneKey, noneKey);

        var entity = world.createEntity();
        entity  .add(new Name("dummy"))
                .add(new Position(x, y))
                .add(new Health(5))
                .add(new Input(noneControls))
                .add(GameControls.none())
                .add(new Held(GameWeapons.BLASTER))
                .add(new Behavior(BrawlerState.NORMAL))
                .add(new Mover(new BrawlerMotion(BRAWLER_PLAYER_TUNING)))
                .add(new Hitboxes(GameHitboxes.SPINEBOY, GameMask.ENEMY, GameMask.PLAYER))
                .add(new Hurtable().despawnAfter(1.5f).onDeath((e, strike) -> Timers.get(e).cancel("attack")))
                .add(timers)
                .add(anim)
                .add(new FloatingHealthBar(entity, 90f))
        ;

        timers.every("attack", 2f, attack -> Behavior.get(attack.entity()).request(BrawlerState.ATTACK));

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

     /** A screen on the back wall showing the feed; it sorts by its bottom edge like everything on the street. */
     public Entity television(TelevisionFeed feed, int x, int y, int w, int h) {
         var television = new Television(feed, w, h);
         television.order = -y;
        return world.createEntity()
                .add(new Name("television"))
                .add(new Position(x, y))
                .add(television);
     }
}
