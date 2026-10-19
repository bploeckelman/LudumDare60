package lando.systems.ld60.game;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.zendogames.zenlib.ecs.components.SpineAnimator;
import lando.systems.ld60.assets.GameSpine;
import lando.systems.ld60.game.anim.AnimClip;
import lando.systems.ld60.game.anim.Move;
import lando.systems.ld60.game.combat.Hitbox;
import lando.systems.ld60.game.combat.HitboxSet;
import lando.systems.ld60.game.combat.Weapon;

/**
 * Every weapon in the game, one shared instance each; {@link #byName} is the Tiled
 * {@code pickup.weapon} member's way in. Shapes are in pixels at the drawn size, like
 * {@code GameHitboxes}; tune with overlay on.
 */
public final class GameWeapons {

    private static final String TAG = GameWeapons.class.getSimpleName();

    private GameWeapons() {}

    /** spineboy's gun: his default, never dropped. The shot from {@link GameHitboxes#SPINEBOY}. */
    public static final Weapon BLASTER = Weapon.fixed("blaster", GameHitboxes.SPINEBOY,
            new Move(AnimClip.spine("shoot"), e -> GameSpine.aim(SpineAnimator.get(e))));

    /** The awol rig's gloves: the 'punch' clip is the whole move ({@code Move.start} plays it; no second track to set up). */
    public static final Weapon AWOL_FIST = Weapon.fixed("fist", GameHitboxes.AWOL, Move.of(AnimClip.spine("punch")));

    /** A length of pipe: a bigger, harder shot window on the same clip (spineboy has no swing), two damage, a tile of lift; thrown it flies six tiles. Breaks after five hits. */
    public static final Weapon PIPE = new Weapon("pipe",
            new HitboxSet()
                    .base(GameHitboxes.SPINEBOY.base.toArray(Hitbox.class))
                    .window("shoot", /*start*/ 0.03f, /*end*/0.25f,
                            Hitbox.hit("swing", Hitbox.rect(0, -10, 56, 24),
                            new Hitbox.Attack(/*damage*/ 2, /*knockback*/ 2f, /*height*/1f)).on("muzzle")),
            BLASTER.attack(),
            /*thrown*/ new Hitbox.Attack(/*damage*/ 2, /*knockback*/ 3f, /*height*/ 1f),
            /*dist*/ 6f,
            /*height*/ 1.5f,
            /*durability*/ 5,
            /*region*/ null);

    public static final Weapon[] ALL = { BLASTER, PIPE };

    /** The weapon named in Tiled, case-insensitive; throws listing the names so a typo fails at load. */
    public static Weapon byName(String name) {
        for (var weapon : ALL) {
            if (weapon.name().equalsIgnoreCase(name)) {
                return weapon;
            }
        }
        var names = new StringBuilder();
        for (var weapon : ALL) {
            names.append(names.isEmpty() ? "" : ", ").append(weapon.name());
        }
        throw new GdxRuntimeException(TAG + ": no weapon '%s', have: %s".formatted(name, names));
    }
}
