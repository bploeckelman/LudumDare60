package lando.systems.ld60.game;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.zendogames.zenlib.tilemap.TiledProps;

import java.util.Arrays;

/**
 * Parser of game-specific Tiled {@link MapObject} properties similar to {@link TiledProps} from the library.
 * Allows parsing game-specific Tiled properties in special ways without having to update the library code
 * for each new use.
 */
public class GameTiledProps {

    private GameTiledProps() {}

    /** A property of the tile a tile object was placed with, else {@code fallback}; non-tile objects return the fallback. */
    public static <T> T tileGet(MapObject object, String name, T fallback, Class<T> type) {
        if (!(object instanceof TiledMapTileMapObject tile)) return fallback;
        var value = tile.getTile().getProperties().get(name);
        return (value == null) ? fallback : cast(object, name, value, type);
    }

    /** Variant of {@link TiledProps#enumOf} which uses {@link #tileGet} to lookup the property from a tile rather than a class property. */
    public static <E extends Enum<E>> E tileEnumOf(MapObject object, String name, E fallback, Class<E> type) {
        var value = tileGet(object, name, fallback.name(), String.class);
        try {
            return Enum.valueOf(type, value.trim().toUpperCase());
        } catch (IllegalArgumentException var5) {
            throw new GdxRuntimeException("property '%s' on map object %s is '%s', expected one of %s"
                    .formatted(name, TiledProps.describe(object), value, Arrays.toString(type.getEnumConstants())));
        }
    }

    /** Copy of {@code TiledProps#cast}, which is currently private. */
    private static <T> T cast(MapObject object, String name, Object value, Class<T> type) {
        if (!type.isInstance(value)) {
            throw new GdxRuntimeException("property '%s' on map object %s is a %s, not a %s (check its type in Tiled)"
                    .formatted(name, TiledProps.describe(object), value.getClass().getSimpleName(), type.getSimpleName()));
        } else {
            return type.cast(value);
        }
    }
}
