package lando.systems.ld60.game.characters;

import com.badlogic.gdx.math.Rectangle;
import com.zendogames.zenlib.collision.CollisionMask;
import com.zendogames.zenlib.collision.shapes.CollisionCaps;
import com.zendogames.zenlib.ecs.components.Collider;
import com.zendogames.zenlib.ecs.components.Input;
import com.zendogames.zenlib.ecs.components.SpineAnimator;
import com.zendogames.zenlib.ecs.components.Timers;
import lando.systems.ld60.assets.GameSpine;
import lando.systems.ld60.game.Factory;
import lando.systems.ld60.game.GameMask;
import lando.systems.ld60.game.anim.AnimClip;
import lando.systems.ld60.game.anim.Move;
import lando.systems.ld60.game.combat.Hitbox;
import lando.systems.ld60.game.combat.HitboxSet;
import lando.systems.ld60.game.combat.Weapon;
import lando.systems.ld60.game.components.BrawlerAnimClips;
import lando.systems.ld60.game.components.Hitboxes;
import lando.systems.ld60.game.components.Hurtable;
import lando.systems.ld60.game.movement.BrawlerMotion;
import lando.systems.ld60.game.movement.BrawlerTuning;

public final class Awol {

    private Awol() {}

    public static class Props {
        // Misc ---------------------------------------------------------------
        public static final int maxHealth = 10;
        // Movement tuning ----------------------------------------------------
        public static final int   unit           = Factory.UNIT; // authoring unit (tiles) -> pixels
        public static final int   speedX         = 6;    // tiles/sec
        public static final int   speedY         = 2;    // tiles/sec
        public static final float accelTime      = 0.2f; // sec
        public static final float stopTime       = 0.1f; // sec
        public static final float jumpHeight     = 2.0f; // tiles
        public static final float jumpTimeToPeak = 0.5f; // sec
        // Animation ----------------------------------------------------------
        public static final GameSpine spineAnim  = GameSpine.AWOL;
        public static final String deathAnim     = "death";
        public static final String hurtAnim      = "hurt";
        public static final String idleAnim      = "idle";
        public static final String jumpAnim      = "jump";
        public static final String kickAnim      = "kick";
        public static final String knockbackAnim = "knockback";
        public static final String punchAnim     = "punch";
        public static final String runAnim       = "run";
        public static final String swingAnim     = "swing";
        public static final String throwAnim     = "throw";
        public static final String walkAnim      = "walk";
        public static final String initialAnim   = idleAnim;
        public static final AnimClip flinchClip  = AnimClip.spine(hurtAnim);
        public static final AnimClip punchClip   = AnimClip.spine(punchAnim);
        public static final AnimClip kickClip    = AnimClip.spine(kickAnim);
        public static final AnimClip swingClip   = AnimClip.spine(swingAnim);
        // Durations / Timers -------------------------------------------------
        public static final float despawnAfter = 2.0f; // sec
        public static final String thinkTimer  = "think";
        // Collision ----------------------------------------------------------
        public static final Rectangle       colliderBounds = new Rectangle(-18, 0, 36, 12);
        public static final CollisionMask[] blockedByMasks = new CollisionMask[] { GameMask.SOLID };
        public static final CollisionMask[] detectsMasks   = new CollisionMask[] { GameMask.PICKUP };
        // Attacks / Hit, Hurt boxes ------------------------------------------
        public static final Hitbox.Attack attackPunch = Hitbox.attack(/*damage*/ 1, /*knockback*/ 4, /*height*/ 3);
        public static final Hitbox.Attack attackKick  = Hitbox.attack(/*damage*/ 2, /*knockback*/ 5, /*height*/ 2);
        public static final Hitbox.Attack attackPipe  = Hitbox.attack(/*damage*/ 2, /*knockback*/ 2, /*height*/ 1);
        public static final Hitbox.Attack attackThrow = Hitbox.attack(/*damage*/ 2, /*knockback*/ 3, /*height*/ 1);
        public static final Hitbox    hitboxPunch = Hitbox.hit("punch", Hitbox.circ(0,  0,  9),    Props.attackPunch).on("front-fist");
        public static final Hitbox    hitboxKick  = Hitbox.hit("kick",  Hitbox.circ(0,  0, 10),    Props.attackKick) .on("front-toe");
        public static final Hitbox    hitboxSwing = Hitbox.hit("swing", Hitbox.rect(0, -4, 24, 8), Props.attackPipe) .on("weapon");
        public static final Hitbox    hurtboxBody = Hitbox.hurt("body", Hitbox.circ(0,  0, 25)).on("body");
        public static final HitboxSet hitboxSet   = new HitboxSet().base(hurtboxBody)
                .window(/*clip*/ "punch", /*start*/ 0.23f, /*end*/ 0.37f, /*boxes*/ Props.hitboxPunch)
                .window(/*clip*/ "kick",  /*start*/ 0.23f, /*end*/ 0.37f, /*boxes*/ Props.hitboxKick);
        public static final Weapon unarmedWeapon = Weapon.fixed("punch", hitboxSet, Move.of(punchClip));
        public static final Move   kickWeapon    = Move.of(kickClip); // TODO: this is a little janky, stuff isn't built for two unarmed attacks at the moment
        public static final Weapon pipeWeapon    = new Weapon("pipe",
                new HitboxSet()
                        .base(hurtboxBody)
                        .window(/*clip*/ "swing", /*start*/ 0.27f, /*end*/ 0.42f, /*boxes*/ hitboxSwing),
                Move.of(swingClip), /*thrown*/ attackThrow, /*dist*/ 6f, /*height*/ 1.5f, /*durability*/ 5, /*region*/ null);
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

    public static BrawlerMotion motion(int facing) {
        var motion = new BrawlerMotion(tuning);
        motion.facing = facing;
        return motion;
    }

    public static BrawlerAnimClips animClips() {
        var clips = new BrawlerAnimClips(
                AnimClip.spine(Props.idleAnim),
                AnimClip.spine(Props.walkAnim),
                AnimClip.spine(Props.runAnim),
                AnimClip.spine(Props.jumpAnim),
                tuning.maxSpeedX());
        return clips
                .hurt(AnimClip.spine(Props.knockbackAnim))
                .dead(AnimClip.spine(Props.deathAnim))
                .throwing(AnimClip.spine(Props.throwAnim));
    }

    public static SpineAnimator animator(int facing) {
        var s = 0.35f; // TODO: might be a little small, test 0.5f
        var anim = new SpineAnimator(Props.spineAnim, Props.initialAnim);
        anim.defaultScale.set(s, s);
        anim.scale.set(s, s);
        anim.facing = facing;
        return anim;
    }

    public static Collider collider() {
        return Collider.capsule(GameMask.ENEMY, Props.colliderBounds, CollisionCaps.Axis.HORIZONTAL)
                .blockedBy(Props.blockedByMasks)
                .detects(Props.detectsMasks);
    }

    public static Hurtable hurtable(BrawlerMotion motion, Timers timers) {
        return new Hurtable().despawnAfter(Props.despawnAfter)
                .onHurt((entity, strike) -> {
                    // shoved along the floor: flinch (present system doesn't change clips it doesn't own until they complete)
                    if (motion.grounded) {
                        Props.flinchClip.start(entity, false);
                    }
                })
                .onDeath((entity, strike) -> {
                    timers.cancel(Props.thinkTimer);
                    Input.get(entity).reset();
                });
    }

    public static Hitboxes hitboxes() {
        return new Hitboxes(Props.unarmedWeapon.set(), GameMask.ENEMY, GameMask.PLAYER);
    }
}
