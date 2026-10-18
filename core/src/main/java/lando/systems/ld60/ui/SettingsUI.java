package lando.systems.ld60.ui;

import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.kotcrab.vis.ui.widget.*;
import lando.systems.ld60.Config;
import lando.systems.ld60.GameFlags;
import lando.systems.ld60.Main;
import lando.systems.ld60.assets.GameFont;
import lando.systems.ld60.assets.GameUIAnim;
import lando.systems.ld60.assets.SoundType;
import lando.systems.ld60.events.AudioEvent;
import lando.systems.ld60.game.systems.AudioSystem;

public class SettingsUI extends VisWindow {

    public boolean isVisible = false;
    float panelWidth = Config.windowWidth * .90f;
    float panelHeight = Config.windowHeight * .90f;
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

        VisCheckBox crtShaderCheckBox = new VisCheckBox("CRT Shader");
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

        VisLabel musicLabel = new VisLabel("Music");
        add().height(10f).row();
        add(musicLabel).row();

        VisSlider musicSlider = new VisSlider(0, 1f, 0.1f, false);
        musicSlider.setValue(prefs.getFloat("musicVolume", 0.5f));
        musicSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
//                Util.log("musicSlider changed: " + musicSlider.getValue());
                AudioSystem.setMusicVolume(musicSlider.getValue());
            }
        });
        add(musicSlider).width(panelWidth / 2f).row();

        add().height(10f).row();

        VisLabel soundLabel = new VisLabel("Sound");
        add(soundLabel).row();

        VisSlider soundSlider = new VisSlider(0, 1f, 0.1f, false);
        soundSlider.setValue(prefs.getFloat("soundVolume", 0.5f));
        soundSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
//                Util.log("soundSlider changed: " + soundSlider.getValue());
                AudioSystem.setSoundVolume(soundSlider.getValue());
                AudioEvent.playSound(SoundType.BELL_HIGH);
            }
        });
        add(soundSlider).width(panelWidth / 2f).row();

        add().height(20f).row();

        VisTextButton closeButton = new VisTextButton("Close");
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
