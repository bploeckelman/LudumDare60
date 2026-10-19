package lando.systems.ld60.game.characters;

import com.badlogic.gdx.math.Rectangle;
import com.zendogames.zenlib.ecs.components.Animator;
import lando.systems.ld60.assets.AnimAtari;
import lando.systems.ld60.game.Factory;
import lando.systems.ld60.game.GameWeapons;
import lando.systems.ld60.game.ai.Minds;
import lando.systems.ld60.game.anim.AnimClip;
import lando.systems.ld60.game.anim.Move;
import lando.systems.ld60.game.combat.Hitbox;
import lando.systems.ld60.game.combat.HitboxSet;
import lando.systems.ld60.game.components.BrawlerAnimClips;
import lando.systems.ld60.game.components.Hurtable;
import lando.systems.ld60.game.movement.BrawlerTuning;
import lando.systems.ld60.game.state.AtariBossState;

import java.util.Map;
import java.util.Set;

/**
 * The Atari joystick boss ({@link AnimAtari}, a sprite fighter), the {@link N64}'s smaller cousin:
 * {@link Props} is the authored data, {@link #KIND} is what the factory spawns, {@link #ROLE} its
 * boss role with its own state machine ({@code AtariBossState}). It stomps along the street
 * (the walk clip hops, and {@link Props#danceHeight} adds to the hop), pokes with the stick
 * (the {@code ATTACK} clip, the box out on the two horizontal frames and reaching past the tip),
 * and whips the cord round (the {@code SPIN} clip, a wide sweep on its two lashing frames).
 * <p>
 * The art is 384x384 frames of the joystick with the stick leaning right and the cord trailing
 * behind, the body sitting on the frame bottom (the floor line). Everything below is authored in
 * <b>frame pixels</b> and scaled through {@link Props#px} so {@link Props#scale} is the one knob
 * for the boss's size; {@code Props.originX} is the body's center column.
 */
public final class Atari {

    private Atari() {}

