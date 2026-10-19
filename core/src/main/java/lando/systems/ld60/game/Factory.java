package lando.systems.ld60.game;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.zendogames.zenlib.collision.shapes.CollisionCaps;
import com.zendogames.zenlib.ecs.Entity;
import com.zendogames.zenlib.ecs.World;
import com.zendogames.zenlib.ecs.components.*;
import com.zendogames.zenlib.render.RenderLayer;
import com.zendogames.zenlib.tilemap.TiledProps;
import lando.systems.ld60.assets.EmitterType;
import lando.systems.ld60.game.ai.Senses;
import lando.systems.ld60.game.characters.*;
import lando.systems.ld60.game.combat.Weapon;
import lando.systems.ld60.game.combat.Weapons;
import lando.systems.ld60.game.components.*;
import lando.systems.ld60.game.movement.BrawlerMotion;
import lando.systems.ld60.particles.ParticleEffectParams;

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

    private final World world;

    /** What the enemies' brains look at: the live players, every fighter. */
    private final Senses senses;

    public Factory(World world) {
        this.world = world;
        this.senses = new Senses(world);
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

    /** The scroller's rank and file: {@code kind} picks the character ({@link Kinds}), {@code boss} its role, {@code weapon} (optional) arms it. */
    public Entity enemy(MapObject object) {
        int x      = TiledProps.x(object);
        int y      = TiledProps.y(object);
        int w      = TiledProps.width(object);
        var kind   = Kinds.byName(TiledProps.get(object, "kind", "spineboy", String.class));
        var boss   = TiledProps.get(object, "boss", false, Boolean.class);
        var weapon = TiledProps.get(object, "weapon", "", String.class);
        var role   = boss ? Roles.boss(Roles.THUG.mind()) : Roles.THUG; // TODO: the boss's own state machine via Minds.states(...)
        var enemy  = fighter(kind, role, x + w / 2, y, -1);
        if (!weapon.isEmpty()) {
            Weapons.arm(enemy, GameWeapons.byName(weapon));
        }
        return enemy;
    }

    /**
     * Any character in any role: the components every brawler has, from the {@link Kind} (rig,
     * clips, tuning, feet, hands) and the {@link Role} (side, masks, hurt policy, mind), in that
     * order so the mind finds everything on the entity. {@link #player}, {@link #thug} and the
     * Tiled {@link #enemy} are this with the pair filled in.
     */
    public Entity fighter(Kind kind, Role role, int x, int y, int facing) {
        var tuning = (role.tuning() != null) ? role.tuning() : kind.tuning();
        var motion = new BrawlerMotion(tuning);
        motion.facing = facing;
        var boxes = new Hitboxes(kind.wield(kind.unarmed()).set(), role.team(), role.targets());
        boxes.onStrike = (e, strike) -> Weapons.wear(e);
        var e = world.createEntity()
                .tag(role.tag())
                .add(new Fighter(kind, role))
                .add(new Name(kind.name()))
                .add(new Position(x, y))
                .add(new Timers())
                .add(new Health(kind.health()))
                .add(new Contacts())
                .add(kind.animator().apply(facing))
                .add(kind.clips().get())
                .add(new Held(kind.unarmed()))
                .add(role.hurtable().get())
                .add(new Mover(motion))
                .add(Collider.capsule(role.body(), kind.feet(), CollisionCaps.Axis.HORIZONTAL)
                        .blockedBy(GameMask.SOLID)
                        .detects(role.detects()))
                .add(boxes);
        if (role.healthBar()) {
            e.add(new FloatingHealthBar(/*yOffset*/ 90f));
        }
        role.mind().attach(e, kind, senses);
        if (kind.dress() != null) {
            kind.dress().accept(this, e);
        }
        return e;
    }

    /** The player: spineboy on the keyboard (or pad). */
    public Entity player(int x, int y) {
        return fighter(Awol.KIND, Roles.PLAYER, x, y, 1);
    }

    /** A spineboy thug: the rank and file, a {@code ThugMind} for a brain. */
    public Entity thug(int x, int y, int facing) {
        return fighter(Spineboy.KIND, Roles.THUG, x, y, facing);
    }

    public Entity n64Boss(int x, int y, int facing) {
        return fighter(Awol.KIND, Roles.boss(Roles.THUG.mind()), x, y, facing); // TODO: its own states: Roles.boss(Minds.states(N64BossState.ENTER))
    }

    public Entity awol(int x, int y, int facing) {
        return fighter(Awol.KIND, Roles.THUG, x, y, facing);
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

    /** A weapon lying on the floor at {@code (x, y)}. */
    public Entity pickup(Weapon weapon, int x, int y) {
        return Weapons.spawnPickup(world, weapon, x, y);
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
