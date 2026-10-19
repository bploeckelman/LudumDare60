package lando.systems.ld60.game.characters;

import lando.systems.ld60.game.anim.Move;
import lando.systems.ld60.game.combat.HitboxSet;

/**
 * How one kind of fighter uses one weapon, its own hands included: the hit and hurt boxes it
 * fights with while holding it (the attack's windows keyed under the clip names), the basic
 * attack, and an optional second move ({@code low}: the player's down + attack when nothing is
 * underfoot, one of the two an AI picks from). A {@link Kind} maps weapon names to these; the
 * {@code Weapon} itself knows nothing about any rig.
 */
public record Wield(HitboxSet set, Move attack, Move low) {

    public static Wield of(HitboxSet set, Move attack) {
        return new Wield(set, attack, null);
    }
}
