package lando.systems.ld60.game;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.zendogames.zenlib.Log;
import com.zendogames.zenlib.collision.shapes.CollisionCaps;
import com.zendogames.zenlib.ecs.Entity;
import com.zendogames.zenlib.ecs.Family;
import com.zendogames.zenlib.ecs.World;
import com.zendogames.zenlib.ecs.components.*;
import com.zendogames.zenlib.render.RenderLayer;
import com.zendogames.zenlib.tilemap.TiledProps;
import lando.systems.ld60.assets.EmitterType;
import lando.systems.ld60.assets.GameSpine;
import lando.systems.ld60.assets.SoundType;
import lando.systems.ld60.events.AudioEvent;
import lando.systems.ld60.game.anim.AnimClip;
import lando.systems.ld60.game.characters.Awol;
import lando.systems.ld60.game.combat.Weapon;
import lando.systems.ld60.game.combat.Weapons;
import lando.systems.ld60.game.components.*;
import lando.systems.ld60.game.input.GameControls;
import lando.systems.ld60.game.movement.BrawlerMotion;
import lando.systems.ld60.game.movement.BrawlerTuning;
import lando.systems.ld60.game.state.BrawlerState;
import lando.systems.ld60.particles.ParticleEffectParams;
import lando.systems.ld60.particles.effects.FootStepDustCloudEffect;

import java.util.List;

public class Factory {

    /** Tile size: the unit every tuning is authored in. */
    public static final int UNIT = 16;

    // Tiled class names: spawner keys, one per factory method below
    public static final String PLAYER       = "player";
    public static final String WALL         = "wall";
    public static final String ENEMY        = "enemy";
    public static final String PICKUP       = "pickup";
    public static final String TELEVISION   = "television"; // scroller: a region where the game world framebuffer is rendered back into for a "watch yourself play" effect
    public static final String STAGE_MARKER = "stage";

    public static final List<String> ENEMY_KINDS = List.of(
            "thug",
            "awol", // TEMP: testing
            "n64Boss"
    );

    /** Every live player, for the enemies' AI to look for. */
    private final Family players;
    private final World world;

    public Factory(World world) {
        this.world = world;
        this.players = world.family(Family.all(GameTags.PLAYER, Position.TYPE));
    }

    // ------------------------------------------------------------------------
    // From map objects: read the Tiled class members here, then build the plain way
    // ------------------------------------------------------------------------

