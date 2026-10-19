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

    /** Bare hands. */
    public static final Weapon FISTS = Weapon.fixed("fists");

    /** spineboy's gun: his hands, never dropped. */
    public static final Weapon BLASTER = Weapon.fixed("blaster");

    /** A length of pipe: thrown it flies six tiles for two damage and a tile of lift; breaks after five hits. */
    public static final Weapon PIPE = new Weapon("pipe",
            /*thrown*/ Hitbox.attack(/*damage*/ 2, /*knockback*/ 3f, /*height*/ 1f),
            /*dist*/ 6f, /*height*/ 1.5f, /*durability*/ 5, /*region*/ null);

    /** What Tiled can place on the floor. */
    public static final Weapon[] ALL = { PIPE };

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
