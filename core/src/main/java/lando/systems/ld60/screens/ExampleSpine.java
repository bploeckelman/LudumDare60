package lando.systems.ld60.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.esotericsoftware.spine.*;
import com.esotericsoftware.spine.utils.TwoColorPolygonBatch;
import com.zendogames.zenlib.ZenGame;
import com.zendogames.zenlib.screens.transition.ScreenTransitionEffectType;

public class ExampleSpine extends BaseScreen {

    TwoColorPolygonBatch polyBatch;
    SkeletonRenderer renderer;
    SkeletonRendererDebug debugRenderer;

    TextureAtlas atlas;
    Skeleton skeleton;
    AnimationState state;

    boolean debugRender = false;

    public ExampleSpine(ZenGame game) {
        super(game, /*withCollision*/ false);

        this.polyBatch = new TwoColorPolygonBatch();
        this.renderer = new SkeletonRenderer();
        this.renderer.setPremultipliedAlpha(true); // PMA results in correct blending without outlines
        this.debugRenderer = new SkeletonRendererDebug();
        this.debugRenderer.setBoundingBoxes(false);
        this.debugRenderer.setRegionAttachments(false);

        this.atlas = new TextureAtlas(Gdx.files.internal("spine/spineboy/spineboy-pma.atlas"));
        var json = new SkeletonBinary(atlas); // This loads skeleton JSON data, which is stateless.
        json.setScale(0.5f); // Load the skeleton at 50% the size it was in Spine.
        var skeletonData = json.readSkeletonData(Gdx.files.internal("spine/spineboy/spineboy-pro.skel"));

        this.skeleton = new Skeleton(skeletonData); // Skeleton holds skeleton state (bone positions, slot attachments, etc).
        this.skeleton.setPosition(0, -300);

        var stateData = new AnimationStateData(skeletonData); // Defines mixing (crossfading) between animations>
        stateData.setMix("run", "jump", 0.2f);
        stateData.setMix("jump", "run", 0.2f);

        this.state = new AnimationState(stateData); // Holds the animation state for a skeleton (current animation, time, etc).
        this.state.setTimeScale(0.5f); // Slow all animations down to 50% speed.

        // Queue animations on track 0.
        this.state.setAnimation(0, "run", true);
        this.state.addAnimation(0, "jump", false, 2); // Jump after 2 seconds.
        this.state.addAnimation(0, "run", true, 0); // Run after the jump.

        actions .add("back to menu",  Input.Keys.ESCAPE, () -> game.setScreen(new IntroScreen(game), ScreenTransitionEffectType.DOORWAY))
                .add("toggle debug render", Input.Keys.D, () -> debugRender = !debugRender);
    }

    @Override
    public void update(float dt) {
        state.update(dt);
        state.apply(skeleton); // Poses skeleton using current animations. This sets the bones' local SRT.
        skeleton.update(dt); // Advance the skeleton time. This is needed when the skeleton has physics.
        skeleton.updateWorldTransform(Physics.update); // Uses the bones' local SRT to compute their world SRT.

        super.update(dt);
    }

    @Override
    protected void renderWorld(SpriteBatch batch) {
        super.renderWorld(batch);

        polyBatch.getProjectionMatrix().set(worldCamera.combined);
        debugRenderer.getShapeRenderer().setProjectionMatrix(worldCamera.combined);

        polyBatch.begin();
        renderer.draw(polyBatch, skeleton); // Draw the skeleton images.
        polyBatch.end();

        if (debugRender) {
            debugRenderer.draw(skeleton); // Draw debug lines
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        atlas.dispose();
        polyBatch.dispose();
    }
}
