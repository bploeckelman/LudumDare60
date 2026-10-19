package lando.systems.ld60.game.characters;

import com.badlogic.gdx.math.Rectangle;
import com.zendogames.zenlib.ecs.Entity;
import com.zendogames.zenlib.ecs.components.SpineAnimator;
import lando.systems.ld60.assets.EmitterType;
import lando.systems.ld60.assets.GameSpine;
import lando.systems.ld60.game.Factory;
import lando.systems.ld60.game.GameHitboxes;
import lando.systems.ld60.game.GameWeapons;
import lando.systems.ld60.game.anim.AnimClip;
import lando.systems.ld60.game.anim.Move;
import lando.systems.ld60.game.combat.Hitbox;
import lando.systems.ld60.game.combat.HitboxSet;
import lando.systems.ld60.game.components.BrawlerAnimClips;
import lando.systems.ld60.game.movement.BrawlerTuning;
import lando.systems.ld60.particles.effects.FootStepDustCloudEffect;

/** The stock Spine example as a fighter: the blaster is its hands, a pipe is swung on the same 'shoot' clip (it has no swing). */
public final class Spineboy {

    private Spineboy() {}

    /** Load scale 0.25 in {@link GameSpine}, half size again per entity: about 85 px tall. */
    static final float SCALE = 0.5f;

    /** The player's pace (the {@code player} builder's numbers); a thug role is slower. */
    static final BrawlerTuning TUNING = new BrawlerTuning(
            Factory.UNIT, // authoring unit -> pixels
            12,           // x-axis speed (tiles/sec)
            6,            // y-axis speed (tiles/sec)
            0.1f,         // accel time
            0.08f,        // stop time
            2.5f,         // 'jump' height (tiles)
            0.35f);       // 'jump' time to peak (seconds)

    /** The 'shoot' clip with the gun leveled on a second track (spineboy's rest pose points it at the floor). */
    static final Move SHOOT = new Move(AnimClip.spine("shoot"), e -> GameSpine.aim(SpineAnimator.get(e)));

    /** The pipe: a bigger, harder window on the same clip, two damage, a tile of lift. */
    static final HitboxSet PIPE_SET = new HitboxSet()
            .base(GameHitboxes.SPINEBOY.base.toArray(Hitbox[]::new))
            .window("shoot", /*start*/ 0.03f, /*end*/ 0.25f,
                    Hitbox.hit("swing", Hitbox.rect(0, -10, 56, 24), Hitbox.attack(/*damage*/ 2, /*knockback*/ 2f, /*height*/ 1f)).on("muzzle"));

    static SpineAnimator animator(int facing) {
        var anim = new SpineAnimator(GameSpine.SPINEBOY, "idle");
        anim.defaultScale.set(SCALE, SCALE);
        anim.scale.set(SCALE, SCALE);
        anim.facing = facing;
        return anim;
    }

    static BrawlerAnimClips clips() {
        return new BrawlerAnimClips(
                AnimClip.spine("idle"),
                AnimClip.spine("walk"),
                AnimClip.spine("run"),
                AnimClip.spine("jump"),
                TUNING.maxSpeedX() * 0.5f)
                .stop(AnimClip.spine("run-to-idle"))
                .dead(AnimClip.spine("death"));
    }

    static void dress(Factory factory, Entity e) {
        factory.emitter(EmitterType.DUST, new FootStepDustCloudEffect.Params(e)); // the rig's "footstep" event
    }

    public static final Kind KIND = new Kind("spineboy",
            TUNING,
            /*health*/ 10,
            /*feet*/ new Rectangle(-18, 0, 36, 12), // only the feet: the sprite's top half may overlap a wall behind it
            Spineboy::animator,
            Spineboy::clips,
            GameWeapons.BLASTER,
            java.util.Map.of(
                    GameWeapons.BLASTER.name(), Wield.of(GameHitboxes.SPINEBOY, SHOOT),
                    GameWeapons.PIPE.name(),    Wield.of(PIPE_SET, SHOOT)),
            Spineboy::dress);
}
