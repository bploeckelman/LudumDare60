package lando.systems.ld60.screens;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.MapObject;
import com.esotericsoftware.spine.SkeletonRendererDebug;
import com.zendogames.zenlib.Log;
import com.zendogames.zenlib.ZenGame;
import com.zendogames.zenlib.ecs.Family;
import com.zendogames.zenlib.ecs.components.SpineAnimator;
import com.zendogames.zenlib.ecs.components.Tilemap;
import com.zendogames.zenlib.render.WorldView;
import com.zendogames.zenlib.screens.transition.ScreenTransitionEffectType;
import com.zendogames.zenlib.tilemap.TiledProps;
import com.zendogames.zenlib.ui.DebugViews;
import lando.systems.ld60.assets.GameMap;
import lando.systems.ld60.game.Factory;

public class ExampleScroller extends ExampleScreen {

    private static final String TAG = ExampleScroller.class.getSimpleName();

    final Family skeletons;
    final SkeletonRendererDebug skeletonDebug;

    boolean debugRender = false;

    public ExampleScroller(ZenGame game) {
        super(game, GameMap.EXAMPLE_SCROLLER);

        this.skeletons = world.family(Family.all(SpineAnimator.TYPE));
        this.skeletonDebug = new SkeletonRendererDebug();
        this.skeletonDebug.setBoundingBoxes(false);
        this.skeletonDebug.setRegionAttachments(false);

        spawner.on(Factory.PLAYER, this::spawnPlayer);

        buildLevel();

        this.debugViews.add(DebugViews.spine(world));
        actions.add("toggle debug render", Input.Keys.B, () -> debugRender = !debugRender);
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

    private void spawnPlayer(Tilemap map, MapObject object) {
        int x = TiledProps.x(object);
        int y = TiledProps.y(object);
        var spineboy = factory.spineboyScroller(x, y);

        var anim = SpineAnimator.get(spineboy);
        anim.defaultScale.set(0.5f, 0.5f);
        anim.scale.set(0.5f, 0.5f);
        // TODO: gate this behind LOG_SPINE
        anim.onEvent(event -> Log.debug(TAG, "event: %s", event.getData().getName()));

        setPlayer(spineboy);
    }
}
