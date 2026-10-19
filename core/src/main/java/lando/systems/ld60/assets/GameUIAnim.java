package lando.systems.ld60.assets;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.zendogames.zenlib.assets.AnimType;
import com.zendogames.zenlib.assets.Asset;
import com.zendogames.zenlib.assets.AtlasType;

public enum GameUIAnim implements AnimType {
    GEAR ("gear")
    , SETTINGS ("ui/settings-modal")
    , CONTROLS ("ui/controls-modal")
    , DIALOG ("ui/dialog-box")
    , GO_ARROW("ui/go")
    , KEY_W ("kenny-input/keyboard-w")
    , KEY_S ("kenny-input/keyboard-s")
    , KEY_D ("kenny-input/keyboard-d")
    , KEY_A ("kenny-input/keyboard-a")
    , KEY_ENTER ("kenny-input/keyboard-enter")
    , KEY_SPACE ("kenny-input/keyboard-space")
    , KEY_SHIFT ("kenny-input/keyboard-shift")
    , KEY_ESCAPE ("kenny-input/keyboard-escape")
    , KEY_WASD ("kenny-input/keyboard-wasd")
    , PAD_X("kenny-input/xbox-button-x")
    , PAD_Y("kenny-input/xbox-button-y")
    , PAD_A("kenny-input/xbox-button-a")
    , PAD_B("kenny-input/xbox-button-b")
    , PAD_START("kenny-input/xbox-button-start")
    , PAD_DOWN("kenny-input/xbox-dpad-down")
    , PAD_UP("kenny-input/xbox-dpad-up")
    , PAD_LEFT("kenny-input/xbox-dpad-left")
    , PAD_RIGHT("kenny-input/xbox-dpad-right")
    , PAD_DPAD("kenny-input/xbox-dpad")
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
