package lando.systems.ld60.screens;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.esotericsoftware.spine.SkeletonRendererDebug;
import com.zendogames.zenlib.Time;
import com.zendogames.zenlib.ZenGame;
import com.zendogames.zenlib.collision.ContactResponder;
import com.zendogames.zenlib.ecs.Entity;
import com.zendogames.zenlib.ecs.Family;
import com.zendogames.zenlib.ecs.components.Position;
import com.zendogames.zenlib.ecs.components.SpineAnimator;
import com.zendogames.zenlib.render.RenderLayer;
import com.zendogames.zenlib.render.WorldView;
import com.zendogames.zenlib.screens.transition.ScreenTransitionEffectType;
import com.zendogames.zenlib.ui.DebugViews;
import lando.systems.ld60.assets.EmitterType;
import lando.systems.ld60.events.AudioEvent;
import lando.systems.ld60.events.CombatEvent;
import lando.systems.ld60.events.Signal;
import lando.systems.ld60.events.SignalListener;
import lando.systems.ld60.game.*;
import lando.systems.ld60.game.combat.HitboxDraw;
import lando.systems.ld60.game.systems.BrawlerPresentSystem;
import lando.systems.ld60.game.systems.HealthBarSystem;
import lando.systems.ld60.game.systems.HitSystem;
import lando.systems.ld60.particles.effects.BloodSprayEffect;
import lando.systems.ld60.particles.effects.BloodSplatterEffect;
import lando.systems.ld60.particles.effects.FootStepDustCloudEffect;

/**
 * The game: one {@link GameLevel} at a time on the brawler's systems (presentation, hits,
 * contacts), spawning the level's objects through {@link Factory}. The scroller example stays
 * as reference; everything the real content needs accumulates here.
 */
public class GameScreen extends MapScreen implements SignalListener<CombatEvent> {

    static final int VIEW_WIDTH  = 320 * 2;
    static final int VIEW_HEIGHT = 180 * 2;

    final GameLevel level;
    final Family skeletons;
    final SkeletonRendererDebug skeletonDebug;
    final HitSystem hitSystem;
    final TelevisionFeed televisions;

    public boolean debugRender  = false;
    public boolean showHitboxes = false;

    public GameScreen(ZenGame game, GameLevel level) {
        super(game, level.map);
        this.level = level;

        this.skeletons = world.family(Family.all(SpineAnimator.TYPE));
        this.skeletonDebug = new SkeletonRendererDebug();
        this.skeletonDebug.setBoundingBoxes(false);
        this.skeletonDebug.setRegionAttachments(false);

        this.hitSystem = new HitSystem(new GameStrikes());
        this.televisions = new TelevisionFeed(VIEW_WIDTH / 2, VIEW_HEIGHT / 2);
        this.televisions.clearColor.set(worldView.clearColor);
        world.addSystem(new BrawlerPresentSystem());
        world.addSystem(hitSystem);
        world.addSystem(new HealthBarSystem());

        spawner .on(Factory.PLAYER,     (map, obj) -> spawnPlayer(factory.hero(obj)))
                .on(Factory.WALL,       (map, obj) -> factory.wall(obj))
                .on(Factory.ENEMY,      (map, obj) -> factory.enemy(obj))
                .on(Factory.PICKUP,     (map, obj) -> factory.pickup(obj))
                .on(Factory.TELEVISION, (map, obj) -> factory.television(obj, televisions))
        ;

        buildLevel();

        worldCamera.position.set(VIEW_WIDTH / 2f, VIEW_HEIGHT / 2f, 0f);
        worldCamera.update();

        CombatEvent.signal.register(this);
        AudioEvent.playMusic(level.music);

        this.debugViews.add(DebugViews.spine(world));
        actions .arm("spawn thug",          Input.Keys.N, (x, y) -> factory.thug(x, y, (player != null && Position.get(player).x < x) ? -1 : 1))
                .arm("spawn pipe",          Input.Keys.K, (x, y) -> factory.pickup(GameWeapons.PIPE, x, y))
                .add("next level",          Input.Keys.RIGHT_BRACKET, () -> game.setScreen(new GameScreen(game, level.next()), ScreenTransitionEffectType.random()))
                .add("toggle debug render", Input.Keys.B, () -> debugRender  = !debugRender)
                .add("toggle hitboxes",     Input.Keys.H, () -> showHitboxes = !showHitboxes);
    }

    /** In a brawler only hits hurt; contacts are for pickups and triggers. */
    @Override
    protected ContactResponder createContactResponder() {
        return new BrawlContacts();
    }

    private void spawnPlayer(Entity hero) {
        factory.emitter(EmitterType.DUST, new FootStepDustCloudEffect.Params(hero));
        setPlayer(hero);
    }

    @Override
    public void renderOffscreenBuffers(Batch batch) {
        televisions.capture(batch, worldCamera, b -> {
            b.begin();
            world.render(Time.delta);
            b.end();
        });
        super.renderOffscreenBuffers(batch);
    }

    @Override
    public void dispose() {
        televisions.dispose();
        super.dispose();
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
                skeletonDebug.draw(SpineAnimator.get(entities.get(i)).skeleton);
            }
        }
    }

    @Override
    protected WorldView createWorldView() {
        var view = new WorldView(worldCamera, VIEW_WIDTH, VIEW_HEIGHT);
        view.clearColor.set(Color.DARK_GRAY);
        return view;
    }

    @Override
    public void receive(Signal<CombatEvent> signal, CombatEvent event) {
        switch (event) {
            case CombatEvent.Struck s:
                var pos = Position.get(s.strike.target());
                var anim = SpineAnimator.get(s.strike.target());
                factory.emitter(EmitterType.BLOOD_SPRAY, new BloodSprayEffect.Params(pos.x, pos.y + 40f, s.strike.away()), RenderLayer.BACKGROUND);
                factory.emitter(EmitterType.BLOOD, new BloodSplatterEffect.Params(pos.x + -anim.facing * 50f, pos.y, 1), RenderLayer.BACKGROUND);
                break;
            case CombatEvent.Died d:
                var deadPos = Position.get(d.entity);
                var deadAnim = SpineAnimator.get(d.entity);
                factory.emitter(EmitterType.BLOOD, new BloodSplatterEffect.Params(deadPos.x + -deadAnim.facing * 50f, deadPos.y, 12), RenderLayer.BACKGROUND);
                break;
            default:
                break;
        }
    }
}
