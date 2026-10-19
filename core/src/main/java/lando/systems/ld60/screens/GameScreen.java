package lando.systems.ld60.screens;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.esotericsoftware.spine.SkeletonRendererDebug;
import com.github.tommyettinger.textra.TypingLabel;
import com.zendogames.zenlib.Log;
import com.zendogames.zenlib.Time;
import com.zendogames.zenlib.ZenGame;
import com.zendogames.zenlib.collision.ContactResponder;
import com.zendogames.zenlib.ecs.Entity;
import com.zendogames.zenlib.ecs.Family;
import com.zendogames.zenlib.ecs.components.*;
import com.zendogames.zenlib.render.RenderLayer;
import com.zendogames.zenlib.render.WorldView;
import com.zendogames.zenlib.screens.transition.ScreenTransitionEffectType;
import com.zendogames.zenlib.ui.DebugViews;
import lando.systems.ld60.assets.EmitterType;
import lando.systems.ld60.assets.GameFont;
import lando.systems.ld60.events.AudioEvent;
import lando.systems.ld60.events.CombatEvent;
import lando.systems.ld60.events.Signal;
import lando.systems.ld60.events.SignalListener;
import lando.systems.ld60.game.*;
import lando.systems.ld60.game.combat.HitboxDraw;
import lando.systems.ld60.game.movement.BrawlerMotion;
import lando.systems.ld60.game.systems.BrawlerPresentSystem;
import lando.systems.ld60.game.systems.HealthBarSystem;
import lando.systems.ld60.game.systems.HitSystem;
import lando.systems.ld60.game.systems.LevelControlSystem;
import lando.systems.ld60.particles.effects.BloodSprayEffect;
import lando.systems.ld60.particles.effects.BloodSplatterEffect;
import lando.systems.ld60.particles.effects.FootStepDustCloudEffect;
import lando.systems.ld60.ui.BossHealthWidget;
import lando.systems.ld60.ui.PlayerHealthWidget;

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
    final Family enemies;
    final SkeletonRendererDebug skeletonDebug;
    final HitSystem hitSystem;
    final TelevisionFeed televisions;
    final LevelControlSystem levelControl; // Add this reference

    private BossHealthWidget bossWidget;
    private TypingLabel bossLabel;

    public boolean debugRender  = false;
    public boolean showHitboxes = false;

    public GameScreen(ZenGame game, GameLevel level) {
        super(game, level.map);
        this.level = level;

        this.skeletons = world.family(Family.all(SpineAnimator.TYPE));
        this.enemies   = world.family(Family.all(GameTags.ENEMY, Mover.TYPE));
        this.skeletonDebug = new SkeletonRendererDebug();
        this.skeletonDebug.setBoundingBoxes(false);
        this.skeletonDebug.setRegionAttachments(false);

        this.hitSystem = new HitSystem(new GameStrikes());
        this.televisions = new TelevisionFeed(VIEW_WIDTH / 2, VIEW_HEIGHT / 2);
        this.televisions.clearColor.set(worldView.clearColor);

        // Initialize the control system to route map objects to the correct Factory method
        this.levelControl = new LevelControlSystem(this, factory::enemy, () -> {
            game.setScreen(new GameScreen(game, level.next()), ScreenTransitionEffectType.random());
        });

        world.addSystem(new BrawlerPresentSystem());
        world.addSystem(hitSystem);
        world.addSystem(new HealthBarSystem());
        world.addSystem(levelControl);

        spawner .on(Factory.PLAYER,     (_, obj) -> spawnPlayer(factory.hero(obj)))
                .on(Factory.WALL,       (_, obj) -> factory.wall(obj))
                .on(Factory.ENEMY,      (_, obj) -> levelControl.queueEnemy(obj)) // Defer spawning
                .on(Factory.PICKUP,     (_, obj) -> factory.pickup(obj))
                .on(Factory.TELEVISION, (_, obj) -> factory.television(obj, televisions))
                .on(Factory.STAGE_MARKER,            (_, _) -> {}) // Safely ignore stage markers
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
                .add("toggle hitboxes",     Input.Keys.H, () -> showHitboxes = !showHitboxes)
                // TEMP: the clips a thug's brain never triggers on its own (jump; knockback without a weapon that lifts)
                .add("enemies hop",         Input.Keys.J, () -> eachEnemy((_, mover, motion) -> motion.jump(mover)))
                .add("enemies knocked back",Input.Keys.L, () -> eachEnemy((e, mover, motion) -> motion.launch(mover, awayFromPlayer(e), /*dist*/ 3f, /*height*/ 2f)));

        PlayerHealthWidget healthWidget = new PlayerHealthWidget(this);
        healthWidget.setSize(300f, 25f);
        // Position it 20 pixels from the left and 20 pixels down from the top of the UI camera
        healthWidget.setPosition(20f, uiStage.getHeight() - healthWidget.getHeight() - 30f);
        uiStage.addActor(healthWidget);

        TypingLabel playerLabel = new TypingLabel("{OUTLINECOLOR=BLACK}{OUTLINE=2}{SPEED=2.5}PLAYER 1", GameFont.DOS_FONT.get());        playerLabel.setPosition(healthWidget.getX(), healthWidget.getY() - 30);
        uiStage.addActor(playerLabel);

        // Initialize Boss Widget but make it invisible
        bossWidget = new BossHealthWidget(levelControl);
        bossWidget.setSize(300f, 25f);
        bossWidget.setPosition(uiStage.getWidth() - bossWidget.getWidth() - 20f, uiStage.getHeight() - bossWidget.getHeight() - 30f);
        bossWidget.setVisible(false);
        uiStage.addActor(bossWidget);

        // Initialize Boss Label but make it invisible
        bossLabel = new TypingLabel("", GameFont.DOS_FONT.get());
        bossLabel.setVisible(false);
        uiStage.addActor(bossLabel);
    }

    interface EnemyMotionAction { void run(Entity e, Mover mover, BrawlerMotion motion); }

    /** TEMP: drives every grounded enemy brawler's motion directly (no strike, so no flash, health or facing change). */
    private void eachEnemy(EnemyMotionAction action) {
        var entities = enemies.entities();
        for (int i = 0; i < entities.size; i++) {
            var e     = entities.get(i);
            var mover = Mover.get(e);
            if (mover.motion instanceof BrawlerMotion motion && motion.grounded && !motion.launched) {
                action.run(e, mover, motion);
            }
        }
    }

    @Override
    public void update(float dt) {
        super.update(dt);

        Entity activeBoss = levelControl.getBossEntity();

        if (activeBoss != null && activeBoss.isAlive()) {
            if (!bossWidget.isVisible()) {
                bossWidget.setVisible(true);

                // Get the boss name from the entity
                Name nameComp = Name.get(activeBoss);
                String rawName = (nameComp != null) ? nameComp.name : "UNKNOWN BOSS";

                // Set the text with the red color and black outline
                bossLabel.restart("{COLOR=YELLOW}{OUTLINECOLOR=BLACK}{OUTLINE=2}{SPEED=2.5}" + rawName.toUpperCase());

                // Re-align the label to match the right-aligned health bar since text length changed
                bossLabel.pack(); // forces it to compute its width immediately
                bossLabel.setPosition(bossWidget.getX() + bossWidget.getWidth() - bossLabel.getWidth(), bossWidget.getY() - 30);
                bossLabel.setVisible(true);

                // TODO: Change music or other stuff?
                Log.info("GameScreen", "Boss spawned!");

            }
        } else {
            if (bossWidget.isVisible()) {
                bossWidget.setVisible(false);
                bossLabel.setVisible(false);
            }
        }
    }

    /** -1/+1 pointing from the player to {@code e}; +1 when there is no player. */
    private int awayFromPlayer(Entity e) {
        return (player != null && Position.get(player).x > Position.get(e).x) ? -1 : 1;
    }

    @Override
    protected void buildLevel() {
        world.clear();

        player = null;
        tilemap = new Tilemap(map.get());
        tilemap.spawn(world, GameMask.SOLID, batch, worldCamera);
        cameraFollow.bounds = new Rectangle(0, 0, tilemap.width(), tilemap.height());

        // Notify the control system to prepare tracking, then execute the spawns
        levelControl.startLevel(tilemap);
        spawner.spawnAll(tilemap);
        levelControl.finishSpawning();

        mapCenter.set(tilemap.width() / 2f, tilemap.height() / 2f);
        if (player != null) cameraFollow.follow(player, /*snap*/ true);

        Log.info("GameScreen", "built level: %dx%d tiles, %d layers, %d objects, player %s",
            tilemap.cols, tilemap.rows, tilemap.layers.size, tilemap.objects.size, player == null ? "none" : player.toString());
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
