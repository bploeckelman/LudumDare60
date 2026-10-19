package lando.systems.ld60.ui;

import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisWindow;
import com.zendogames.zenlib.Log;
import de.golfgl.gdx.controllers.ControllerSlider;
import lando.systems.ld60.Config;
import lando.systems.ld60.GameFlags;
import lando.systems.ld60.Main;
import lando.systems.ld60.assets.GameUIAnim;
import lando.systems.ld60.assets.SoundType;
import lando.systems.ld60.events.AudioEvent;
import lando.systems.ld60.game.systems.AudioSystem;

public class SettingsUI extends VisWindow {

    private static final String TAG = SettingsUI.class.getSimpleName();

    public boolean isVisible = false;
    public VisCheckBox crtShaderCheckBox;
    public ControllerSlider musicSlider;
    public ControllerSlider soundSlider;
    public VisTextButton closeButton;

    float panelWidth  = Config.windowWidth  * 0.9f;
    float panelHeight = Config.windowHeight * 0.9f;
    Preferences prefs = Main.game.prefs;

    public SettingsUI() {
        super("");

        GameFlags.CRT_SHADER.set(prefs.getBoolean("crtShader", true));

        setBackground(new TextureRegionDrawable(GameUIAnim.SETTINGS.get().getKeyFrame(0)));
        setWidth(panelWidth);
        setHeight(panelHeight);
        setPosition(Config.windowWidth * .05f, Config.windowHeight * .05f);
        setVisible(false);

        add().height(40f).row();

        this.crtShaderCheckBox = new VisCheckBox("CRT Shader");
        crtShaderCheckBox.setChecked(prefs.getBoolean("crtShader", true));
        crtShaderCheckBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                prefs.putBoolean("crtShader", crtShaderCheckBox.isChecked());
                prefs.flush();
                GameFlags.CRT_SHADER.set(prefs.getBoolean("crtShader", true));
                crtShaderCheckBox.setChecked(prefs.getBoolean("crtShader", true));
            }
        });
        add(crtShaderCheckBox).row();

        var musicLabel = new VisLabel("Music");
        add().height(10f).row();
        add(musicLabel).row();

        this.musicSlider = new ControllerSlider(0, 1f, 0.1f, false, VisUI.getSkin());
        musicSlider.setValue(prefs.getFloat("musicVolume", 0.5f));
        musicSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Log.debug(TAG, "musicSlider changed: %0.2f", musicSlider.getValue());
                AudioSystem.setMusicVolume(musicSlider.getValue());
            }
        });
        add(musicSlider).width(panelWidth / 2f).row();

        add().height(10f).row();

        var soundLabel = new VisLabel("Sound");
        add(soundLabel).row();

        this.soundSlider = new ControllerSlider(0, 1f, 0.1f, false, VisUI.getSkin());
        soundSlider.setValue(prefs.getFloat("soundVolume", 0.5f));
        soundSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Log.debug(TAG, "soundSlider changed: %0.2f", soundSlider.getValue());
                AudioSystem.setSoundVolume(soundSlider.getValue());
                AudioEvent.playSound(SoundType.BELL_HIGH);
            }
        });
        add(soundSlider).width(panelWidth / 2f).row();

        add().height(20f).row();

        this.closeButton = new VisTextButton("Close");
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                toggle();
            }
        });
        add(closeButton).height(35f).row();
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void toggle() {
        isVisible = !isVisible;
        setVisible(isVisible);
        if (isVisible) {
            toFront();
            // TODO: play a sound?
        }
    }
}
