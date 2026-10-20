package lando.systems.ld60.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.zendogames.zenlib.assets.Asset;
import com.zendogames.zenlib.assets.MapType;

public enum GameMap implements MapType {
      LEVEL_1            ("maps/01-level.tmx")
    ,  LEVEL_2            ("maps/02-level.tmx")
    ,  LEVEL_3            ("maps/03-level.tmx")
    , EXAMPLE_PLATFORMER ("maps/00-example-platformer.tmx")
    , EXAMPLE_TOP_DOWN   ("maps/00-example-top-down.tmx")
//    , EXAMPLE_SCROLLER   ("maps/00-example-tmnt.tmx")
    ;

    /** Class property defaults come from here; every map shares the one project. */
    private static final String PROJECT = "maps/ld60.tiled-project";

    private final Asset<TiledMap> asset = new Asset<>(this);
    private final String path;

    GameMap(String path) {
        this.path = path;
    }

    @Override public Asset<TiledMap> asset() { return asset; }
    @Override public FileHandle file()       { return Gdx.files.internal(path); }
    @Override public String projectFile()    { return PROJECT; }
}
