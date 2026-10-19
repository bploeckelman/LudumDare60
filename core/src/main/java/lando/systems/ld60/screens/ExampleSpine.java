package lando.systems.ld60.screens;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.esotericsoftware.spine.SkeletonRendererDebug;
import com.zendogames.zenlib.Log;
import com.zendogames.zenlib.ZenGame;
import com.zendogames.zenlib.ecs.Entity;
import com.zendogames.zenlib.ecs.Family;
import com.zendogames.zenlib.ecs.components.SpineAnimator;
import com.zendogames.zenlib.screens.transition.ScreenTransitionEffectType;
import com.zendogames.zenlib.ui.DebugViews;
import lando.systems.ld60.game.FactoryExample;

/**
 * Spine through the ECS: spineboy is an entity with a {@code SpineAnimator}, advanced by the
 * {@code SpineSystem} and drawn by the {@code RenderSystem} in z-order like any other renderable.
 */
public class ExampleSpine extends BaseScreen {

    private static final String TAG = ExampleSpine.class.getSimpleName();

    final Family skeletons;
    final SkeletonRendererDebug skeletonDebug;
    final FactoryExample factoryExample;

    Entity spineboy;
    boolean debugRender = false;

    public ExampleSpine(ZenGame game) {
        super(game, /*withCollision*/ false);

        this.skeletons = world.family(Family.all(SpineAnimator.TYPE));
        this.skeletonDebug = new SkeletonRendererDebug();
        this.skeletonDebug.setBoundingBoxes(false);
        this.skeletonDebug.setRegionAttachments(false);
        this.factoryExample = new FactoryExample(world, pixel);

        this.debugViews.add(DebugViews.spine(world));
        addCommonSystems();

        int x = (int) worldCamera.viewportWidth  / 2;
        int y = (int) worldCamera.viewportHeight / 2 - 300;
        this.spineboy = factoryExample.spineboy(x, y);
        var anim = SpineAnimator.get(spineboy);
        anim.state.setTimeScale(0.5f);          // slow all animations to 50% speed
        anim.queue("jump", false, 2);           // jump after 2 seconds...
        anim.queue("run", true, 0);             // ...then back to running
        anim.onEvent(event -> Log.info(TAG, "event: %s", event.getData().getName()));

        actions .add("back to menu",        Input.Keys.ESCAPE, () -> game.setScreen(new IntroScreen(game), ScreenTransitionEffectType.DOORWAY))
                .add("jump",                Input.Keys.SPACE,  () -> { anim.start("jump", false); anim.queue("run", true, 0); })
                .add("turn around",         Input.Keys.F,      () -> anim.facing = -anim.facing)
                .add("squash",              Input.Keys.S,      () -> anim.scale.set(1.3f, 0.7f))
                .add("toggle debug render", Input.Keys.D,      () -> debugRender = !debugRender);
    }

    @Override
    protected void renderWorld(Batch batch) {
        super.renderWorld(batch);

        if (debugRender) {
            // The skeletons were just posed at their render positions, so the bones line up with the draw
            skeletonDebug.getShapeRenderer().setProjectionMatrix(worldCamera.combined);
            var entities = skeletons.entities();
            for (int i = 0; i < entities.size; i++) {
                var e = entities.get(i);
                var spineAnim = SpineAnimator.get(e);
                skeletonDebug.draw(spineAnim.skeleton);
            }
        }
    }
}
