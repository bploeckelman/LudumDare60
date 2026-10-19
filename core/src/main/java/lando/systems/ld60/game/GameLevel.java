package lando.systems.ld60.game;

import lando.systems.ld60.assets.GameMap;
import lando.systems.ld60.assets.MusicType;

/**
 * The game's run: three themed streets, each ending in a miniboss, then the arena. A level is a
 * map, a tune and a title card; {@code GameScreen} plays one and moves to {@link #next()} when it
 * is cleared. Maps and themes are placeholders until the {@code 01-} maps exist.
 */
public enum GameLevel {
    LEVEL_1 ("Gamer's Paradise",     GameMap.LEVEL_1, MusicType.MAIN_THEME),
    LEVEL_2 ("The Server Room",   GameMap.LEVEL_2, MusicType.CHIPTUNE_THEME),
    LEVEL_3 ("The Toy Box",        GameMap.LEVEL_3, MusicType.JAZZY_THEME),
    FINALE  ("The Mainframe",     GameMap.LEVEL_1, MusicType.MAIN_THEME);

    public final String title;
    public final GameMap map;
    public final MusicType music;

    GameLevel(String title, GameMap map, MusicType music) {
        this.title = title;
        this.map   = map;
        this.music = music;
    }

    public boolean isLast() { return ordinal() == values().length - 1; }

    /** The level after this one; the last level repeats. */
    public GameLevel next() { return isLast() ? this : values()[ordinal() + 1]; }
}
