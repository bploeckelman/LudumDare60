package lando.systems.ld60.game.characters;

import com.badlogic.gdx.math.Rectangle;
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
 * A fighter made from one picture on the bendy rig ({@code docs/spine/bendy/}): the rig bends,
 * bounces and tips the picture, so every bendy character has the same clip names, events and
 * anchor bones ({@code strike} at the top front corner, {@code center} 60 % up the picture),
 * and only its size, health and pace differ. {@link #kind} turns a {@link Props} into a
 * {@link Kind}; a new enemy is one more {@code Props} line here, one {@code GameSpine} entry and
 * one entry in {@link Kinds#ALL}.
 */
public final class Bendy {

    private Bendy() {}

    /**
     * What differs per picture. {@code width} and {@code height} are the rig's setup bounds in
     * skeleton units (the {@code skeleton} block of the export, printed by {@code build_bendy.py});
     * {@code scale} is the per-entity draw scale that makes it street-sized (about 85 px tall).
     */
    public record Props(String name, GameSpine rig, float scale, float width, float height, int health) {
        /** Drawn size in pixels. */
        float drawnWidth()  { return width * scale; }
        float drawnHeight() { return height * scale; }
    }

    public static final Props BLACKBERRY = new Props("blackberry", GameSpine.BLACKBERRY, 0.5f,  107, 164, 4);   // 53 x 82 px
    public static final Props BRICKPHONE = new Props("brickphone", GameSpine.BRICKPHONE, 0.3f,   82, 293, 5);   // 25 x 88 px
    public static final Props CASSETTE   = new Props("cassette",   GameSpine.CASSETTE,   0.3f,   82, 293, 5);   // TODO: author
    public static final Props FOOTBALL   = new Props("football",   GameSpine.FOOTBALL,   0.3f,   82, 293, 5);   // TODO: author
    public static final Props PROF       = new Props("prof",       GameSpine.PROF,       0.3f,   82, 293, 5);   // TODO: author
    public static final Props SPEAK      = new Props("speak",      GameSpine.SPEAK,      0.3f,   82, 293, 5);   // TODO: author

    /** The clip names in the rig (the same set the AWOL rig has, so the game treats both alike). */
    static final String IDLE = "idle", WALK = "walk", RUN = "run", JUMP = "jump", PUNCH = "punch", KICK = "kick",
            SWING = "swing", THROW = "throw", HURT = "hurt", KNOCKBACK = "knockback", DEATH = "death";

    /** Attacks: a headbutt, a low shove, and the whole-body chop as the power move. */
    static final Hitbox.Attack HEADBUTT = Hitbox.attack(/*damage*/ 1, /*knockback*/ 4, /*height*/ 3);
    static final Hitbox.Attack SHOVE    = Hitbox.attack(/*damage*/ 1, /*knockback*/ 5, /*height*/ 1);
    static final Hitbox.Attack CHOP     = Hitbox.attackHeavy(/*damage*/ 3, /*knockback*/ 8, /*height*/ 3);

    /** The power move parks this far into the swing (frame 5, wound up, before the window) while attack is held. */
    static final float POWER_WINDUP = 0.17f;

    /** A bendy character's pace: a brick on the move is not quick. A thug role overrides this anyway. */
    static final BrawlerTuning TUNING = new BrawlerTuning(Factory.UNIT,
            /*speedX*/ 5, /*speedY*/ 2, /*accelTime*/ 0.2f, /*stopTime*/ 0.1f, /*jumpHeight*/ 2f, /*jumpTimeToPeak*/ 0.45f);

    public static Kind kind(Props p) {
        float w = p.drawnWidth(), h = p.drawnHeight();
        // the picture tips and bends, the boxes do not rotate: keep them inside the picture at rest
        var hurt = Hitbox.hurt("body", Hitbox.rect(-0.45f * w, -0.55f * h, 0.9f * w, 0.9f * h)).on("center");
        // the headbutt: from the top front corner back toward the body, so nothing fits between the picture and the box
        var headbutt = Hitbox.hit("punch", Hitbox.capsule(-0.3f * h, -0.1f * h, 0.45f * h, 0.2f * h), HEADBUTT).on("strike");
        // the shove: ahead of the feet, knee height (the feet lunge 10 % of the height forward in the clip)
        var shove = Hitbox.hit("kick", Hitbox.rect(0, 0, 0.6f * w + 0.1f * h, 0.25f * h), SHOVE);
        var chop = Hitbox.hit("power", Hitbox.capsule(-0.35f * h, -0.15f * h, 0.55f * h, 0.3f * h), CHOP).on("strike").depth(1.5f * Hitbox.DEPTH);
        // windows open at the snap (frame 5, 0.17 s) and close at the end of the hold (frame 11 / 12)
        var fists = new HitboxSet().base(hurt)
                .window(PUNCH, 0.17f, 0.37f, headbutt)
                .window(KICK,  0.17f, 0.37f, shove)
                .window(SWING, 0.17f, 0.40f, chop);
        int feetWidth = Math.round(Math.min(36, 0.8f * w));
        return new Kind(p.name(),
                TUNING,
                p.health(),
                new Rectangle(-feetWidth / 2f, 0, feetWidth, 12),
                facing -> animator(p, facing),
                Bendy::clips,
                GameWeapons.FISTS,
                Map.of(GameWeapons.FISTS.name(), new Wield(fists,
                        /*attack*/ Move.of(AnimClip.spine(PUNCH)),
                        /*low*/    Move.of(AnimClip.spine(KICK)),
                        /*power*/  new Wield.Power(Move.of(AnimClip.spine(SWING)), POWER_WINDUP))),
                Bendy::dress);
    }

    static SpineAnimator animator(Props p, int facing) {
        var anim = new SpineAnimator(p.rig(), IDLE);
        anim.defaultScale.set(p.scale(), p.scale());
        anim.scale.set(p.scale(), p.scale());
        anim.facing = facing;
        return anim;
    }

    static BrawlerAnimClips clips() {
        return new BrawlerAnimClips(
                AnimClip.spine(IDLE),
                AnimClip.spine(WALK),
                AnimClip.spine(RUN),
                AnimClip.spine(JUMP),
                TUNING.maxSpeedX())
                .hurt(AnimClip.spine(KNOCKBACK))   // thrown through the air
                .flinch(AnimClip.spine(HURT))      // shoved along the floor
                .dead(AnimClip.spine(DEATH))
                .throwing(AnimClip.spine(THROW));
    }

    /** After spawn: the rig's "footstep" to a dust emitter, stand-in sounds for the rest. */
    static void dress(Factory factory, Entity e) {
        factory.emitter(EmitterType.DUST, new FootStepDustCloudEffect.Params(e));
        SpineAnimator.get(e).onEvent(event -> {
            switch (event.getData().getName()) {
                case "hurt"  -> AudioEvent.playSound(SoundType.SoundGroup.GRUNTS);
                case "swing" -> AudioEvent.playSound(SoundType.SWING1, .75f);
                case "thud"  -> AudioEvent.playSound(SoundType.BELL_LOW);
                default -> {} // footstep: the emitter; jump / throw: a landed hit is better heard from CombatEvent
            }
        });
    }
}
