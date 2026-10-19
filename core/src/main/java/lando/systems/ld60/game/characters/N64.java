package lando.systems.ld60.game.characters;

import com.badlogic.gdx.math.Rectangle;
import com.zendogames.zenlib.ecs.Entity;
import com.zendogames.zenlib.ecs.components.*;
import com.zendogames.zenlib.render.RenderLayer;
import lando.systems.ld60.assets.AnimN64;
import lando.systems.ld60.assets.EmitterType;
import lando.systems.ld60.assets.SoundType;
import lando.systems.ld60.events.AudioEvent;
import lando.systems.ld60.events.ParticleEvent;
import lando.systems.ld60.game.*;
import lando.systems.ld60.game.ai.Minds;
import lando.systems.ld60.game.ai.Senses;
import lando.systems.ld60.game.anim.AnimClip;
import lando.systems.ld60.game.anim.Move;
import lando.systems.ld60.game.combat.Hitbox;
import lando.systems.ld60.game.combat.HitboxSet;
import lando.systems.ld60.game.combat.Strike;
import lando.systems.ld60.game.components.*;
import lando.systems.ld60.game.movement.BrawlerMotion;
import lando.systems.ld60.game.movement.BrawlerTuning;
import lando.systems.ld60.game.state.N64BossState;
import lando.systems.ld60.game.state.RumblePakState;
import lando.systems.ld60.particles.effects.FireExplosionEffect;
import lando.systems.ld60.particles.effects.SpawnCloudEffect;

import java.util.Map;
import java.util.Set;

/**
 * The N64 controller boss ({@link AnimN64}, a sprite fighter): everything about the character
 * and nothing about who it is for, like {@link Awol}. {@link Props} is the authored data,
 * {@link #KIND} is what the factory spawns, {@link #ROLE} is its boss role (its own state machine,
 * {@code N64BossState}). The rumble pak it fires is a projectile entity of its own
 * ({@link #shoot}) that {@link #explode}s where the player stood.
 * <p>
 * The art is 400x534 frames with the controller's joystick (its punch arm) pointing right and the
 * floor shadow baked in at the bottom; {@code Props.originX} is the controller's center column and
 * the origin's y is the frame's bottom, the floor line. Pixels in the hit boxes are at
 * {@link Props#scale}.
 */
public final class N64 {

    private N64() {}

