package lando.systems.ld60.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.github.tommyettinger.textra.Font;
import com.github.tommyettinger.textra.TypingLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.zendogames.zenlib.Zen;
import com.zendogames.zenlib.ZenGame;
import com.zendogames.zenlib.ecs.Entity;
import com.zendogames.zenlib.ecs.components.Name;
import com.zendogames.zenlib.ecs.components.Position;
import com.zendogames.zenlib.ecs.components.SpineAnimator;
import com.zendogames.zenlib.screens.transition.ScreenTransitionEffectType;
import lando.systems.ld60.assets.GameFont;
import lando.systems.ld60.assets.GameSpine;
import lando.systems.ld60.assets.MusicType;
import lando.systems.ld60.assets.PetAnim;
import lando.systems.ld60.events.AudioEvent;

public class CreditsScreen extends BaseScreen {

    private static final float PANEL_WIDTH = 340f;
    private static final float PANEL_PAD = 10f;
    private static final float PET_SIZE = 32f;
    private static final Color PANEL_COLOR = new Color(0f, 0f, 0f, 0.7f);
    private static final Color GROUND_COLOR = new Color(0.1f, 0.1f, 0.1f, 1f);
    private static final Color RUNNER_TINT = new Color(0.7f, 0.7f, 0.7f, 1f);

    private static final String[] ACTIONS = { "jump", "kick", "punch", "swing", "death" };

    private final Font creditFont = GameFont.DOS_FONT.get();
    private final Array<Pet> pets = new Array<>();
    private final Array<Runner> runners = new Array<>();
    private final float width = Zen.config.framebufferWidth();
    private final float height = Zen.config.framebufferHeight();
    private float accum = 0f;

    private record Pet(PetAnim anim, TextureRegionDrawable drawable) {}

