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
 * and only its size, health, pace and punch differ. {@link #kind} turns a {@link Props} into a
 * {@link Kind}; a new enemy is one more {@code Props} line here, one {@code GameSpine} entry and
 * one entry in {@link Kinds#ALL}.
 */
public final class Bendy {
    private Bendy() {}

    /** The player (AWOL at 0.35) stands about this tall; the sizes below are set against it. */
    static final float PLAYER_HEIGHT = 92;

    /**
     * What differs per picture.
     *
     * @param width, height the rig's setup bounds in skeleton units: the {@code skeleton} block of the
     *                      export, also printed by {@code build_bendy.py} ("-> W x H units")
     * @param drawnHeight   how tall it stands on the street, in pixels; the draw scale follows from it
     * @param tuning        its pace; only felt once a thug role stops overriding it (see {@code Roles.thug})
     * @param headbutt      what the punch does
     * @param chop          what the power move does (the player's hold-and-release; thugs never use it)
     */
    public record Props(String name, GameSpine rig, float width, float height, float drawnHeight, int health,
                        BrawlerTuning tuning, Hitbox.Attack headbutt, Hitbox.Attack chop) {
        float scale()      { return drawnHeight / height; }
        float drawnWidth() { return width * scale(); }
    }

    static BrawlerTuning pace(float speedX, float speedY, float accelTime, float stopTime, float jumpHeight, float jumpTimeToPeak) {
        return new BrawlerTuning(Factory.UNIT, speedX, speedY, accelTime, stopTime, jumpHeight, jumpTimeToPeak);
    }

    //                                            name          rig                   W      H     tall  hp  pace: x    y   accel  stop  jump  peak     headbutt: dmg kb  h          chop: dmg kb h
    public static final Props BLACKBERRY = new Props("blackberry", GameSpine.BLACKBERRY, 107,   164,  80,   3,  pace(7,   3,   0.15f, 0.08f, 2.5f, 0.35f), Hitbox.attack(1, 3, 2), Hitbox.attackHeavy(2, 7, 3));   // zippy, light
    public static final Props BRICKPHONE = new Props("brickphone", GameSpine.BRICKPHONE,  82,   293, 108,   7,  pace(3.5f, 1.5f, 0.3f, 0.15f, 1.5f, 0.45f), Hitbox.attack(2, 6, 3), Hitbox.attackHeavy(3, 9, 3));   // tall, lumbering, hits hard
    public static final Props CASSETTE   = new Props("cassette",   GameSpine.CASSETTE,    88,    83,  84,   3,  pace(8,   3,   0.1f,  0.06f, 3f,   0.3f),  Hitbox.attack(1, 3, 3), Hitbox.attackHeavy(2, 6, 4));   // skittish, it has legs
    public static final Props FOOTBALL   = new Props("football",   GameSpine.FOOTBALL,    79,   124,  92,   5,  pace(5,   2,   0.15f, 0.1f,  2f,   0.4f),  Hitbox.attack(1, 4, 3), Hitbox.attackHeavy(3, 8, 3));   // the rank and file
    public static final Props PROF       = new Props("prof",       GameSpine.PROF,       103.5f, 147.5f, 92, 6, pace(4.5f, 2, 0.2f,  0.1f,  2f,   0.4f),  Hitbox.attack(1, 4, 2), Hitbox.attackHeavy(3, 8, 3));   // steady, a little sturdier
    public static final Props SPEAK      = new Props("speak",      GameSpine.SPEAK,      124.5f, 191.5f, 92, 8, pace(4,   1.5f, 0.25f, 0.12f, 1.5f, 0.45f), Hitbox.attack(2, 5, 2), Hitbox.attackHeavy(3, 9, 3));   // slow, heavy, a shove of a headbutt

    /** The clip names in the rig (the same set the AWOL rig has, so the game treats both alike). The kick is in the rig but unused: a leaning shove never read as an attack. */
    static final String IDLE = "idle", WALK = "walk", RUN = "run", JUMP = "jump", PUNCH = "punch",
            SWING = "swing", THROW = "throw", HURT = "hurt", KNOCKBACK = "knockback", DEATH = "death";

    /** The power move parks this far into the swing (frame 5, wound up, before the window) while attack is held. */
    static final float POWER_WINDUP = 0.17f;

    public static Kind kind(Props p) {
        float w = p.drawnWidth(), h = p.drawnHeight();
        // Boxes in drawn pixels. They do not rotate with the bones: the hurt box is an upright rect that
        // follows `center` (60 % up), inset from the picture's edges so a graze misses; the strike box is a
        // capsule that reaches back from `strike` (the top front corner) into the picture, so nothing fits
        // between the head and the box, and pokes only a few pixels past the front edge.
        var hurt     = Hitbox.hurt("body",  Hitbox.rect(-0.4f * w, -0.52f * h, 0.8f * w, 0.84f * h)).on("center");              // 8 % to 92 % of the height, 80 % of the width
        var headbutt = Hitbox.hit("punch",  Hitbox.capsule(-0.3f * h, -0.09f * h, 0.38f * h, 0.18f * h), p.headbutt()).on("strike");
        var chop     = Hitbox.hit("power",  Hitbox.capsule(-0.32f * h, -0.12f * h, 0.44f * h, 0.24f * h), p.chop()).on("strike").depth(1.5f * Hitbox.DEPTH);
        // windows open at the snap (frame 5, 0.17 s) and close at the end of the hold (frame 11 / 12)
        var fists = new HitboxSet().base(hurt)
                .window(PUNCH, 0.17f, 0.37f, headbutt)
                .window(SWING, 0.17f, 0.40f, chop);
        int feetWidth = Math.round(Math.min(36, 0.8f * w));
        return new Kind(p.name(),
                p.tuning(),
                p.health(),
                new Rectangle(-feetWidth / 2f, 0, feetWidth, 12),
                facing -> animator(p, facing),
                () -> clips(p),
                GameWeapons.FISTS,
                Map.of(GameWeapons.FISTS.name(), new Wield(fists,
                        /*attack*/ Move.of(AnimClip.spine(PUNCH)),
                        /*low*/    null,   // punch only
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

    static BrawlerAnimClips clips(Props p) {
        return new BrawlerAnimClips(
                AnimClip.spine(IDLE),
                AnimClip.spine(WALK),
                AnimClip.spine(RUN),
                AnimClip.spine(JUMP),
                /*runSpeed*/ 0.7f * p.tuning().maxSpeedX())   // the run hop above 70 % of top speed, the walk hop below
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
