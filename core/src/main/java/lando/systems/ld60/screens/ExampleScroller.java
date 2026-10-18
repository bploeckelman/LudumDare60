package lando.systems.ld60.screens;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.MapObject;
import com.esotericsoftware.spine.SkeletonRendererDebug;
import com.zendogames.zenlib.Log;
import com.zendogames.zenlib.ZenGame;
import com.zendogames.zenlib.ecs.Family;
import com.zendogames.zenlib.ecs.components.Mover;
import com.zendogames.zenlib.ecs.components.Position;
import com.zendogames.zenlib.ecs.components.SpineAnimator;
import com.zendogames.zenlib.ecs.components.Tilemap;
import com.zendogames.zenlib.tilemap.TiledProps;
import com.zendogames.zenlib.ui.DebugViews;
import lando.systems.ld60.assets.EmitterType;
import lando.systems.ld60.assets.GameMap;
import lando.systems.ld60.game.Factory;
import lando.systems.ld60.game.GameStrikes;
import lando.systems.ld60.game.combat.HitboxDraw;
import lando.systems.ld60.game.movement.BrawlerMotion;
import lando.systems.ld60.game.systems.HitSystem;
import lando.systems.ld60.particles.effects.FootStepDustCloudEffect;

public class ExampleScroller extends ExampleScreen {

    private static final String TAG = ExampleScroller.class.getSimpleName();

    final Family skeletons;
    final SkeletonRendererDebug skeletonDebug;
    final HitSystem hitSystem;

    boolean debugRender = false;
    boolean showHitboxes = true;

    public ExampleScroller(ZenGame game) {
        super(game, GameMap.EXAMPLE_SCROLLER);

        this.skeletons = world.family(Family.all(SpineAnimator.TYPE));
        this.skeletonDebug = new SkeletonRendererDebug();
        this.skeletonDebug.setBoundingBoxes(false);
        this.skeletonDebug.setRegionAttachments(false);

        this.hitSystem = new HitSystem(new GameStrikes());
        world.addSystem(hitSystem);

        spawner .on(Factory.PLAYER, this::spawnPlayer)
                .on(Factory.WALL, (map, obj) -> factory.wall(obj));

        buildLevel();

        this.debugViews.add(DebugViews.spine(world));
        actions .arm("spawn dummy",         Input.Keys.N, (x, y) -> factory.dummy(x, y, (player != null && Position.get(player).x < x) ? -1 : 1))
                .add("toggle debug render", Input.Keys.B, () -> debugRender  = !debugRender)
                .add("toggle hitboxes",     Input.Keys.H, () -> showHitboxes = !showHitboxes)
        ;
    }

    @Override
    protected void renderWorld(Batch batch) {
        super.renderWorld(batch);

        if (showHitboxes) {
            batch.begin();
            HitboxDraw.all(shapes, hitSystem, world.alpha());
            batch.end();
        }

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

        factory.emitter(EmitterType.DUST, new FootStepDustCloudEffect.Params(spineboy));

        setPlayer(spineboy);
    }

    private void testHurt() {
        if (player == null) return;

        var anim = SpineAnimator.get(player);
        anim.play("jump");
        anim.scale.set(
                anim.defaultScale.x * 0.9f,
                anim.defaultScale.y * 1.1f);

        var mover = Mover.get(player);
        var motion = mover.motion(BrawlerMotion.class);
        motion.facing = 1;
        motion.launch(mover, -1, /*dist*/ 5f, /*height*/ 0.2f);
    }
}
