package lando.systems.ld60.ui;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisWindow;
import com.zendogames.zenlib.Log;
import de.golfgl.gdx.controllers.ControllerMenuStage;
import lando.systems.ld60.Config;
import lando.systems.ld60.assets.GameUIAnim;

public class ControlsUI extends VisWindow {

    private static final String TAG = ControlsUI.class.getSimpleName();
    public boolean isVisible = false;
    public VisTextButton closeButton;

    /** Whoever had focus when the window opened, focused again when it closes. */
    private Actor returnFocusTo;

    float panelWidth  = Config.windowWidth  * 0.9f;
    float panelHeight = Config.windowHeight * 0.9f;

    public ControlsUI() {
        super("");
        // Load the magenta-to-alpha converted "CONTROLS" background
        setBackground(new TextureRegionDrawable(GameUIAnim.CONTROLS.get().getKeyFrame(0)));
        setWidth(panelWidth);
        setHeight(panelHeight);
        setPosition(Config.windowWidth * .05f, Config.windowHeight * .05f);
        setVisible(false);

        // REDUCED: Bring top space down from 45f to 30f to free up vertical room at the bottom
        add().height(30f).colspan(2).row();

        // Left Column (Movement/Navigation) & Right Column (Combat/Interactions) Tables
        var leftTable = new com.badlogic.gdx.scenes.scene2d.ui.Table();
        var rightTable = new com.badlogic.gdx.scenes.scene2d.ui.Table();

        // Tighten internal vertical space to be absolutely sure the Close button isn't cut off
        leftTable.defaults().space(4f);
        rightTable.defaults().space(4f);

        // Movement Row Grouping
        addControlRow(leftTable, "WASD", "DPAD", "MOVE");
        addControlRow(leftTable, "SPACE", "A", "JUMP");
        addControlRow(leftTable, "ESCAPE", "START", "SETTINGS");

        // Action/Combat Row Grouping
        addControlRow(rightTable, "ENTER", "X", "PUNCH / PICKUP");
        addControlRow(rightTable, "ENTER", "Y", "(HOLD) HVY ATTACK");
        addControlRow(rightTable, "SHIFT", "B", "THROW");

        // --- NEW ALIGNMENT ADJUSTMENTS ---
        // Left column is moved "in and right" by applying padLeft(45f) and padRight(5f)
        // PadBottom(10f) helps push everything up slightly
        add(leftTable).expand().fill().padLeft(45f).padRight(5f).padTop(5f).padBottom(10f);

        // Right column remains standard, keeping padLeft(15f) and padRight(15f) for padding
        add(rightTable).expand().fill().padLeft(15f).padRight(15f).padTop(5f).padBottom(10f);
        row();

        // Footer Area Close Button
        this.closeButton = new VisTextButton("Close");
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                close();
            }
        });

        // Reduced bottom pad slightly from 15f to 8f to avoid overlap or clipping
        add(closeButton).height(35f).width(100f).colspan(2).padBottom(8f).row();
    }

    /**
     * Resolves a control key and gamepad button, then stacks them above a centered descriptive text row
     * to save horizontal width while allowing much larger icons.
     */
    private void addControlRow(com.badlogic.gdx.scenes.scene2d.ui.Table table, String kbName, String padName, String description) {
        float iconSize = 75f;

        // Safe check for the Asset/Texture maps
        TextureRegion kbRegion = getControlIcon(kbName, false);
        TextureRegion padRegion = getControlIcon(padName, true);

        // Create a horizontal sub-table just for the icons
        var iconTable = new com.badlogic.gdx.scenes.scene2d.ui.Table();

        // Keyboard Icon
        var kbImg = new Image(kbRegion);
        kbImg.setAlign(Align.center);
        iconTable.add(kbImg).size(iconSize, iconSize);

        // "Or" Visual separator
        var orLabel = new VisLabel("/");
        orLabel.setAlignment(Align.center);
        iconTable.add(orLabel).width(24f);

        // Controller Icon
        var padImg = new Image(padRegion);
        padImg.setAlign(Align.center);
        iconTable.add(padImg).size(iconSize, iconSize);

        // Add the icon pairing to the main column
        table.add(iconTable).center().row();

        // Add the Command Description label centered directly underneath the icons
        var descLabel = new VisLabel(description);
        descLabel.setAlignment(Align.center);
        // Reduced bottom padding slightly to prevent excessive downward scaling
        table.add(descLabel).center().padTop(1f).padBottom(3f).row();
    }

    /**
     * Attempts to fetch key regions dynamically based on registration naming schemes.
     * Adapt this method's calls according to the naming pattern of the sprite atlas.
     */
    private TextureRegion getControlIcon(String label, boolean isGamepad) {
        try {
            String assetName = isGamepad ? "pad_" + label.toLowerCase() : "key_" + label.toLowerCase();
            // If asset atlas lacks a prefix, fallback to direct search lookup or general key/pad frame arrays
            return GameUIAnim.valueOf(assetName.toUpperCase()).get().getKeyFrame(0f);
        } catch (Exception e) {
            // Log issues gracefully, utilizing visual placeholders/pixels to maintain layout without hard crashes
            Log.error(TAG, "Missing " + (isGamepad ? "Controller" : "Keyboard") + " visual: " + label);
            return GameUIAnim.GEAR.get().getKeyFrame(0f); // Fallback placeholder
        }
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
        if (getStage() instanceof ControllerMenuStage menuStage) {
            returnFocusTo = menuStage.getFocusedActor();
            menuStage.setFocusedActor(closeButton); // Focus exit action immediately on opening for controller compatibility
        }
    }

    public void close() {
        isVisible = false;
        setVisible(false);
        if (getStage() instanceof ControllerMenuStage menuStage) {
            menuStage.setFocusedActor(returnFocusTo);
            returnFocusTo = null;
        }
    }
}
