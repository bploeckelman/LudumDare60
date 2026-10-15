package lando.systems.ld60.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.zendogames.zenlib.assets.Asset;
import com.zendogames.zenlib.assets.AtlasType;

public enum SpriteAtlas implements AtlasType {
    SPRITES("sprites/sprites.atlas");

    private final Asset<TextureAtlas> asset;
    private final String path;

    SpriteAtlas(String path) {
        this.path = path;
        this.asset = new Asset<>(this);
    }

    @Override public Asset<TextureAtlas> asset() { return asset; }
    @Override public FileHandle file() { return Gdx.files.internal(path); }
}
