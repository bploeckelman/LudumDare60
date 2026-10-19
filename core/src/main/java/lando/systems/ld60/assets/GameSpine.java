package lando.systems.ld60.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.spine.Animation;
import com.esotericsoftware.spine.AnimationStateData;
import com.zendogames.zenlib.assets.Asset;
import com.zendogames.zenlib.assets.SpineData;
import com.zendogames.zenlib.assets.SpineType;
import com.zendogames.zenlib.ecs.components.SpineAnimator;

/**
 * Spine skeletons the game loads: the skeleton file, its atlas, and the load-time scale.
 * <p>
 * Two scales compose. {@link #scale()} is baked into the skeleton data by the loader (bones,
 * attachments, {@code SpineAnimator.size}), so it should match the atlas pack scale for a
 * 1:1 texel mapping (the spineboy atlas is packed at 0.5). A per-entity size on top of that is
 * {@code SpineAnimator.defaultScale}; {@code SpineAnimator.scale} is only the squash/stretch
 * channel and eases back to {@code defaultScale} every tick, so setting it alone does not stick.
 * {@code SpineAnimator.size} is the setup-pose bounds from the export and is read, never set.
 * <p>
 * {@link #mixes} sets per-pair crossfade times on the shared {@code AnimationStateData}.
 */
public enum GameSpine implements SpineType {
    SPINEBOY ("spine/spineboy/spineboy-pro.skel", "spine/spineboy/0.25/spineboy-pro.atlas", 0.25f) {
        @Override public void mixes(AnimationStateData mixes) {
            mixes.setMix("run", "jump", 0.2f);
            mixes.setMix("jump", "run", 0.2f);

            // The 'aim' clip shows the editor's crosshair sprite at its IK target; drop t from the skin so the clip finds nothing
            var data = mixes.getSkeletonData();
            data.getDefaultSkin().removeAttachment(data.findSlot("crosshair").getIndex(), "crosshair");

            // The 'jump' clip carries its own 90 px leap (hip and IK-target translations);
            // BrawlerMotion.z owns the height, keep only the pose
            var jump = data.findAnimation("jump");
            var risers = Array.with("hip", "front-foot-target", "rear-foot-target", "front-leg-target", "rear-leg-target");
            var timelines = jump.getTimelines();
            for (int i = timelines.size - 1; i >= 0; i--) {
                if (timelines.get(i) instanceof Animation.TranslateTimeline tl
                 && risers.contains(data.getBones().get(tl.getBoneIndex()).getName(), false)) {
                    timelines.removeIndex(i);
                }
            }
            jump.setTimelines(timelines, jump.getBones()); // recomputes the property ids the mixer checks
        }
    },
    AWOL ("spine/awol/awol.skel", "spine/awol/awol.atlas", 1f) {
        @Override public void mixes(AnimationStateData mixes) {
            mixes.setMix("run", "jump", 0.2f);
            mixes.setMix("jump", "run", 0.2f);
            mixes.setMix("run", "punch", 0.05f);
            mixes.setMix("run", "kick",  0.05f);
            mixes.setMix("run", "swing", 0.05f);
        }
    },
    FLOPPYD ("spine/floppyd/floppyd.skel", "spine/floppyd/floppyd.atlas", 1f) {
        @Override public void mixes(AnimationStateData mixes) {
            mixes.setMix("run", "jump", 0.2f);
            mixes.setMix("jump", "run", 0.2f);
            mixes.setMix("run", "punch", 0.05f);
            mixes.setMix("run", "kick",  0.05f);
            mixes.setMix("run", "swing", 0.05f);
        }
    },
    BLACKBERRY ("spine/blackberry/blackberry.skel", "spine/blackberry/blackberry.atlas", 1f) {
        @Override public void mixes(AnimationStateData mixes) {
            mixes.setMix("run", "jump", 0.2f);
            mixes.setMix("jump", "run", 0.2f);
            mixes.setMix("run", "punch", 0.05f);
            mixes.setMix("run", "kick",  0.05f);
            mixes.setMix("run", "swing", 0.05f);
        }
    },
    BRICKPHONE ("spine/brickphone/brickphone.skel", "spine/brickphone/brickphone.atlas", 1f) {
        @Override public void mixes(AnimationStateData mixes) {
            mixes.setMix("run", "jump", 0.2f);
            mixes.setMix("jump", "run", 0.2f);
            mixes.setMix("run", "punch", 0.05f);
            mixes.setMix("run", "kick",  0.05f);
            mixes.setMix("run", "swing", 0.05f);
        }
    },
    CASSETTE ("spine/cassette/cassette.skel", "spine/cassette/cassette.atlas", 1f) {
        @Override public void mixes(AnimationStateData mixes) {
            mixes.setMix("run", "jump", 0.2f);
            mixes.setMix("jump", "run", 0.2f);
            mixes.setMix("run", "punch", 0.05f);
            mixes.setMix("run", "kick",  0.05f);
            mixes.setMix("run", "swing", 0.05f);
        }
    },
    FOOTBALL ("spine/football/football.skel", "spine/football/football.atlas", 1f) {
        @Override public void mixes(AnimationStateData mixes) {
            mixes.setMix("run", "jump", 0.2f);
            mixes.setMix("jump", "run", 0.2f);
            mixes.setMix("run", "punch", 0.05f);
            mixes.setMix("run", "kick",  0.05f);
            mixes.setMix("run", "swing", 0.05f);
        }
    },
    PROF ("spine/prof/prof.skel", "spine/prof/prof.atlas", 1f) {
        @Override public void mixes(AnimationStateData mixes) {
            mixes.setMix("run", "jump", 0.2f);
            mixes.setMix("jump", "run", 0.2f);
            mixes.setMix("run", "punch", 0.05f);
            mixes.setMix("run", "kick",  0.05f);
            mixes.setMix("run", "swing", 0.05f);
        }
    },
    SPEAK ("spine/speak/speak.skel", "spine/speak/speak.atlas", 1f) {
        @Override public void mixes(AnimationStateData mixes) {
            mixes.setMix("run", "jump", 0.2f);
            mixes.setMix("jump", "run", 0.2f);
            mixes.setMix("run", "punch", 0.05f);
            mixes.setMix("run", "kick",  0.05f);
            mixes.setMix("run", "swing", 0.05f);
        }
    }
    ;

