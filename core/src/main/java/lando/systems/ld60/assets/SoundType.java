package lando.systems.ld60.assets;

import com.badlogic.gdx.audio.Sound;
import com.zendogames.zenlib.assets.Asset;
import com.zendogames.zenlib.assets.AssetType;
import com.zendogames.zenlib.assets.Assets;

public enum SoundType implements AssetType<Sound> {
    BELL("blip1.ogg", 1.0f)
    , BELL_LOW("blip1.ogg", 0.8f)
    , BELL_HIGH("blip1.ogg", 1.2f)
    , FOOTSTEP1("footstep1.ogg", 1f)
    , FOOTSTEP2("footstep2.ogg", 1f)
    , FOOTSTEP3("footstep3.ogg", 1f)
    , FOOTSTEP4("footstep4.ogg", 1f)
    , FOOTSTEP5("footstep5.ogg", 1f)
    , FOOTSTEP6("footstep6.ogg", 1f)
    , GRUNT1("grunt1.ogg", 1f)
    , GRUNT2("grunt2.ogg", 1f)
    , GRUNT3("grunt3.ogg", 1f)
    , GRUNT4("grunt4.ogg", 1f)
    , GRUNT5("grunt5.ogg", 1f)
    , GRUNT6("grunt6.ogg", 1f)
    , GRUNT7("grunt7.ogg", 1f)
    , GRUNT8("grunt8.ogg", 1f)
    , GRUNT9("grunt9.ogg", 1f)
    , SWING1("swing1.ogg", 1f)
    , DEGAUSS("degauss.ogg", 1f)
    , YES("yes.ogg", 1f)
    , ONWARD("onward.ogg", 1f)
    , LETSGO("letsgo.ogg", 1f)
    , DO_IT("doit.ogg", 1f)
    , FIGHT_MORE("fight_more.ogg", 1f)
    , DAMN_SON("damnson1.ogg", 1f)
    , DAMN("damn1.ogg", 1f)
    , COOKING("cooking.ogg", 1f)
    , NICE("nice.ogg", 1f)
    , OW1("ow1.ogg", 1f)
    , OW2("ow2.ogg", 1f)
    , OW3("ow3.ogg", 1f)
    , IMPACT1("impact1.ogg", 1f)
    , IMPACT2("impact2.ogg", 1f)
    , CHORD("chord.ogg", 1f)
    , CHIMES("chimes.ogg", 1f)
    , TADA("tada.ogg", 1f)
    , DING("ding.ogg", 1f)
    , WHOOSH("whoosh.ogg", 1f)
    , POP("pop.ogg", 1f)
    , THUNK1("thunk1.ogg", 1f)
    , THUNK2("thunk2.ogg", 1f)
    , THUNK3("thunk3.ogg", 1f)
    , LOWTHUNK1("lowthunk1.ogg", 1f)
    , LOWTHUNK2("lowthunk2.ogg", 1f)
    , THUNKDING1("thunkding1.ogg", 1f)
    , CLANG1("clang1.ogg", 1f)
    , CLANG2("clang2.ogg", 1f)
    , CLANG3("clang3.ogg", 1f)
    , POCK1("pock1.ogg", 1f)
    , POCK2("pock2.ogg", 1f)
    , BONG1("bong1.ogg", 1f)
    , THWACK("thwack.ogg", 1f)
    , BANG1("bang1.ogg", 1f)
    , BANG2("bang2.ogg", 1f)
    , WHACK1("whack1.ogg", 1f)
    ;

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
        )
        ,DEGAUSS(
            new WeightedSound(SoundType.DEGAUSS, 100f)
//            new WeightedSound(BELL_HIGH, 20f),
//            new WeightedSound(BELL_LOW, 10f)
        )
        ,FOOTSTEPS(
            new WeightedSound(SoundType.FOOTSTEP1, 16f)
            ,new WeightedSound(FOOTSTEP2, 16f)
            ,new WeightedSound(FOOTSTEP3, 16f)
            ,new WeightedSound(FOOTSTEP4, 16f)
            ,new WeightedSound(FOOTSTEP5, 16f)
//            ,new WeightedSound(FOOTSTEP6, 17f) // this has like all steps in it, makes an echo
//            new WeightedSound(BELL_LOW, 10f)
        )
        ,GRUNTS(
            new WeightedSound(SoundType.GRUNT1, 14f)
            ,new WeightedSound(GRUNT2, 14f)
            ,new WeightedSound(GRUNT3, 14f)
            ,new WeightedSound(GRUNT4, 16f)
            ,new WeightedSound(GRUNT5, 14f)
            ,new WeightedSound(GRUNT6, 14f)
            ,new WeightedSound(GRUNT7, 14f)
//            new WeightedSound(BELL_LOW, 10f)
        )
        ,CLANGS(
            new WeightedSound(SoundType.CLANG1, 33f)
            ,new WeightedSound(SoundType.CLANG2, 33f)
            ,new WeightedSound(SoundType.CLANG3, 34f)
        )
        ,POCKS(
            new WeightedSound(SoundType.POCK1, 50f)
            ,new WeightedSound(SoundType.POCK2, 50f)
        )
        ;

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
