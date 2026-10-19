package lando.systems.ld60.game.characters;

import com.badlogic.gdx.math.Rectangle;
import com.zendogames.zenlib.Log;
import com.zendogames.zenlib.ecs.Entity;
import com.zendogames.zenlib.ecs.components.SpineAnimator;
import lando.systems.ld60.assets.EmitterType;
import lando.systems.ld60.assets.GameSpine;
import lando.systems.ld60.assets.SoundType;
import lando.systems.ld60.events.AudioEvent;
import lando.systems.ld60.game.Factory;
import lando.systems.ld60.game.GameWeapons;
import lando.systems.ld60.game.anim.AnimClip;
import lando.systems.ld60.game.anim.Move;
import lando.systems.ld60.game.combat.Hitbox;
import lando.systems.ld60.game.combat.HitboxSet;
import lando.systems.ld60.game.components.BrawlerAnimClips;
import lando.systems.ld60.game.movement.BrawlerTuning;
import lando.systems.ld60.particles.effects.FootStepDustCloudEffect;

import java.util.Map;

/**
 * The AWOL fighter (the disc with gloves, {@code docs/spine/}): everything about the character
 * and nothing about who it is for. {@link Props} is the authored data, the methods below build
 * the components from it, {@link #KIND} is what the factory spawns.
 */
public final class Awol {

    private Awol() {}

    public static class Props {
        // Misc ---------------------------------------------------------------
        public static final int maxHealth = 10;
        public static final float scale   = 0.35f; // TODO: might be a little small, test 0.5f
        // Movement tuning ----------------------------------------------------
        public static final int   unit           = Factory.UNIT; // authoring unit (tiles) -> pixels
        public static final int   speedX         = 6;    // tiles/sec
        public static final int   speedY         = 2;    // tiles/sec
        public static final float accelTime      = 0.2f; // sec
        public static final float stopTime       = 0.1f; // sec
        public static final float jumpHeight     = 2.0f; // tiles
        public static final float jumpTimeToPeak = 0.5f; // sec
        // Animation: the clip names in the rig -------------------------------
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
        // Collision ----------------------------------------------------------
        public static final Rectangle feet = new Rectangle(-18, 0, 36, 12);
        // Attacks / Hit, Hurt boxes (drawn pixels at `scale`; windows in seconds into the clip) --
        public static final Hitbox.Attack attackPunch = Hitbox.attack(/*damage*/ 1, /*knockback*/ 4, /*height*/ 3);
        public static final Hitbox.Attack attackKick  = Hitbox.attack(/*damage*/ 2, /*knockback*/ 5, /*height*/ 2);
        public static final Hitbox.Attack attackPipe  = Hitbox.attack(/*damage*/ 2, /*knockback*/ 2, /*height*/ 1);
        public static final Hitbox hitboxPunch = Hitbox.hit("punch", Hitbox.circ(0,  0,  9),    attackPunch).on("front-fist");
        public static final Hitbox hitboxKick  = Hitbox.hit("kick",  Hitbox.circ(0,  0, 10),    attackKick) .on("front-toe");
        public static final Hitbox hitboxSwing = Hitbox.hit("swing", Hitbox.rect(0, -4, 24, 8), attackPipe) .on("weapon");
        public static final Hitbox hurtboxBody = Hitbox.hurt("body", Hitbox.circ(0,  0, 25)).on("body");
        /** Bare hands: the jab and the kick, held extended (frames 7 to 11 of 16). */
        public static final HitboxSet fistsSet = new HitboxSet().base(hurtboxBody)
                .window(punchAnim, /*start*/ 0.23f, /*end*/ 0.37f, hitboxPunch)
                .window(kickAnim,  /*start*/ 0.23f, /*end*/ 0.37f, hitboxKick);
        /** The pipe: the chop, held at the bottom (frames 8 to 12.5 of 18). */
        public static final HitboxSet pipeSet = new HitboxSet().base(hurtboxBody)
                .window(swingAnim, /*start*/ 0.27f, /*end*/ 0.42f, hitboxSwing);
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

    static SpineAnimator animator(int facing) {
        var anim = new SpineAnimator(Props.spineAnim, Props.initialAnim);
        anim.defaultScale.set(Props.scale, Props.scale);
        anim.scale.set(Props.scale, Props.scale);
        anim.facing = facing;
        return anim;
    }

    static BrawlerAnimClips animClips() {
        return new BrawlerAnimClips(
                AnimClip.spine(Props.idleAnim),
                AnimClip.spine(Props.walkAnim),
                AnimClip.spine(Props.runAnim),
                AnimClip.spine(Props.jumpAnim),
                tuning.maxSpeedX())
                .hurt(AnimClip.spine(Props.knockbackAnim))   // thrown through the air
                .flinch(AnimClip.spine(Props.hurtAnim))      // shoved along the floor
                .dead(AnimClip.spine(Props.deathAnim))
                .throwing(AnimClip.spine(Props.throwAnim));
    }

    /** After spawn: the rig's "footstep" to a dust emitter, every event logged, stand-in sounds until there are real ones. */
    static void dress(Factory factory, Entity e) {
        factory.emitter(EmitterType.DUST, new FootStepDustCloudEffect.Params(e));
        SpineAnimator.get(e).onEvent(event -> {
            var name = event.getData().getName();
            Log.info("awol", "event '%s' at %.2fs of '%s'", name, event.getTime(), AnimClip.current(e));
            switch (name) {
                case "footstep" -> AudioEvent.playSound(SoundType.BELL_LOW);  // TODO: change me
                case "hurt"     -> AudioEvent.playSound(SoundType.BELL_HIGH); // TODO: change me (or remove, see default)
                case "jump"     -> AudioEvent.playSound(SoundType.BELL_HIGH); // TODO: change me (or remove, see default)
                case "swing"    -> AudioEvent.playSound(SoundType.BELL_HIGH); // TODO: change me (swing a weapon)
                case "throw"    -> AudioEvent.playSound(SoundType.BELL_HIGH); // TODO: change me (throw a weapon)
                case "thud"     -> AudioEvent.playSound(SoundType.BELL_LOW);  // TODO: change me (hit ground on death)
                default -> {} // a landed hit is better heard from CombatEvent, which knows whether a glove connected
            }
        });
    }

    public static final Kind KIND = new Kind("awol",
            tuning,
            Props.maxHealth,
            Props.feet,
            Awol::animator,
            Awol::animClips,
            GameWeapons.FISTS,
            Map.of(
                    GameWeapons.FISTS.name(), new Wield(Props.fistsSet, Move.of(AnimClip.spine(Props.punchAnim)), /*low*/ Move.of(AnimClip.spine(Props.kickAnim))),
                    GameWeapons.PIPE.name(),  Wield.of(Props.pipeSet, Move.of(AnimClip.spine(Props.swingAnim)))),
            Awol::dress);
}
