package lando.systems.ld60.assets;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.zendogames.zenlib.assets.AnimType;
import com.zendogames.zenlib.assets.Asset;
import com.zendogames.zenlib.assets.AtlasType;

public enum ParticleAnim implements AnimType {
    CIRCLE("kenney-particles/circle"),
    DIRT("kenney-particles/dirt"),
    FIRE("kenney-particles/fire"),
    FLAME("kenney-particles/flame"),
    FLARE("kenney-particles/flare"),
    LIGHT("kenney-particles/light"),
    MAGIC("kenney-particles/magic"),
    MUZZLE("kenney-particles/muzzle"),
    SCORCH("kenney-particles/scorch"),
    SLASH("kenney-particles/slash"),
    SMOKE("kenney-particles/smoke"),
    SPARK("kenney-particles/spark"),
    STAR("kenney-particles/star"),
    SYMBOL("kenney-particles/symbol"),
    TRACE("kenney-particles/trace"),
    TWIRL("kenney-particles/twirl"),
    WINDOW("kenney-particles/window"),
    SPLAT("kenney-splats/splat"),
    ;

    private final Asset<Animation<TextureRegion>> asset;
    private final String regionName;
    private final float frameDuration;
    private final Animation.PlayMode playMode;

    ParticleAnim(String regionName) {
        this(regionName, 0.1F, DEFAULT_PLAY_MODE);
    }

    ParticleAnim(String regionName, float frameDuration, Animation.PlayMode playMode) {
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
