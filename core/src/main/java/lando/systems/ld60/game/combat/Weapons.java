package lando.systems.ld60.game.combat;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.zendogames.zenlib.Log;
import com.zendogames.zenlib.ecs.Entity;
import com.zendogames.zenlib.ecs.World;
import com.zendogames.zenlib.ecs.components.*;
import com.zendogames.zenlib.sim.SimulationStep;
import lando.systems.ld60.Main;
import lando.systems.ld60.assets.GameAtlas;
import lando.systems.ld60.game.Factory;
import lando.systems.ld60.game.GameMask;
import lando.systems.ld60.game.GameTags;
import lando.systems.ld60.game.anim.Move;
import lando.systems.ld60.game.components.Held;
import lando.systems.ld60.game.components.Hitboxes;
import lando.systems.ld60.game.components.Pickup;
import lando.systems.ld60.game.movement.BrawlerMotion;
import lando.systems.ld60.game.movement.BrawlerTuning;

/**
 * Picking up, dropping, throwing and wearing out weapons: the moves that change a fighter's
 * {@link Held} and keep its {@code Hitboxes.set} in step. States and AI call these; nothing here
 * decides when. The two spawns ({@link #spawnPickup}, {@link #spawnThrown}) take the {@code World}
 * because a state has no {@code Factory}; the factory's own {@code pickup(...)} delegates here.
 */
public final class Weapons {

    private static final String TAG = Weapons.class.getSimpleName();

    /** The flying item's box, from its anchor: a hand-sized square, thick enough to clip a fighter a few pixels up or down the street. */
    private static final float ITEM = 12, ITEM_DEPTH = 8;

    private Weapons() {}

    /** The basic attack of whatever the fighter holds; null for a fighter without {@link Held}. */
    public static Move attackOf(Entity e) {
        var held = Held.get(e);
        return (held == null) ? null : held.weapon.attack();
    }

    /** The pickup entity the fighter stands over (its contacts this tick), null when none. */
    public static Entity under(Entity fighter) {
        var contacts = Contacts.get(fighter);
        if (contacts == null) return null;
        for (int i = 0; i < contacts.curr.size; i++) {
            var other = contacts.curr.get(i).entity;
            if (other.isAlive() && Pickup.has(other)) return other;
        }
        return null;
    }

    /** Takes {@code item} (despawned) into the fighter's hands, dropping what it held if that was a weapon. */
    public static boolean pickUp(Entity fighter, Entity item) {
        var held = Held.get(fighter);
        var pickup = Pickup.get(item);
        if (held == null || pickup == null || !item.isAlive() || item.isRemoving()) return false;
        if (held.armed()) drop(fighter);
        arm(fighter, held, pickup.weapon());
        item.world().despawn(item);
        Log.info(TAG, "%s picked up %s", Name.of(fighter), pickup.weapon().name());
        return true;
    }

    /** Puts the held weapon on the floor at the fighter's feet and goes back to bare hands. */
    public static void drop(Entity fighter) {
        var held = Held.get(fighter);
        if (held == null || !held.armed()) return;
        var pos = Position.get(fighter);
        spawnPickup(fighter.world(), held.weapon, pos.x, pos.y);
        arm(fighter, held, held.unarmed);
    }

    /** Throws the held weapon the way the fighter faces; it flies as a HIT box on the fighter's side, then lands as a pickup. */
    public static Entity throwHeld(Entity fighter) {
        var held = Held.get(fighter);
        if (held == null || !held.canThrow()) return null;
        var weapon = held.weapon;
        arm(fighter, held, held.unarmed);
        return spawnThrown(fighter.world(), fighter, weapon);
    }

    /** Counts a landed hit against the held weapon's durability; a worn-out weapon breaks (no pickup). Wire it as the fighter's {@code Hitboxes.onStrike}. */
    public static void wear(Entity fighter) {
        var held = Held.get(fighter);
        if (held == null || !held.armed() || !held.weapon.breaks()) return;
        if (++held.hits < held.weapon.durability()) return;
        Log.info(TAG, "%s's %s broke", Name.of(fighter), held.weapon.name());
        arm(fighter, held, held.unarmed);
    }

    /** Puts {@code weapon} straight into the fighter's hands (a spawn-time loadout, a boss phase); what it held is lost. */
    public static void arm(Entity fighter, Weapon weapon) {
        var held = Held.get(fighter);
        if (held != null) arm(fighter, held, weapon);
    }

    private static void arm(Entity fighter, Held held, Weapon weapon) {
        held.weapon = weapon;
        held.hits = 0;
        var boxes = Hitboxes.get(fighter);
        if (boxes != null) {
            boxes.set = weapon.set();
        }
        // a rig with a 'weapon' slot shows the held item: the attachment named after the weapon, nothing when unarmed or unknown
        var spine = SpineAnimator.get(fighter);
        if (spine != null && spine.skeleton.findSlot("weapon") != null) {
            var shown = held.armed() && spine.skeleton.getAttachment("weapon", weapon.name()) != null;
            spine.skeleton.setAttachment("weapon", shown ? weapon.name() : null);
        }
    }