    public Entity player(MapObject object) {
        int x = TiledProps.x(object);
        int y = TiledProps.y(object);
        int w = TiledProps.width(object);
        return player(x + w / 2, y);
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
            case "n64Boss" -> n64Boss(x + w / 2, y, -1);
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

    /** The player: a brawler on the keyboard (or pad) with the states that read its buttons; hits ask for HURT and DEAD through its Hurtable. */
    public Entity player(int x, int y) {
        var hurtable = new Hurtable()
                .hurtState(BrawlerState.HURT)
                .deadState(BrawlerState.DEAD)
                .deathTint(null); // the death clip is the look
        var boxes = new Hitboxes(GameWeapons.BLASTER.set(), GameMask.PLAYER, GameMask.ENEMY);
        boxes.onStrike = (e, strike) -> Weapons.wear(e);

        var anim = new SpineAnimator(GameSpine.SPINEBOY, "idle");
        anim.defaultScale.set(0.5f, 0.5f);
        anim.scale.set(0.5f, 0.5f);
        anim.facing = 1;

        var tuning = new BrawlerTuning(
                UNIT,    // authoring unit -> pixels
                12,      // x-axis speed (tiles/sec)
                6,       // y-axis speed (tiles/sec)
                0.1f,    // accel time
                0.08f,   // stop time
                2.5f,    // 'jump' height (tiles)
                0.35f);  // 'jump' time to peak (seconds)
        var clips = new BrawlerAnimClips(
                AnimClip.spine("idle"),
                AnimClip.spine("walk"),
                AnimClip.spine("run"),
                AnimClip.spine("jump"),
                tuning.maxSpeedX() * 0.5f)
                .stop(AnimClip.spine("run-to-idle"))
                .dead(AnimClip.spine("death"));

        return world.createEntity()
                .tag(GameTags.PLAYER)
                .add(new Name("hero"))
                .add(new Position(x, y))
                .add(new Timers())
                .add(new Health(10))
                .add(new Contacts())
                .add(new Input(GameControls.PLAYER))
                .add(GameControls.playerButtons())
                .add(new Held(GameWeapons.BLASTER))
                .add(anim)
                .add(clips)
                .add(hurtable)
                .add(new Behavior(BrawlerState.NORMAL))
                .add(new Mover(new BrawlerMotion(tuning)))
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
        var tuning = new BrawlerTuning(
                UNIT,    // authoring unit -> pixels
                5,       // x-axis speed (tiles/sec)
                2,       // y-axis speed (tiles/sec)
                0.1f,    // accel time
                0.08f,   // stop time
                2.5f,    // 'jump' height (tiles)
                0.35f);  // 'jump' time to peak (seconds)
        var motion = new BrawlerMotion(tuning);
        motion.facing = facing;
        var hurtable = new Hurtable()
                .despawnAfter(1.5f)
                .onDeath((e, strike) -> { timers.cancel("think"); Input.get(e).reset(); });
        var anim = new SpineAnimator(GameSpine.SPINEBOY, "idle");
        anim.defaultScale.set(0.5f, 0.5f);
        anim.scale.set(0.5f, 0.5f);
        anim.facing = 1;
        var clips = new BrawlerAnimClips(
                AnimClip.spine("idle"),
                AnimClip.spine("walk"),
                AnimClip.spine("run"),
                AnimClip.spine("jump"),
                tuning.maxSpeedX() * 0.5f)
                .stop(AnimClip.spine("run-to-idle"))
                .dead(AnimClip.spine("death"));
        var thug = world.createEntity()
                .tag(GameTags.ENEMY)
                .add(new Name("thug"))
                .add(new Position(x, y))
                .add(new Health(5))
                .add(new FloatingHealthBar(/*yOffset*/ 90f))
                .add(new Contacts())
                .add(new Input())
                .add(new Held(GameWeapons.BLASTER))
                .add(anim)
                .add(clips)
                .add(hurtable)
                .add(new Mover(motion))
                .add(Collider.capsule(GameMask.ENEMY, new Rectangle(-18, 0, 36, 12), CollisionCaps.Axis.HORIZONTAL)
                        .blockedBy(GameMask.SOLID)
                        .detects(GameMask.PICKUP))
                .add(new Hitboxes(GameWeapons.BLASTER.set(), GameMask.ENEMY, GameMask.PLAYER))
                .add(timers);

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

    public Entity n64Boss(int x, int y, int facing) {
        var timers = new Timers();
        var tuning = new BrawlerTuning(
                UNIT,    // authoring unit -> pixels
                5,       // x-axis speed (tiles/sec)
                2,       // y-axis speed (tiles/sec)
                0.1f,    // accel time
                0.08f,   // stop time
                2.5f,    // 'jump' height (tiles)
                0.35f);  // 'jump' time to peak (seconds)
        var motion = new BrawlerMotion(tuning);
        motion.facing = facing;
        var hurtable = new Hurtable()
            .despawnAfter(1.5f)
            .onHurt((e, strike) -> { if (motion.grounded) AnimClip.spine("hurt").start(e, false); }) // shoved along the floor: flinch; the present system leaves a clip it doesn't own alone until it completes
            .onDeath((e, strike) -> { timers.cancel("think"); Input.get(e).reset(); });

        var anim = new SpineAnimator(GameSpine.AWOL, "idle");
        anim.defaultScale.set(0.35f, 0.35f);
        anim.scale.set(0.35f, 0.35f);
        anim.facing = facing;

        var clips = new BrawlerAnimClips(
                AnimClip.spine("idle"),
                AnimClip.spine("walk"),
                AnimClip.spine("run"),
                AnimClip.spine("jump"),
                tuning.maxSpeedX())
                .hurt(AnimClip.spine("knockback"))
                .dead(AnimClip.spine("death"));

        var entity = world.createEntity()
            .tag(GameTags.BOSS)
            .add(new Name("awol"))
            .add(new Position(x, y))
            .add(new Health(3))
            .add(new FloatingHealthBar(/*yOffset*/ 90f))
            .add(new Contacts())
            .add(new Input())
            .add(new Held(GameWeapons.AWOL_FIST))
            .add(new Mover(motion))
            .add(Collider.capsule(GameMask.ENEMY, new Rectangle(-18, 0, 36, 12), CollisionCaps.Axis.HORIZONTAL)
                .blockedBy(GameMask.SOLID)
                .detects(GameMask.PICKUP))
            .add(new Hitboxes(GameWeapons.AWOL_FIST.set(), GameMask.ENEMY, GameMask.PLAYER))
            .add(anim)
            .add(clips)
            .add(hurtable)
            .add(timers);

        // The rig's events: "footstep" goes to a dust emitter; the rest are logged and play stand-in sounds until there are real ones
        emitter(EmitterType.DUST, new FootStepDustCloudEffect.Params(entity));
        anim.onEvent(event -> {
            var name = event.getData().getName();
            Log.info("n64boss", "event '%s' at %.2fs of '%s'", name, event.getTime(), AnimClip.current(entity));
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

    public Entity awol(int x, int y, int facing) {
        var timers   = new Timers();
        var motion   = Awol.motion(facing);
        var animator = Awol.animator(facing);

        var entity = world.createEntity()
                .tag(GameTags.ENEMY)
                .add(new Name("awol"))
                .add(new Position(x, y))
                .add(new Health(Awol.Props.maxHealth))
                .add(new Input())
                .add(new Contacts())
                .add(new Held(Awol.Props.unarmedWeapon))
                .add(new FloatingHealthBar(/*yOffset*/ 90f))
                .add(new Mover(motion))
                .add(Awol.collider())
                .add(Awol.animClips())
                .add(Awol.hurtable(motion, timers))
                .add(Awol.hitboxes())
                .add(animator)
                .add(timers);

        // The rig's events: "footstep" goes to a dust emitter; the rest are logged and play stand-in sounds until there are real ones
        emitter(EmitterType.DUST, new FootStepDustCloudEffect.Params(entity));

        // Available events in Awol spine anim: [footstep, hurt, jump, swing, throw, thud]
        animator.onEvent(event -> {
            var name = event.getData().getName();
            Log.info("awol", "event '%s' at %.2fs of '%s'", name, event.getTime(), AnimClip.current(entity));
            switch (name) {
                case "footstep" -> AudioEvent.playSound(SoundType.BELL_LOW);  // TODO: change me
                case "hurt"     -> AudioEvent.playSound(SoundType.BELL_HIGH); // TODO: change me (or remove, see default)
                case "jump"     -> AudioEvent.playSound(SoundType.BELL_HIGH); // TODO: change me (or remove, see default)
                case "swing"    -> AudioEvent.playSound(SoundType.BELL_HIGH); // TODO: change me (swing a weapon)
                case "throw"    -> AudioEvent.playSound(SoundType.BELL_HIGH); // TODO: change me (throw a weapon)
                case "thud"     -> AudioEvent.playSound(SoundType.BELL_LOW);  // TODO: change me (hit ground on death)
                default -> {} // "jump", "hurt": a landed hit is better heard from CombatEvent, which knows whether a glove connected
            }
        });

        timers.every(Awol.Props.thinkTimer, 0.25f, think -> {
            var e      = think.entity();
            var input  = Input.get(e);
            var target = nearestPlayer(e);
            if (target == null) {
                input.moveX = 0;
                input.moveY = 0;
                return;
            }

            var item = Weapons.under(e);
            if (item != null) {
                Weapons.pickUp(e, item);
            }

            var pos  = Position.get(e);
            var tPos = Position.get(target);
            int dx = tPos.x - pos.x;
            int dy = tPos.y - pos.y;
            int reach = 3 * UNIT;
            input.moveX = (Math.abs(dx) > reach) ? Integer.signum(dx) : 0;
            input.moveY = (Math.abs(dy) > 6)     ? Integer.signum(dy) : 0;
            if (input.moveX == 0 && input.moveY == 0 && Math.abs(dx) <= reach + UNIT / 2) {
                if (dx != 0) motion.facing = Integer.signum(dx);
                var held = Held.get(e);
                var attack = (held.armed() || MathUtils.randomBoolean()) ? Weapons.attackOf(e) : Awol.Props.kickWeapon;
                if (attack != null && !attack.playing(e) && !timers.has("cooldown") && motion.grounded && !motion.launched) {
                    attack.start(e);

                    // wait a bit before attacking again
                    timers.after("cooldown", 1.2f, cooldown -> {});
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
