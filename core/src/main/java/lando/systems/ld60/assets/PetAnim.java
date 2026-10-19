package lando.systems.ld60.assets;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.zendogames.zenlib.assets.AnimType;
import com.zendogames.zenlib.assets.Asset;
import com.zendogames.zenlib.assets.AtlasType;

public enum PetAnim implements AnimType {
    ASUKA("pets/asuka"),
    CHERRY("pets/cherry"),
    OSHA("pets/osha"),
    ;

    private final Asset<Animation<TextureRegion>> asset;
    private final String regionName;
    private final float frameDuration;
    private final Animation.PlayMode playMode;

    PetAnim(String regionName) {
        this(regionName, 0.1F, DEFAULT_PLAY_MODE);
    }

    PetAnim(String regionName, float frameDuration, Animation.PlayMode playMode) {
        this.asset = new Asset(this);
        this.regionName = regionName;
        this.frameDuration = frameDuration;
        this.playMode = playMode;
    }

    public AtlasType atlas() {
        return GameAtlas.SPRITES;
    }

    public String regionName() {
        return this.regionName;
    }

    public float frameDuration() {
        return this.frameDuration;
    }

    public Animation.PlayMode playMode() {
        return this.playMode;
    }

    public Asset<Animation<TextureRegion>> asset() {
        return this.asset;
    }

}