    public static class Props {
        // Misc ---------------------------------------------------------------
        public static final int   maxHealth = 25;
        /** Frame pixels to drawn pixels: every number below is in frame pixels through {@link #px}. */
        public static final float scale     = 0.6f;
        /** The body's center column in the frame; the origin's y is the frame bottom, the floor line. */
        public static final float originX   = 197;
        public static float px(float framePixels) { return framePixels * scale; }
        // Movement tuning: a stomp, not a glide ------------------------------
        public static final int   unit           = Factory.UNIT;
        public static final int   speedX         = 4;     // tiles/sec
        public static final int   speedY         = 2;     // tiles/sec
        public static final float accelTime      = 0.15f; // sec
        public static final float stopTime       = 0.1f;  // sec
        public static final float jumpHeight     = 1.0f;  // tiles (never jumps)
        public static final float jumpTimeToPeak = 0.3f;  // sec
        /** The dance: the walk clip's own hop plus this much more height (drawn px), this many hops per walk cycle; 0 leaves the art alone. */
        public static final float danceHeight = 10f, danceBeats = 1f;
        /** The walk frames carry the body sideways (each step's lunge), by this much per frame; the state re-anchors the art so the entity's own speed is the only travel. All zeros lets the art slide and snap. */
        public static final float[] walkDrift = { 0, 0, 0, 0, 17, 27, 42 };
        // Collision ----------------------------------------------------------
        public static final Rectangle feet = new Rectangle(px(-90), 0, px(180), 12);
        // The brain's numbers (s; px in frame pixels) ------------------------
        /** The poke's reach and the whip's (the far edge of each box), where it stands to poke, within {@code settle} drawn px; street depth it attacks within. */
        public static final float pokeReach = px(190), whipReach = px(230), standoff = px(150), settle = 10;
        public static final int   depth = 10;
        public static final float pokeRestMin = 0.7f, pokeRestMax = 1.3f;
        public static final float whipRestMin = 3f,   whipRestMax = 5f;
        /** The stagger: how long it reels, and how far the art shudders sideways (frame px) at full intensity. */
        public static final float staggerTime = 0.45f, shakeMax = 10;
        /** Hit box names that stagger it at once (and knock it out of a whip); a plain hit staggers it every so many hits while idle. */
        public static final Set<String> heavyHits = Set.of("power", "swing");
        public static final int staggerEvery = 3;
        /** Dead: the art squashes flat onto the floor, to these factors of {@link #scale}. */
        public static final float deathSquashX = 1.2f, deathSquashY = 0.2f, deathSquashSpeed = 1.5f;
        // Attacks / Hit, Hurt boxes (frame pixels; windows in seconds into the clip) ---------------
        public static final Hitbox.Attack attackPoke = Hitbox.attack     (/*damage*/ 2, /*knockback*/ 4, /*height*/ 2);
        public static final Hitbox.Attack attackWhip = Hitbox.attackHeavy(/*damage*/ 3, /*knockback*/ 7, /*height*/ 3);
        /** The body: the box from the floor up to its top face. */
        public static final Hitbox hurtboxBody  = Hitbox.hurt("body",  Hitbox.rect(px(-100), 0, px(200), px(112))).depth(10);
        /** The stick, standing up out of the body. */
        public static final Hitbox hurtboxStick = Hitbox.hurt("stick", Hitbox.rect(px(3), px(112), px(28), px(110)));
        /** The poke: the stick lies level at 120..160 px (frames 4 and 5, tip at +120); the box runs past the tip to +190 and down to a player's chest. */
        public static final Hitbox hitboxPoke   = Hitbox.hit("poke", Hitbox.capsule(px(40), px(95), px(150), px(70)), attackPoke);
        /** The whip: the cord lashes over the top and down ahead (frames 1 and 2, out to +180); a wide sweep past that, thick along the street. */
        public static final Hitbox hitboxWhip   = Hitbox.hit("cord", Hitbox.capsule(px(60), px(50), px(170), px(130)), attackWhip).depth(14);
        public static final HitboxSet set = new HitboxSet().base(hurtboxBody, hurtboxStick)
                .window(AnimAtari.ATTACK.name(), /*start*/ 0.4f, /*end*/ 0.6f, hitboxPoke)
                .window(AnimAtari.SPIN.name(),   /*start*/ 0.1f, /*end*/ 0.3f, hitboxWhip);
        // The states' moves ---------------------------------------------------
        public static final Move pokeMove = Move.of(AnimClip.sprite(AnimAtari.ATTACK));
        public static final Move whipMove = Move.of(AnimClip.sprite(AnimAtari.SPIN));
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

    /** A frame of the joystick's art at the boss's size, floor line at the frame bottom. */
    static Animator animator(int facing) {
        var anim = new Animator(AnimAtari.IDLE, Props.originX, 0);
        anim.defaultScale.set(Props.scale, Props.scale);
        anim.scale.set(Props.scale, Props.scale);
        anim.facing = facing;
        return anim;
    }

    /** Idle and the hopping walk; it never jumps or flies (super armor), and death is the idle clip squashed flat ({@code AtariBossState.DEAD}). */
    static BrawlerAnimClips animClips() {
        var idle = AnimClip.sprite(AnimAtari.IDLE);
        var walk = AnimClip.sprite(AnimAtari.WALK);
        return new BrawlerAnimClips(idle, walk, walk, idle, Float.MAX_VALUE);
    }

    public static final Kind KIND = new Kind("atari",
            tuning,
            Props.maxHealth,
            Props.feet,
            Atari::animator,
            Atari::animClips,
            GameWeapons.FISTS,
            Map.of(GameWeapons.FISTS.name(), new Wield(Props.set, Props.pokeMove, Props.whipMove)),
            null);

    /** Its boss role: its own states, super armor, the stagger and death hooks into them. */
    public static final Role ROLE = Roles.boss(Minds.states(AtariBossState.IDLE),
            () -> new Hurtable().knockback(0).recoverTime(0.4f).despawnAfter(2.5f)
                    .deadState(AtariBossState.DEAD)
                    .onHurt(AtariBossState::onHurt));
}
