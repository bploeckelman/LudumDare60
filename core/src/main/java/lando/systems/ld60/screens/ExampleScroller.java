package lando.systems.ld60.screens;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.MapObject;
import com.esotericsoftware.spine.SkeletonRendererDebug;
import com.zendogames.zenlib.Log;
import com.zendogames.zenlib.ZenGame;
import com.zendogames.zenlib.collision.ContactResponder;
import com.zendogames.zenlib.ecs.Family;
import com.zendogames.zenlib.ecs.components.Position;
import com.zendogames.zenlib.ecs.components.SpineAnimator;
import com.zendogames.zenlib.ecs.components.Tilemap;
import com.zendogames.zenlib.render.RenderLayer;
import com.zendogames.zenlib.tilemap.TiledProps;
import com.zendogames.zenlib.ui.DebugViews;
import lando.systems.ld60.assets.EmitterType;
import lando.systems.ld60.assets.GameMap;
import lando.systems.ld60.events.CombatEvent;
import lando.systems.ld60.events.Signal;
import lando.systems.ld60.events.SignalListener;
import lando.systems.ld60.game.*;
import lando.systems.ld60.game.combat.HitboxDraw;
import lando.systems.ld60.game.systems.BrawlerPresentSystem;
import lando.systems.ld60.game.systems.HealthBarSystem;
import lando.systems.ld60.game.systems.HitSystem;
import lando.systems.ld60.particles.effects.BloodSplatterEffect;
import lando.systems.ld60.particles.effects.CloneEffect;
import lando.systems.ld60.particles.effects.FootStepDustCloudEffect;

public class ExampleScroller extends MapScreen {

    private static final String TAG = ExampleScroller.class.getSimpleName();

    final Family skeletons;
    final SkeletonRendererDebug skeletonDebug;
    final HitSystem hitSystem;
    final FactoryExample factoryExample;

    boolean debugRender = false;
    boolean showHitboxes = false;

    public ExampleScroller(ZenGame game) {
//        super(game, GameMap.EXAMPLE_SCROLLER);
        super(game, GameMap.EXAMPLE_PLATFORMER); // NOTE: 00-example-tmnt.tmx disabled due to cleaning up unused art to reduce artifact size

        this.skeletons = world.family(Family.all(SpineAnimator.TYPE));
        this.skeletonDebug = new SkeletonRendererDebug();
        this.skeletonDebug.setBoundingBoxes(false);
        this.skeletonDebug.setRegionAttachments(false);
        this.factoryExample = new FactoryExample(world, pixel);

        this.hitSystem = new HitSystem(new GameStrikes());
        world.addSystem(new BrawlerPresentSystem());
        world.addSystem(hitSystem);
        world.addSystem(new HealthBarSystem());
        CombatEvent.signal.register(this::onCombatEvent);

        spawner .on(Factory.PLAYER, this::spawnPlayer)
                .on(FactoryExample.DUMMY,  this::spawnDummy)
                .on(Factory.WALL, (map, obj) -> factory.wall(obj));

        buildLevel();

        this.debugViews.add(DebugViews.spine(world));
        actions.defaultClick((x, y) -> factory.emitter(EmitterType.CLONE, new CloneEffect.Params(x, y), RenderLayer.BACKGROUND))
                .arm("spawn dummy",         Input.Keys.N, (x, y) -> factory.thug(x, y, (player != null && Position.get(player).x < x) ? -1 : 1))
                .arm("spawn pipe",          Input.Keys.K, (x, y) -> factory.pickup(GameWeapons.PIPE, x, y))
                .add("toggle debug render", Input.Keys.B, () -> debugRender  = !debugRender)
                .add("toggle hitboxes",     Input.Keys.H, () -> showHitboxes = !showHitboxes)
        ;
    }

    @Override
    protected ContactResponder createContactResponder() {
        return new BrawlContacts();
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
        var entity = factory.player(object);
        var anim = SpineAnimator.get(entity);
        anim.onEvent(event -> Log.debug(TAG, "event: %s", event.getData().getName()));
        factory.emitter(EmitterType.DUST, new FootStepDustCloudEffect.Params(entity));
        setPlayer(entity);
    }

    private void spawnDummy(Tilemap map, MapObject object) {
        int x = TiledProps.x(object);
        int y = TiledProps.y(object);
        int playerX = (player != null) ? Position.get(player).x : 0;
        int facing  = (playerX < x) ? -1 : +1;
        factoryExample.dummy(x, y, facing);
    }


    public void onCombatEvent(Signal<CombatEvent> signal, CombatEvent event) {
        switch (event) {
            case CombatEvent.Died d:
                var pos = Position.get(d.entity);
                factory.emitter(EmitterType.BLOOD, new BloodSplatterEffect.Params(pos.x, pos.y, 12), RenderLayer.BACKGROUND);
                break;
//            case CombatEvent.Struck s:
//                var pos = Position.get(player);
//                factory.emitter(EmitterType.BLOOD, new BloodSplatterEffect.Params(pos.x, pos.y), RenderLayer.BACKGROUND);
//                break;
            default:
                break;
        }
    }

    @Override
    public void dispose() {
        CombatEvent.signal.remove(this::onCombatEvent);
        super.dispose();
    }
}
