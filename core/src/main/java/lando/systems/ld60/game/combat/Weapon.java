
package lando.systems.ld60.game.combat;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.GdxRuntimeException;
import lando.systems.ld60.game.anim.Move;

/**
 * A fighter's armament, bare hands included: the {@link HitboxSet} it fights with (swapped onto
 * {@code Hitboxes.set} while held), the basic attack {@link Move}, and for a weapon that can be
 * thrown, what the flying item does when it hits and how it flies. Plain shared data, one
 * instance per kind in {@code GameWeapons}; the per-fighter state (which one, how worn) is the
 * {@code Held} component.
 *
 * @param name        label for logs and the Tiled {@code pickup.weapon} member
 * @param thrown      what the thrown item does on a hit; null for a weapon that cannot be thrown
 * @param throwDist   tiles the item flies along the street
 * @param throwHeight tiles the item arcs up
 * @param durability  hits landed before it breaks; 0 never breaks
 * @param region      the floor item's and the flying item's picture; null draws a tinted pixel block
 */
public record Weapon(
        String name,
        Hitbox.Attack thrown,
        float throwDist,
        float throwHeight,
        int durability,
        TextureRegion region
) {
    public Weapon {
        if (name == null) throw new GdxRuntimeException("Weapon: name must be non-null");
    }

    /** Hands, or a weapon welded to the fighter: cannot be thrown or lost. */
    public static Weapon fixed(String name) {
        return new Weapon(name, null, 0, 0, 0, null);
    }

    public boolean throwable() { return thrown != null; }
    public boolean breaks()    { return durability > 0; }

    /** A copy with a picture. */
    public Weapon region(TextureRegion region) {
        return new Weapon(name, thrown, throwDist, throwHeight, durability, region);
    }
}
