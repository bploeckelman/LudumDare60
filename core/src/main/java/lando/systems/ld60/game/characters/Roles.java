package lando.systems.ld60.game.characters;

import com.badlogic.gdx.graphics.Color;
import lando.systems.ld60.game.Drops;
import lando.systems.ld60.game.Factory;
import lando.systems.ld60.game.GameMask;
import lando.systems.ld60.game.GameTags;
import lando.systems.ld60.game.ai.Mind;
import lando.systems.ld60.game.ai.Minds;
import lando.systems.ld60.game.ai.ThugMind;
import lando.systems.ld60.game.components.Hurtable;
import lando.systems.ld60.game.movement.BrawlerTuning;
import lando.systems.ld60.game.state.BrawlerState;

/** The roles a fighter can be spawned in. */
public final class Roles {

    private Roles() {}


    /** The player's movement tuning. */
    public static final BrawlerTuning PLAYER_TUNING = new BrawlerTuning(
            Factory.UNIT, // authoring unit -> pixels
            12,            // x-axis speed (tiles/sec)
            6,             // y-axis speed (tiles/sec)
            0.1f,         // accel time
            0.08f,        // stop time
            3.0f,         // 'jump' height (tiles)
            0.25f);       // 'jump' time to peak (seconds)

    /** The one on the keyboard: {@code BrawlerState} reads its buttons, hits go through its states, the death clip is the look. */
    public static final Role PLAYER = new Role("player", GameTags.PLAYER,
            /*body*/ GameMask.PLAYER, /*detects*/ masks(GameMask.TRIGGER, GameMask.PICKUP),
            /*team*/ GameMask.PLAYER, /*targets*/ masks(GameMask.ENEMY),
            PLAYER_TUNING,
            () -> new Hurtable().hurtState(BrawlerState.HURT).deadState(BrawlerState.DEAD).deathTint(Color.LIGHT_GRAY),
            Minds.player(),
            /*healthBar*/ false);

    /** The rank and file's pace, whatever the kind: five tiles a second along the street, two up it (the {@code thug} builder's numbers). */
    public static final BrawlerTuning THUG_TUNING = new BrawlerTuning(
            Factory.UNIT, // authoring unit -> pixels
            5,            // x-axis speed (tiles/sec)
            2,            // y-axis speed (tiles/sec)
            0.1f,         // accel time
            0.08f,        // stop time
            2.5f,         // 'jump' height (tiles)
            0.35f);       // 'jump' time to peak (seconds)

    /** Rank and file: slower than the player, a {@link ThugMind} brain, flashes, flies, dies and vanishes. */
    public static final Role THUG = thug(new ThugMind(/*reach*/ 3 * Factory.UNIT, /*cooldown*/ 1.2f));

    /** A thug with the given brain (a kind that fights differently, a test dummy with {@link Minds#none()}). */
    public static Role thug(Mind mind) {
        return new Role("thug", GameTags.ENEMY,
                GameMask.ENEMY, masks(GameMask.PICKUP),
                GameMask.ENEMY, masks(GameMask.PLAYER),
                THUG_TUNING,
                () -> new Hurtable().despawnAfter(1.5f).onDeath(Drops::health),
                mind,
                true);
    }

    /** A boss: tagged for the level script, super armor against knockback, the body stays; the mind is the boss's own state machine. */
    public static Role boss(Mind mind) {
        return new Role("boss", GameTags.BOSS,
                GameMask.ENEMY, masks(GameMask.PICKUP),
                GameMask.ENEMY, masks(GameMask.PLAYER),
                null,
                () -> new Hurtable().knockback(0).recoverTime(0.5f).despawnAfter(2.5f),
                mind,
                true);
    }

    private static GameMask[] masks(GameMask... masks) { return masks; }
}