    public CreditsScreen(ZenGame game) {
        super(game, false);
        addCommonSystems();

        worldCamera.position.set(width / 2f, height / 2f, 0f);
        worldCamera.update();

        runners.add(new Runner(GameSpine.AWOL, "run", -100f, 40f, 1, 160f));
        runners.add(new Runner(GameSpine.FLOPPYD, "walk", width * 0.5f, 40f, 1, 80f));
        runners.add(new Runner(GameSpine.BLACKBERRY, "run", width + 100f, 20f, -1, 180f));
        runners.add(new Runner(GameSpine.BRICKPHONE, "walk", width * 0.3f, 20f, -1, 90f));

        var back = new VisTextButton("Back to title", new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                if (transitioning) return;
                transitioning = true;
                game.setScreen(new IntroScreen(game), ScreenTransitionEffectType.random());
            }
        });

        var root = new VisTable();
        root.setFillParent(true);
        root.top().padTop(50f);
        root.add(header()).colspan(2).row();
        root.add(leftPanel()).width(PANEL_WIDTH).top().pad(PANEL_PAD);
        root.add(rightPanel()).width(PANEL_WIDTH).top().pad(PANEL_PAD).row();
        root.add().colspan(2).growY().row();
        root.add(label("{FADE}Thanks for playing!{ENDFADE}\n{SIZE=50%}Quote of the day: Don't get old.{SIZE=100%}")).colspan(2).row();
        root.add(back).colspan(2).size(240f, 48f).padTop(PANEL_PAD).padBottom(PANEL_PAD);
        uiStage.addActor(root);

        AudioEvent.stopAllMusic();
        AudioEvent.playMusic(MusicType.READING_RAINBOW);
    }

    private VisTable header() {
        var table = new VisTable();
        table.add(label("{SIZE=200%}{RAINBOW}Ludum Dare 60{ENDRAINBOW}{SIZE=100%}")).row();
        table.add(label("Theme: Old Tech")).padTop(4f).padBottom(PANEL_PAD);
        return table;
    }

    private VisTable leftPanel() {
        var panel = panel();
        panel.add(label("{GRADIENT=GOLD;ORANGE}Code{ENDGRADIENT}")).row();
        panel.add(label("Brian Ploeckelman\nDoug Graham\nJeffrey Hwang")).padBottom(PANEL_PAD * 2).row();
        panel.add(label("{GRADIENT=GOLD;ORANGE}Pets{ENDGRADIENT}")).row();

        var petTable = new VisTable();
        petTable.defaults().pad(2f);

        var asuka = new TextureRegionDrawable(PetAnim.ASUKA.get().getKeyFrame(0f));
        pets.add(new Pet(PetAnim.ASUKA, asuka));
        petTable.add(new Image(asuka)).size(PET_SIZE);
        petTable.add(label("Asuka")).left().row();

        var osha = new TextureRegionDrawable(PetAnim.OSHA.get().getKeyFrame(0f));
        pets.add(new Pet(PetAnim.OSHA, osha));
        petTable.add(new Image(osha)).size(PET_SIZE);
        petTable.add(label("Osha")).left().row();

        var cherry = new TextureRegionDrawable(PetAnim.CHERRY.get().getKeyFrame(0f));
        pets.add(new Pet(PetAnim.CHERRY, cherry));
        petTable.add(new Image(cherry)).size(PET_SIZE);
        petTable.add(label("Cherry")).left().row();

        panel.add(petTable);
        return panel;
    }

    private VisTable rightPanel() {
        var panel = panel();
        panel.add(label("{GRADIENT=GOLD;ORANGE}Art{ENDGRADIENT}")).row();
        panel.add(label("Matt Neuman\nPete Valeo")).padBottom(PANEL_PAD * 2).row();
        panel.add(label("{GRADIENT=WHITE;GREEN}Audio{ENDGRADIENT}")).row();
        panel.add(label("Pete Valeo")).padBottom(PANEL_PAD * 2).row();
        panel.add(label("{GRADIENT=BLUE;PINK}Made With{ENDGRADIENT}")).row();
        panel.add(label("libGDX, Spine, and {COLOR=RED}Love{ENDCOLOR}"));
        return panel;
    }

    private VisTable panel() {
        var panel = new VisTable();
        panel.setBackground(panelBackground());
        panel.pad(PANEL_PAD);
        panel.defaults().center();
        return panel;
    }

    private Drawable panelBackground() {
        return new TextureRegionDrawable(pixel).tint(PANEL_COLOR);
    }

    private TypingLabel label(String text) {
        return new TypingLabel(text, creditFont);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        accum += delta;
        for (var pet : pets) {
            pet.drawable().setRegion(pet.anim().get().getKeyFrame(accum));
        }
    }

    @Override
    protected void afterWorldUpdate(float dt) {
        for (var runner : runners) {
            runner.update(dt);
        }
    }

    @Override
    protected void renderWorld(Batch batch) {
        batch.setProjectionMatrix(worldCamera.combined);
        batch.begin();
        batch.setColor(GROUND_COLOR);
        batch.draw(pixel, 0f, 0f, width, 120f);
        batch.setColor(Color.WHITE);
        batch.end();
        super.renderWorld(batch);
    }

    private class Runner {
        final Entity entity;
        final SpineAnimator anim;
        final String defaultAnimation;
        final int facing;
        final float speed;
        float x;
        float actionTimer = MathUtils.random(2f, 5f);

        Runner(GameSpine spine, String defaultAnimation, float x, float y, int facing, float speed) {
            this.defaultAnimation = defaultAnimation;
            this.facing = facing;
            this.speed = speed;
            this.x = x;
            this.anim = new SpineAnimator(spine, defaultAnimation);
            anim.facing = facing;
            anim.tint.set(RUNNER_TINT);
            this.entity = world.createEntity()
                .add(new Name(spine.name().toLowerCase()))
                .add(new Position(x, y))
                .add(anim);
        }

        void update(float dt) {
            var pos = Position.get(entity);
            pos.savePrev();

            actionTimer -= dt;
            if (actionTimer <= 0f) {
                // do special action
                actionTimer = MathUtils.random(3f, 7f);
                anim.start(ACTIONS[MathUtils.random(ACTIONS.length - 1)], false);
                anim.queue(defaultAnimation, true, 0f);
            }

            if (anim.is(defaultAnimation)) {
                // not doing special action, move along
                x += facing * speed * dt;
            }

            //offscreen
            if (facing > 0 && x > width + 120f) {
                x = -120f;
            }

            if (facing < 0 && x < -120f) {
                x = width + 120f;
            }

            // if offscreen, teleport
            if (Math.abs(x - pos.x) > width) {
                pos.teleport((int) x, pos.y);
            } else {
                pos.x = (int) x;
            }
        }
    }
}
