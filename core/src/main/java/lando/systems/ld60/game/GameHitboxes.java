package lando.systems.ld60.game;

import lando.systems.ld60.game.combat.Hitbox;
import lando.systems.ld60.game.combat.HitboxSet;

/**
 * Every fighter's hit and hurt shapes, one {@link HitboxSet} per character, in pixels at the size
 * the character is drawn. Tune with the overlay on ({@code H}) and frame step ({@code 0/9}).
 */
public final class GameHitboxes {

    /** The example's only attack: spineboy's shot. One damage, a tile and a half of shove, no lift. */
    public static final Hitbox.Attack SHOT = new Hitbox.Attack(1, 1.5f, 0f);

    /**
     * spineboy at the scroller's size (load scale 0.25, {@code defaultScale} 0.5, about 85 px tall).
     * Head and body ride their bones, so idle, walk, run and jump need no windows of their own; the
     * shot is out while the muzzle flash shows (0.03 to 0.2 s into "shoot").
     */
    public static final HitboxSet SPINEBOY = new HitboxSet()
            .base(Hitbox.hurt("head", Hitbox.circ(2, 16, 15)).on("head"),
                  Hitbox.hurt("body", Hitbox.rect(-8, -28, 16, 46)).on("hip"))
            .window("shoot", 0.03f, 0.2f, Hitbox.hit("shot", Hitbox.rect(4, -6, 40, 12), SHOT).on("muzzle"));

    public static final Hitbox.Attack AWOL_PUNCH = new Hitbox.Attack(/*dmg*/ 1, /*knockback*/ 4, /*height*/ 3);

    /**
     * The awol rig at 0.35 scale (about 85 px tall): the disc is a circle on the {@code body} bone;
     * the glove rides the {@code front-fist} anchor (the glove's center) while the punch is extended
     * and held (frames 4 to 8 of 12 at 30 fps: 0.13 to 0.28 s), not during the wind-up before it.
     */
    public static final HitboxSet AWOL = new HitboxSet()
            .base(Hitbox.hurt("body", Hitbox.circ(0, 0, 25)).on("body"))
            .window("punch", /*start*/ 0.13f, /*end*/ 0.28f,
                    Hitbox.hit("punch", Hitbox.circ(0, 0, 9), AWOL_PUNCH).on("front-fist"));

    private GameHitboxes() {}
}
