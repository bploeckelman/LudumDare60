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

    public static final HitboxSet AWOL = new HitboxSet()
            .base(Hitbox.hurt("body", Hitbox.circ(0, 0, 35)).on("body-awol"))
            .window("punch", 0.03f, 0.2f, Hitbox.hit("punch", Hitbox.rect(4, -6, 40, 12), SHOT).on("left-hand"));

    private GameHitboxes() {}
}
