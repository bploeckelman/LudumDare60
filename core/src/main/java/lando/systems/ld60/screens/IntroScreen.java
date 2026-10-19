package lando.systems.ld60.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.zendogames.zenlib.ZenGame;
import com.zendogames.zenlib.render.RenderLayer;
import com.zendogames.zenlib.screens.ZenScreen;
import com.zendogames.zenlib.screens.transition.ScreenTransitionEffectType;
import lando.systems.ld60.Config;
import lando.systems.ld60.Main;
import lando.systems.ld60.assets.EmitterType;
import lando.systems.ld60.assets.GameUIAnim;
import lando.systems.ld60.assets.MusicType;
import lando.systems.ld60.events.AudioEvent;
import lando.systems.ld60.events.DistortionEvent;
import lando.systems.ld60.game.GameLevel;
import lando.systems.ld60.particles.effects.FireExplosionEffect;

import java.util.function.Supplier;

public class IntroScreen extends BaseScreen {

    private static final String TAG = IntroScreen.class.getSimpleName();

    Texture background;
    Texture mainTitle;
    Texture subTitle;
    Texture sequelTitle;

    private static final float BUTTON_WIDTH  = 320f;
    private static final float BUTTON_HEIGHT = 64f;
    private static final float BUTTON_MARGIN = 16f;

    public IntroScreen(ZenGame game) {
        super(game, false);
        addCommonSystems();
        worldCamera.position.set(Config.windowWidth / 2f, Config.windowHeight / 2f, 0f);
        worldCamera.update();
        background = new Texture(Gdx.files.internal("images/title-screen-blank_00.png"));
        mainTitle = new Texture(Gdx.files.internal("images/title-text-coot2_00.png"));
        subTitle = new Texture(Gdx.files.internal("images/title-text-obsolete_00.png"));
        sequelTitle = new Texture(Gdx.files.internal("images/title-text-evencooter_00.png"));

        var play     = buttonWithOnClick("Play",     () -> start(() -> new DialogScreenIntro(game)));
        var settings = buttonWithOnClick("Settings", settingsUI::toggle);
        var credits  = buttonWithOnClick("Credits",  () -> start(() -> new CreditsScreen(game)));

        var table = new VisTable();
        table.setFillParent(true);

        // Anchor the table contents to the bottom center
        table.align(Align.bottom);
        // Lift the buttons 100 pixels up from the bottom edge (adjust this to fit your titles!)
        table.padBottom(60f);

        table.add(play)     .size(BUTTON_WIDTH, BUTTON_HEIGHT).padBottom(BUTTON_MARGIN).row();
        table.add(settings) .size(BUTTON_WIDTH, BUTTON_HEIGHT).padBottom(BUTTON_MARGIN).row();
        table.add(credits)  .size(BUTTON_WIDTH, BUTTON_HEIGHT).padBottom(BUTTON_MARGIN).row();
        uiStage.addActor(table);

        menu.focus(play, settings, credits); // 'Play' is highlighted; keys or gamepad move from there

        AudioEvent.playMusic(MusicType.INTRO_THEME);

        addSettingButton();
    }

    private static VisTextButton disabledButtonWithOnClick(String text, Runnable onClick) {
        var button = buttonWithOnClick(text, onClick);
        button.setDisabled(true);
        return button;
    }

    private static VisTextButton buttonWithOnClick(String text, Runnable onClick) {
// 1. Copy the default VisUI text button style so we keep the standard font
        VisTextButton.VisTextButtonStyle style = new VisTextButton.VisTextButtonStyle(
            com.kotcrab.vis.ui.VisUI.getSkin().get(VisTextButton.VisTextButtonStyle.class)
        );

        // 2. Create a drawable from your custom retro texture
        com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable bgDrawable =
            new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(GameUIAnim.BUTTON.get().getKeyFrame(0));

        // 3. Override the default background states with your image
        style.up = bgDrawable;

        // Optional: If you want to visually see the button get pressed, you can tint the 'down' state grey
        style.down = bgDrawable.tint(new Color(0.7f, 0.7f, 0.7f, 1f));
        style.over = bgDrawable; // What it looks like when hovered

        // 4. Create the button using your custom style
        VisTextButton button = new VisTextButton(text, style);

        button.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                onClick.run();
            }
        });

        return button;
    }

    /** The screen is built only once the guard passes, so a double click cannot create two. */
    private void start(Supplier<ZenScreen> screen) {
        if (transitioning) return;
        transitioning = true;
        game.setScreen(screen.get(), ScreenTransitionEffectType.random());
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
//            AudioEvent.playSound(SoundType.SoundGroup.BELLS);
            DistortionEvent.trigger();
            var mouse = mouseWorldPosition();
            factory.emitter(EmitterType.FIRE_EXPLOSION, new FireExplosionEffect.Params(mouse.x, mouse.y, 20, 20), RenderLayer.OVERLAY);
        }
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
        batch.begin();
        batch.draw(background, 0, 0, Config.windowWidth, Config.windowHeight);
        batch.setColor(.6f, .6f, .6f, .3f);
        batch.draw(Main.game.pixel, 0, 0, Config.windowWidth, Config.windowHeight);
        batch.setColor(1, 1, 1, 1);

        // Calculate X to center each title horizontally
        float mainX = (Config.windowWidth - mainTitle.getWidth()) / 2f;
        float subX = (Config.windowWidth - subTitle.getWidth()) / 2f;
        float sequelX = (Config.windowWidth - sequelTitle.getWidth()) / 2f;

        // Calculate Y to stack them directly underneath each other
        float mainY = (Config.windowHeight * 0.8f) - (mainTitle.getHeight() / 2f);

        // Subtract the subTitle's height from the mainY to place it immediately below
        float padding = 3f; // Optional pixel gap between titles
        float subY = mainY - subTitle.getHeight() - padding;

        // Subtract the sequelTitle's height from the subY to place it below the subTitle
        float sequelY = subY - sequelTitle.getHeight() - padding;

        // --- NEW BOX DRAWING CODE ---
        float boxPadding = 20f; // Space around the edges of the text

        // Find the widest texture
        float maxWidth = Math.max(mainTitle.getWidth(), Math.max(subTitle.getWidth(), sequelTitle.getWidth()));
        float boxWidth = maxWidth + (boxPadding * 2f);

        // Height spans from the top of the main title to the bottom of the sequel title
        float topEdge = mainY + mainTitle.getHeight();
        float bottomEdge = sequelY;
        float boxHeight = topEdge - bottomEdge + (boxPadding * 2f);

        float boxX = (Config.windowWidth - boxWidth) / 2f;
        float boxY = bottomEdge - boxPadding;

        batch.setColor(0.3f, 0.3f, 0.3f, 0.8f);
        batch.draw(Main.game.pixel, boxX, boxY, boxWidth, boxHeight);

        // Reset the batch color to pure white before drawing the textures so they aren't tinted grey
        batch.setColor(1f, 1f, 1f, 1f);
        // ----------------------------

        batch.draw(mainTitle, mainX, mainY);
        batch.draw(subTitle, subX, subY);
        batch.draw(sequelTitle, sequelX, sequelY);

        batch.end();
        uiStage.draw();
    }
}
