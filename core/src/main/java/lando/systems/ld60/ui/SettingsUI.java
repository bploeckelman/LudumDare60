package lando.systems.ld60.ui;

import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisWindow;
import com.zendogames.zenlib.Log;
import de.golfgl.gdx.controllers.ControllerMenuStage;
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
    public VisCheckBox invulnerabilityCheckBox;
    public ControllerSlider musicSlider;
    public ControllerSlider soundSlider;
    public VisTextButton controlsButton;
    public VisTextButton closeButton;
    private ControlsUI controlsUI;

    /** Whoever had focus when the window opened, focused again when it closes. */
    private Actor returnFocusTo;

    float panelWidth  = Config.windowWidth  * 0.9f;
    float panelHeight = Config.windowHeight * 0.9f;
    Preferences prefs = Main.game.prefs;

    public SettingsUI(ControlsUI controlsUI) {
        super("");
        this.controlsUI = controlsUI;
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
        this.invulnerabilityCheckBox = new VisCheckBox("Invulnerability");
        invulnerabilityCheckBox.setChecked(prefs.getBoolean("invulnerability", false));
        invulnerabilityCheckBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                prefs.putBoolean("invulnerability", invulnerabilityCheckBox.isChecked());
                prefs.flush();
                GameFlags.INVULNERABILITY.set(prefs.getBoolean("invulnerability", false));
                invulnerabilityCheckBox.setChecked(prefs.getBoolean("invulnerability", false));
            }
        });
        add(crtShaderCheckBox).row();
        add(invulnerabilityCheckBox).row();

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

        this.controlsButton = new VisTextButton("View Controls");
        controlsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Keep settings open underneath, but bring controls to the front
                controlsUI.open();
            }
        });
        add(controlsButton).height(35f).padBottom(5f).row();

        this.closeButton = new VisTextButton("Close");
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                close();
            }
        });
        add(closeButton).height(35f).row();
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void toggle() {
        if (isVisible) close();
        else           open();
    }

    public void open() {
        isVisible = true;
        setVisible(true);
        toFront();
        // TODO: play a sound?
        if (getStage() instanceof ControllerMenuStage menu) {
            returnFocusTo = menu.getFocusedActor();
            menu.setFocusedActor(soundSlider);
        }
    }

    public void close() {
        isVisible = false;
        setVisible(false);
        if (getStage() instanceof ControllerMenuStage menu) {
            menu.setFocusedActor(returnFocusTo);
            returnFocusTo = null;
        }
    }
}
