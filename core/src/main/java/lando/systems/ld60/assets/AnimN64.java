package lando.systems.ld60.assets;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.zendogames.zenlib.assets.AnimType;
import com.zendogames.zenlib.assets.Asset;
import com.zendogames.zenlib.assets.AtlasType;

public enum AnimN64 implements AnimType {
      ARMED      ("characters/n64/n64-armed/n64-armed")
    , HURT       ("characters/n64/n64-hurt/n64-hurt",             DEFAULT_FRAME_DURATION, Animation.PlayMode.NORMAL)
    , IDLE       ("characters/n64/n64-idle/n64-idle")
    , PROJECTILE ("characters/n64/n64-projectile/n64-projectile", DEFAULT_FRAME_DURATION, Animation.PlayMode.NORMAL)
    , PUNCH      ("characters/n64/n64-punch/n64-punch",           DEFAULT_FRAME_DURATION, Animation.PlayMode.NORMAL)
    , SHOOT      ("characters/n64/n64-shoot/n64-shoot",           DEFAULT_FRAME_DURATION, Animation.PlayMode.NORMAL)
    , SPIN       ("characters/n64/n64-spin/n64-spin",             DEFAULT_FRAME_DURATION, Animation.PlayMode.NORMAL)
    ; // one-shot clips hold their last frame (a Move ends on it; the dead

    private final Asset<Animation<TextureRegion>> asset;
    private final String regionName;
    private final float frameDuration;
    private final Animation.PlayMode playMode;

    AnimN64(String regionName) {
        this(regionName, DEFAULT_FRAME_DURATION, DEFAULT_PLAY_MODE);
    }

    AnimN64(String regionName, float frameDuration, Animation.PlayMode playMode) {
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
