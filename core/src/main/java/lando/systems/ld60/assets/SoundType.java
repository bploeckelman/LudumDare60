package lando.systems.ld60.assets;

import com.badlogic.gdx.audio.Sound;
import com.zendogames.zenlib.assets.Asset;
import com.zendogames.zenlib.assets.AssetType;
import com.zendogames.zenlib.assets.Assets;

public enum SoundType implements AssetType<Sound> {
    BELL("blip1.ogg", 1.0f),
    BELL_LOW("blip1.ogg", 0.8f),
    BELL_HIGH("blip1.ogg", 1.2f);

    private final Asset<Sound> asset;
    private final String path;
    public final float pitch;

    SoundType(String filename, float pitch) {
        this.path = "audio/sounds/" + filename;
        this.pitch = pitch;
        this.asset = new Asset<>(this);
    }

    SoundType(String filename) {
        this(filename, 1f);
    }

    @Override
    public Asset<Sound> asset() {
        return asset;
    }

    @Override
    public void load(Assets assets) {
        assets.mgr.load(path, Sound.class);
    }

    @Override
    public Sound init(Assets assets) {
        return assets.mgr.get(path, Sound.class);
    }

    // ========================================================================
    // Tightly Coupled Sound Groups
    // ========================================================================

    public static class WeightedSound {
        public final SoundType sound;
        public final float weight;

        public WeightedSound(SoundType sound, float weight) {
            this.sound = sound;
            this.weight = weight;
        }
    }

    public enum SoundGroup {
        BELLS(
            new WeightedSound(BELL, 70f),
            new WeightedSound(BELL_HIGH, 20f),
            new WeightedSound(BELL_LOW, 10f)
        );

        public final WeightedSound[] sounds;
        public final float totalWeight;

        SoundGroup(WeightedSound... sounds) {
            this.sounds = sounds;
            float sum = 0f;
            for (WeightedSound ws : sounds) {
                sum += ws.weight;
            }
            this.totalWeight = sum;
        }
    }
}
