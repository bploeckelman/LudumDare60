package lando.systems.ld60.assets;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.zendogames.zenlib.assets.AnimType;
import com.zendogames.zenlib.assets.Asset;
import com.zendogames.zenlib.assets.AtlasType;

public enum AnimAtari implements AnimType {
      ATTACK ("characters/atari-joystick/atari-joystick-attack/atari-joystick-attack", DEFAULT_FRAME_DURATION, Animation.PlayMode.NORMAL)
    , IDLE   ("characters/atari-joystick/atari-joystick-idle/atari-joystick-idle")
    , SPIN   ("characters/atari-joystick/atari-joystick-spin/atari-joystick-spin",     DEFAULT_FRAME_DURATION, Animation.PlayMode.NORMAL)
    , WALK   ("characters/atari-joystick/atari-joystick-walk/atari-joystick-walk")
    ; // the two moves hold their last frame (a Move ends on it); idle and walk loop

    private final Asset<Animation<TextureRegion>> asset;
    private final String regionName;
    private final float frameDuration;
    private final Animation.PlayMode playMode;

    AnimAtari(String regionName) {
        this(regionName, DEFAULT_FRAME_DURATION, DEFAULT_PLAY_MODE);
    }

    AnimAtari(String regionName, float frameDuration, Animation.PlayMode playMode) {
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