    public static class Props {
        // Misc ---------------------------------------------------------------
        public static final int   maxHealth = 30;
        public static final float scale     = 0.5f;
        /** The controller's center column in the frame; the origin's y is the frame bottom (the baked shadow's floor line). */
        public static final float originX   = 160;
        // Movement tuning: slow and floaty -----------------------------------
        public static final int   unit           = Factory.UNIT;
        public static final int   speedX         = 3;    // tiles/sec
        public static final int   speedY         = 1;    // tiles/sec
        public static final float accelTime      = 0.5f; // sec
        public static final float stopTime       = 0.4f; // sec
        public static final float jumpHeight     = 1.0f; // tiles (never jumps; sets the fall when it dies)
        public static final float jumpTimeToPeak = 0.3f; // sec
        /** The float: height range (px) and how long each drift takes (s). */
        public static final float hoverMin = 4, hoverMax = 16, hoverPeriodMin = 1.4f, hoverPeriodMax = 3.0f;
        // Collision ----------------------------------------------------------
        public static final Rectangle feet = new Rectangle(-40, 0, 80, 12);
        // The brain's numbers (px, s) -----------------------------------------
        /** Jab reach: joystick tip plus a body; stands {@code standoff} away, within {@code settle}; street depth it attacks within. */
        public static final float reach = 110, standoff = 85, settle = 12;
        public static final int   depth = 10;
        public static final float punchRestMin = 0.8f, punchRestMax = 1.4f;
        public static final float specialRestMin = 4f, specialRestMax = 7f;
        public static final float armedHoldMin = 0.6f, armedHoldMax = 1.8f;
        /** Seconds into the shoot clip the pak leaves (the muzzle flash frame). */
        public static final float launchTime = 0.1f;
        /** Sideways shudder while armed, in frame pixels at full intensity. */
        public static final float shakeMax = 14;
        /** Hit box names that knock it out of its spin-up; a plain hit staggers it every so many hits while idle. */
        public static final Set<String> heavyHits = Set.of("power", "swing");
        public static final int staggerEvery = 3;
        // The rumble pak -----------------------------------------------------
        /** Flight speed along the floor. */
        public static final float pakSpeed = 240;
        /** Where the fall frames put the pack down, ahead of the frame origin (drawn px): the entity stops this short of the target. */
        public static final int pakLandX = 80;
        /** Blast reach along the street and up it (an ellipse on the floor). */
        public static final float blastRadius = 90, blastDepth = 30;
        // Attacks / Hit, Hurt boxes (drawn pixels at `scale`; windows in seconds into the clip) --
        public static final Hitbox.Attack attackPunch = Hitbox.attack(/*damage*/ 2, /*knockback*/ 5, /*height*/ 2.5f);
        public static final Hitbox.Attack attackBlast = Hitbox.attack(/*damage*/ 3, /*knockback*/ 6, /*height*/ 3);
        /** The blast on the boss's own side: thrown clear, no damage. */
        public static final Hitbox.Attack attackShove = Hitbox.attack(/*damage*/ 0, /*knockback*/ 4, /*height*/ 2);
        /** The controller body, floating {@code hoverMin..hoverMax} px up on top of the art's own 21 px; thick, it is a big target. */
        public static final Hitbox hurtboxBody  = Hitbox.hurt("body", Hitbox.rect(-66, 21, 132, 137)).depth(10);
        /** The joystick out to its tip (stick at 75..95 px), reaching back into the body and down to a player's chest: the boss is twice its height. */
        public static final Hitbox hitboxPunch  = Hitbox.hit("punch", Hitbox.rect(50, 30, 60, 62), attackPunch);
        public static final Hitbox hitboxBlast  = Hitbox.hit("blast", Hitbox.circ(0, 0, blastRadius), attackBlast).depth(blastDepth);
        public static final Hitbox hitboxShove  = Hitbox.hit("blast", Hitbox.circ(0, 0, blastRadius), attackShove).depth(blastDepth);
        /** The jab: out on the two extended frames (4 and 5 of 8 at 10 fps). */
        public static final HitboxSet set = new HitboxSet().base(hurtboxBody)
                .window(AnimN64.PUNCH.name(), /*start*/ 0.4f, /*end*/ 0.6f, hitboxPunch);
        /** No boxes at all: the pak in flight hits nothing and is hit by nothing; the blast is {@link #explode}'s. */
        public static final HitboxSet pakSet = new HitboxSet();
        // The states' moves ---------------------------------------------------
        public static final Move punchMove = Move.of(AnimClip.sprite(AnimN64.PUNCH));
        public static final Move spinMove  = Move.of(AnimClip.sprite(AnimN64.SPIN));
        public static final Move armedMove = Move.of(AnimClip.sprite(AnimN64.ARMED));
        public static final Move shootMove = Move.of(AnimClip.sprite(AnimN64.SHOOT));
        public static final Move hurtMove  = Move.of(AnimClip.sprite(AnimN64.HURT));
    }

    public static final BrawlerTuning tuning = new BrawlerTuning(
            Props.unit,
            Props.speedX,
            Props.speedY,
            Props.accelTime,
            Props.stopTime,
            Props.jumpHeight,
            Props.jumpTimeToPeak
    );

    static Animator animator(int facing) {
        return animator(AnimN64.IDLE, facing);
    }

    /** A frame of the controller's art at the boss's size, floor line at the frame bottom. */
    static Animator animator(AnimN64 clip, int facing) {
        var anim = new Animator(clip, Props.originX, 0);
        anim.defaultScale.set(Props.scale, Props.scale);
        anim.scale.set(Props.scale, Props.scale);
        anim.facing = facing;
        return anim;
    }

    /** It floats: idle is its only locomotion; the hurt clip stands in for a knockback and, held on its last frame, for death. */
    static BrawlerAnimClips animClips() {
        var idle = AnimClip.sprite(AnimN64.IDLE);
        return new BrawlerAnimClips(idle, idle, idle, idle, Float.MAX_VALUE)
                .hurt(AnimClip.sprite(AnimN64.HURT))
                .dead(AnimClip.sprite(AnimN64.HURT));
    }

    /** After spawn: the float. */
    static void dress(Factory factory, Entity e) {
        e.add(new Hover(Props.hoverMin, Props.hoverMax, Props.hoverPeriodMin, Props.hoverPeriodMax));
    }

    public static final Kind KIND = new Kind("n64",
            tuning,
            Props.maxHealth,
            Props.feet,
            N64::animator,
            N64::animClips,
            GameWeapons.FISTS,
            Map.of(GameWeapons.FISTS.name(), Wield.of(Props.set, Props.punchMove)),
            N64::dress);