    // ------------------------------------------------------------------------
    // Spawns
    // ------------------------------------------------------------------------

    /** A weapon on the floor at {@code (x, y)}: a PICKUP zone a tile wide that fighters detect. */
    public static Entity spawnPickup(World world, Weapon weapon, int x, int y) {
        var e = world.createEntity()
                .add(new Name("pickup:" + weapon.name()))
                .add(new Position(x, y))
                .add(new Pickup(weapon))
                .add(Collider.rect(GameMask.PICKUP, -Factory.UNIT / 2f, -4, Factory.UNIT, 8));
        var image = image(weapon, Color.GOLD);
        if (image != null) e.add(image);
        return e;
    }

    /**
     * {@code weapon} in flight from {@code thrower}'s hands: a brawler mover launched the way the
     * thrower faces, carrying one HIT box on the thrower's team. It lands as a pickup where it
     * stops: on the first fighter it hits, against a wall, or when its arc is over.
     */
    public static Entity spawnThrown(World world, Entity thrower, Weapon weapon) {
        var pos    = Position.get(thrower);
        var boxes  = Hitboxes.get(thrower);
        var facing = (Mover.has(thrower) && Mover.get(thrower).motion instanceof BrawlerMotion m) ? m.facing : boxes.facing;
        var tuning = new BrawlerTuning(
                Factory.UNIT,    // authoring unit -> pixels
                5,       // x-axis speed (tiles/sec)
                2,       // y-axis speed (tiles/sec)
                0.1f,    // accel time
                0.08f,   // stop time
                2.5f,    // 'jump' height (tiles)
                0.35f);  // 'jump' time to peak (seconds)
        var motion = new BrawlerMotion(tuning);
        var mover  = new Mover(motion);
        var timers = new Timers();
        var set    = new HitboxSet().base(Hitbox.hit("item", Hitbox.rect(-ITEM / 2, -ITEM / 2, ITEM, ITEM), weapon.thrown()).depth(ITEM_DEPTH));
        var hits   = new Hitboxes(set, boxes.team, teams(boxes.targets));
        hits.facing   = facing;
        hits.onStrike = (e, strike) -> land(e, weapon);
        mover.onHitX  = (e, mov, dir) -> land(e, weapon);
        motion.facing = facing;
        motion.launch(mover, facing, weapon.throwDist(), weapon.throwHeight());
        motion.z = 32; // leaves the hand, not the feet

        var e = world.createEntity()
                .tag(GameTags.PROJECTILE)
                .add(new Name("thrown:" + weapon.name()))
                .add(new Position(pos.x + facing * 12, pos.y))
                .add(Collider.circ(GameMask.PICKUP, 0, 0, 4).blockedBy(GameMask.SOLID))
                .add(mover)
                .add(hits)
                .add(timers);
        var image = image(weapon, Color.WHITE);
        if (image != null) e.add(image);

        // The arc is over after hangTime; a tick late is fine, so check and come back next tick until it is down
        timers.after("land", motion.tuning.hangTime(weapon.throwHeight()), land -> {
            if (!motion.grounded) { land.restart(SimulationStep.FIXED_DT); return; }
            land(land.entity(), weapon);
        });
        return e;
    }

    /** The thrown item comes to rest: a pickup where it is, the projectile gone. Idempotent within the tick. */
    private static void land(Entity thrown, Weapon weapon) {
        if (!thrown.isAlive() || thrown.isRemoving()) return;
        var pos = Position.get(thrown);
        spawnPickup(thrown.world(), weapon, pos.x, pos.y);
        thrown.world().despawn(thrown);
    }

    /** The weapon's picture, or a tinted pixel block when it has none; null headless. */
    private static Image image(Weapon weapon, Color tint) {
        var region = weapon.region();
        if (region == null) {
            // try to find the region in the sprite atlas if not already set on the weapon
            var atlas = GameAtlas.SPRITES.get();
            region = atlas.findRegion("weapons/" + weapon.name());
        }
        if (region != null) {
            var image = new Image(region);
            image.origin.set(region.getRegionWidth() / 2f, 0);
            return image;
        }
        if (Main.game == null || Main.game.pixel == null) return null;
        var image = new Image(Main.game.pixel, 8, 8);
        image.origin.set(4, 0);
        image.tint.set(tint);
        return image;
    }

    /** The masks in a {@code Hitboxes.targets} bit set, as the constructor wants them. */
    private static GameMask[] teams(int bits) {
        var out = new Array<GameMask>(GameMask.values().length);
        for (var mask : GameMask.values()) {
            if (mask.in(bits)) out.add(mask);
        }
        return out.toArray(GameMask.class);
    }
}