    /** Where spineboy's 'aim' IK points the gun: the crosshair bone's position from the feet, in skeleton units (x is always ahead, facing flips the skeleton. */
    static final float AIM_X = 300, AIM_Y = 85;

    /**
     * Levels spineboy's gun for a shot: "aim" on track 1 for half a second so the gun points at the
     * crosshair instead of the "shoot" clip's pointing-down rest pose (the clip's recoil key gives
     * way to the aim pose), then the track empties. The {@code Move} that plays "shoot" calls this.
     */
    public static void aim(SpineAnimator anim) {
        anim.skeleton.findBone("crosshair").getPose().setPosition(AIM_X, AIM_Y);
        anim.start(1, "aim", true);
        anim.state.addEmptyAnimation(1, /*mix*/ 0.2f, /*delay*/ 0.5f);
    }

    private final Asset<SpineData> asset = new Asset<>(this);
    private final String path;
    private final String atlasPath;
    private final float scale;

    GameSpine(String path, String atlasPath) {
        this(path, atlasPath, /*scale*/ 1f);
    }

    GameSpine(String path, String atlasPath, float scale) {
        this.path = path;
        this.atlasPath = atlasPath;
        this.scale = scale;
    }

    @Override public Asset<SpineData> asset() { return asset; }
    @Override public FileHandle file()        { return Gdx.files.internal(path); }
    @Override public FileHandle atlasFile()   { return Gdx.files.internal(atlasPath); }
    @Override public float scale()            { return scale; }
    @Override public float defaultMix()       { return 0.1f; }
}
