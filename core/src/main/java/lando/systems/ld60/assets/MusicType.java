package lando.systems.ld60.assets;

import com.badlogic.gdx.audio.Music;
import com.zendogames.zenlib.assets.Asset;
import com.zendogames.zenlib.assets.AssetType;
import com.zendogames.zenlib.assets.Assets;

public enum MusicType implements AssetType<Music> {
    MAIN_THEME("main1.ogg"),
    TITLE_THEME("main1.ogg");

    private final Asset<Music> asset;
    private final String path;

    MusicType(String filename) {
        this.path = "audio/music/" + filename;
        this.asset = new Asset<>(this);
    }

    @Override
    public Asset<Music> asset() {
        return asset;
    }

    @Override
    public void load(Assets assets) {
        assets.mgr.load(path, Music.class);
    }

    @Override
    public Music init(Assets assets) {
        return assets.mgr.get(path, Music.class);
    }
}
