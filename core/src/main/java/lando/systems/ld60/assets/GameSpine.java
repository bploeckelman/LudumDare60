package lando.systems.ld60.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.esotericsoftware.spine.AnimationStateData;
import com.zendogames.zenlib.assets.Asset;
import com.zendogames.zenlib.assets.SpineData;
import com.zendogames.zenlib.assets.SpineType;

public enum GameSpine implements SpineType {
      SPINEBOY ("spine/spineboy/spineboy-pro.skel", "spine/spineboy/spineboy-pma.atlas", 0.5f) {
          @Override public void mixes(AnimationStateData mixes) {
              mixes.setMix("run", "jump", 0.2f);
              mixes.setMix("jump", "run", 0.2f);
          }
      }
    ;

    private final Asset<SpineData> asset = new Asset<>(this);
    private final String path;
    private final String atlasPath;
    private final float scale;

    GameSpine(String path, String atlasPath, float scale) {
        this.path = path;
        this.atlasPath = atlasPath;
        this.scale = scale;
    }

    @Override public Asset<SpineData> asset() { return asset; }
    @Override public FileHandle file()        { return Gdx.files.internal(path); }
    @Override public FileHandle atlasFile()   { return Gdx.files.internal(atlasPath); }
    @Override public float scale()            { return scale; }
}
