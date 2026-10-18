package lando.systems.ld60.assets;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.zendogames.zenlib.assets.AnimType;
import com.zendogames.zenlib.assets.Asset;
import com.zendogames.zenlib.assets.AtlasType;
import com.zendogames.zenlib.assets.ZenAtlas;

public enum GameUIAnim implements AnimType {
    GEAR ("gear")
    , SETTINGS ("ui/settings-modal")
    ;

    private final Asset<Animation<TextureRegion>> asset;
    private final String regionName;
    private final float frameDuration;
    private final Animation.PlayMode playMode;

    GameUIAnim(String regionName) {
        this(regionName, DEFAULT_FRAME_DURATION, DEFAULT_PLAY_MODE);
    }

    GameUIAnim(String regionName, float frameDuration, Animation.PlayMode playMode) {
        this.asset = new Asset<>(this);
        this.regionName = regionName;
        this.frameDuration = frameDuration;
        this.playMode = playMode;
    }

    @Override public AtlasType atlas()     { return GameAtlas.SPRITES; }
    @Override public String regionName()   { return regionName; }
    @Override public float frameDuration() { return frameDuration; }
    @Override public Animation.PlayMode playMode()   { return playMode; }
    @Override public Asset<Animation<TextureRegion>> asset() { return asset; }
}
