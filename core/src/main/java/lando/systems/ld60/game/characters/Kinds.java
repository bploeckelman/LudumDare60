package lando.systems.ld60.game.characters;

import com.badlogic.gdx.utils.GdxRuntimeException;

import java.util.List;

/** Every kind of fighter, for the Tiled {@code enemy.kind} member and the debug spawners. */
public final class Kinds {

    private Kinds() {}

    public static final List<Kind> ALL = List.of(
              Awol.KIND
            , Bendy.kind(Bendy.BLACKBERRY)
            , Bendy.kind(Bendy.BRICKPHONE)
            , Bendy.kind(Bendy.CASSETTE)
            , Bendy.kind(Bendy.FOOTBALL)
            , Bendy.kind(Bendy.PROF)
            , Bendy.kind(Bendy.SPEAK)
            , N64.KIND
            , Spineboy.KIND
    );

    /** The kind named in Tiled, case-insensitive; throws listing the names so a typo fails at load. */
    public static Kind byName(String name) {
        for (var kind : ALL) {
            if (kind.name().equalsIgnoreCase(name)) return kind;
        }
        var names = new StringBuilder();
        for (var kind : ALL) names.append(names.isEmpty() ? "" : ", ").append(kind.name());
        throw new GdxRuntimeException("Kinds: no kind '%s', have: %s".formatted(name, names));
    }

    /** The role a Tiled {@code boss: true} of {@code kind} spawns in: its own state machine when it has one, else a thug's brain with a boss's armor. */
    public static Role bossRole(Kind kind) {
        return (kind == N64.KIND) ? N64.ROLE : Roles.boss(Roles.THUG.mind());
    }
}
