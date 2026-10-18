package lando.systems.ld60.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.zendogames.zenlib.Log;
import com.zendogames.zenlib.ZenGame;
import com.zendogames.zenlib.render.RenderLayer;
import com.zendogames.zenlib.screens.ZenScreen;
import com.zendogames.zenlib.screens.transition.ScreenTransitionEffectType;
import lando.systems.ld60.assets.EmitterType;
import lando.systems.ld60.assets.MusicType;
import lando.systems.ld60.assets.SoundType;
import lando.systems.ld60.events.AudioEvent;
import lando.systems.ld60.particles.effects.CloneEffect;

import java.util.function.Supplier;

public class IntroScreen extends BaseScreen {

    private static final String TAG = IntroScreen.class.getSimpleName();

    private static final float BUTTON_WIDTH  = 320f;
    private static final float BUTTON_HEIGHT = 64f;
    private static final float BUTTON_MARGIN = 16f;

    public IntroScreen(ZenGame game) {
        super(game, false);
        addCommonSystems();

        var title = new VisLabel("Ludum Dare 60");

        var table = new VisTable();
        table.setFillParent(true);
        table.add(title).padBottom(BUTTON_MARGIN * 3).row();
        table.add(buttonWithOnClick("Example: Platformer", () -> start(() -> new ExamplePlatformer(game)))).size(BUTTON_WIDTH, BUTTON_HEIGHT).padBottom(BUTTON_MARGIN).row();
        table.add(buttonWithOnClick("Example: Overhead",   () -> start(() -> new ExampleOverhead(game))))  .size(BUTTON_WIDTH, BUTTON_HEIGHT).padBottom(BUTTON_MARGIN).row();
        table.add(buttonWithOnClick("Example: Scroller",   () -> start(() -> new ExampleScroller(game))))  .size(BUTTON_WIDTH, BUTTON_HEIGHT).padBottom(BUTTON_MARGIN).row();
        table.add(buttonWithOnClick("Example: Spine",      () -> start(() -> new ExampleSpine(game))))     .size(BUTTON_WIDTH, BUTTON_HEIGHT).padBottom(BUTTON_MARGIN).row();
        table.add(new VisTextButton("Settings")).size(BUTTON_WIDTH, BUTTON_HEIGHT).padBottom(BUTTON_MARGIN).row();
        table.add(new VisTextButton("Credits")).size(BUTTON_WIDTH, BUTTON_HEIGHT).padBottom(BUTTON_MARGIN).row();
        uiStage.addActor(table);
        AudioEvent.playMusic(MusicType.MAIN_THEME);
    }

    private static VisTextButton buttonWithOnClick(String text, Runnable onClick) {
        return new VisTextButton(text, new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                onClick.run();
            }
        });
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
            Log.info(TAG, "Left mouse button clicked");
            AudioEvent.playSound(SoundType.SoundGroup.BELLS);
            var mouse = mouseWorldPosition();
            factory.emitter(EmitterType.CLONE, new CloneEffect.Params(mouse.x, mouse.y), RenderLayer.OVERLAY);
        }
    }
}
