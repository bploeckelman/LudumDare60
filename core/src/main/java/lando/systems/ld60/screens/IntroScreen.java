package lando.systems.ld60.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.zendogames.zenlib.ZenGame;
import com.zendogames.zenlib.screens.ZenScreen;
import com.zendogames.zenlib.screens.transition.ScreenTransitionEffectType;

public class IntroScreen extends ZenScreen {

    private static final String TAG = IntroScreen.class.getSimpleName();
    private static final float BUTTON_WIDTH  = 320f;
    private static final float BUTTON_HEIGHT = 64f;
    private static final float BUTTON_MARGIN = 16f;

    public IntroScreen(ZenGame game) {
        super(game);

        var title = new VisLabel("Ludum Dare 60");

        var table = new VisTable();
        table.setFillParent(true);
        table.add(title).padBottom(BUTTON_MARGIN * 3).row();
        table.add(buttonWithOnClick("Game", this::startGame)).size(BUTTON_WIDTH, BUTTON_HEIGHT).padBottom(BUTTON_MARGIN).row();
        table.add(new VisTextButton("Settings")).size(BUTTON_WIDTH, BUTTON_HEIGHT).padBottom(BUTTON_MARGIN).row();
        table.add(new VisTextButton("Credits")).size(BUTTON_WIDTH, BUTTON_HEIGHT).padBottom(BUTTON_MARGIN).row();
        uiStage.addActor(table);
    }

    private static VisTextButton buttonWithOnClick(String text, Runnable onClick) {
        return new VisTextButton(text, new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                onClick.run();
            }
        });
    }

    private void startGame() {
        if (transitioning) return;
        transitioning = true;
        game.setScreen(new ExampleScreen(game), ScreenTransitionEffectType.random());
    }
}