    /** Its boss role: its own states, super armor, the hurt and death hooks into them. */
    public static final Role ROLE = Roles.boss(Minds.states(N64BossState.IDLE),
            () -> new Hurtable().knockback(0).recoverTime(0.5f).despawnAfter(2.5f)
                    .deadState(N64BossState.DEAD)
                    .onHurt(N64BossState::onHurt));

    // ------------------------------------------------------------------------
    // The rumble pak
    // ------------------------------------------------------------------------

    /**
     * Fires the pak from {@code boss} at where the nearest player stands right now (a tile ahead
     * when there is none): a projectile entity drawn from the boss's position with the boss's origin
     * and scale, so its first frame is the pack where the shoot clip showed it. {@code RumblePakState}
     * takes it from there.
     */
    public static Entity shoot(Entity boss) {
        var pos    = Position.get(boss);
        var facing = Mover.get(boss).motion(BrawlerMotion.class).facing;
        var player = Senses.nearestPlayerTo(boss);
        int targetX = (player != null) ? Position.get(player).x : pos.x + facing * 10 * Factory.UNIT;
        int targetY = (player != null) ? Position.get(player).y : pos.y;

        // the fall frames carry the pack pakLandX ahead of the frame: stop that short of the target, and never behind where it started
        int landX = targetX - facing * Props.pakLandX;
        if ((landX - pos.x) * facing < 0) landX = pos.x;

        var anim = animator(AnimN64.PROJECTILE, facing);
        anim.order = -pos.y;
        var boxes = new Hitboxes(Props.pakSet, GameMask.ENEMY, GameMask.PLAYER);
        boxes.vulnerable = false; // nothing to hit on it; keeps the range cue off it too
        boxes.facing = facing;

        AudioEvent.playSound(SoundType.MARIO); // TODO: a launch sound
        return boss.world().createEntity()
                .tag(GameTags.PROJECTILE)
                .add(new Name("rumble-pak"))
                .add(new Position(pos.x, pos.y))
                .add(new RumblePak(targetX, targetY, landX, targetY, Props.pakSpeed, pos.x, pos.y))
                .add(anim)
                .add(boxes)
                .add(new Behavior(RumblePakState.FLY));
    }

    /**
     * The pak goes off on its target: a sound, a cloud, and every fighter within the blast ellipse
     * takes a strike through the same rules as a landed hit ({@code GameStrikes}): the player's
     * side the blast, the boss's own side a shove with no damage (the boss itself is left alone).
     */
    public static void explode(Entity pak) {
        var rp    = RumblePak.get(pak);
        var boxes = Hitboxes.get(pak);
        AudioEvent.playSound(SoundType.DEGAUSS); // TODO: an explosion sound
        ParticleEvent.spawn(EmitterType.SPAWN_CLOUD, new SpawnCloudEffect.Params(new Position(rp.targetX, rp.targetY)), RenderLayer.OVERLAY);
        ParticleEvent.spawn(EmitterType.FIRE_EXPLOSION, new FireExplosionEffect.Params(rp.targetX, rp.targetY, 20f, 20f), RenderLayer.OVERLAY);
        var responder = new GameStrikes();
        var fighters  = Senses.fightersIn(pak.world());
        for (int i = 0; i < fighters.size; i++) {
            var f = fighters.get(i);
            if (!f.isAlive() || f.isRemoving() || f.hasTag(GameTags.BOSS)) continue;
            var target = Hitboxes.get(f);
            if (target == null || !target.vulnerable) continue;
            var pos = Position.get(f);
            float ex = (pos.x - rp.targetX) / Props.blastRadius;
            float ey = (pos.y - rp.targetY) / Props.blastDepth;
            if (ex * ex + ey * ey > 1f) continue;
            var hurt = firstHurt(target);
            if (hurt == null) continue;
            var hit = target.team.in(boxes.targets) ? Props.hitboxBlast : Props.hitboxShove;
            responder.strike(new Strike(pak, hit, f, hurt, boxes.facing, pos.x - rp.targetX, rp.targetY - pos.y));
        }
    }

    /** The first HURT box this fighter always has out, for the strike report; null for one with none. */
    private static Hitbox firstHurt(Hitboxes boxes) {
        if (boxes.set == null) return null;
        for (var box : boxes.set.base) {
            if (box.isHurt()) return box;
        }
        return null;
    }
}
