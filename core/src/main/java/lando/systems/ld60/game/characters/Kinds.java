package lando.systems.ld60.game.characters;

import com.badlogic.gdx.utils.GdxRuntimeException;

import java.util.List;

/** Every kind of fighter, for the Tiled {@code enemy.kind} member and the debug spawners. */
public final class Kinds {

    private Kinds() {}

    public static final List<Kind> ALL = List.of(Spineboy.KIND, Awol.KIND);

    /** The kind named in Tiled, case-insensitive; throws listing the names so a typo fails at load. */
    public static Kind byName(String name) {
        for (var kind : ALL) {
            if (kind.name().equalsIgnoreCase(name)) return kind;
        }
        var names = new StringBuilder();
        for (var kind : ALL) names.append(names.isEmpty() ? "" : ", ").append(kind.name());
        throw new GdxRuntimeException("Kinds: no kind '%s', have: %s".formatted(name, names));
    }
}
