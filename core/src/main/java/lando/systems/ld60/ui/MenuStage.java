package lando.systems.ld60.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.golfgl.gdx.controllers.ControllerMenuStage;
import lando.systems.ld60.game.input.GameControls;

/**
 * The game's {@link ControllerMenuStage}: a drop-in {@code Stage} where focus moves between registered
 * actors and the focused one is pressed from keys. Besides the library's arrows / TAB / ENTER, the
 * player's steering keys move focus and the jump and attack keys press, so a pad drives the menu
 * through the same key codes {@code Gamepad} maps it onto in-game. A {@code VisTextButton} shows focus
 * with its {@code over} drawable, the same highlight as the mouse hovering it.
 * <pre>{@code
 * uiStage.addActor(table);
 * menu.focus(play, settings, credits);   // Play is highlighted; W/S, arrows, d-pad, stick move it
 * }</pre>
 * A stage with no focusable actors handles no key at all, so the in-game screens are unaffected.
 */
public class MenuStage extends ControllerMenuStage {

    public MenuStage(Viewport viewport, Batch batch) {
        super(viewport, batch);
    }

    /**
     * Registers the actors as the focusable set, in TAB order, and focuses the first. Lays out the
     * enclosing table first: focus needs the actors' positions to pick neighbours and to show the
     * highlight, and nothing has been laid out before the first {@code act}. Call after the actors
     * are on this stage.
     */
    public void focus(Actor first, Actor... rest) {
        addFocusableActor(first);
        for (var actor : rest) addFocusableActor(actor);
        layoutAncestors(first);
        setFocusedActor(first);
    }

    @Override public boolean isGoUpKeyCode(int keyCode)    { return super.isGoUpKeyCode(keyCode)    || keyCode == GameControls.PLAYER.up(); }
    @Override public boolean isGoDownKeyCode(int keyCode)  { return super.isGoDownKeyCode(keyCode)  || keyCode == GameControls.PLAYER.down(); }
    @Override public boolean isGoLeftKeyCode(int keyCode)  { return super.isGoLeftKeyCode(keyCode)  || keyCode == GameControls.PLAYER.left(); }
    @Override public boolean isGoRightKeyCode(int keyCode) { return super.isGoRightKeyCode(keyCode) || keyCode == GameControls.PLAYER.right(); }

    /** ENTER and CENTER from the library, plus jump (pad A) and attack (pad X). */
    @Override public boolean isDefaultActionKeyCode(int keyCode) {
        return super.isDefaultActionKeyCode(keyCode)
                || keyCode == GameControls.PLAYER.action()
                || keyCode == GameControls.PLAYER_ATTACK;
    }

    /**
     * Esc, Backspace, Android back, and the special key (pad B). Not {@code super}: the library was compiled
     * against a libGDX whose {@code Keys.ESCAPE} was 131, inlined, and it is 111 since 1.10, so its own
     * Esc check never matches.
     */
    @Override public boolean isEscapeActionKeyCode(int keyCode) {
        return keyCode == Input.Keys.ESCAPE
            || keyCode == Input.Keys.BACKSPACE
            || keyCode == Input.Keys.BACK
            || keyCode == GameControls.PLAYER_SPECIAL;
    }

    /** Validates the outermost {@link Layout} above the actor; a table lays out its children itself. */
    private static void layoutAncestors(Actor actor) {
        Layout outermost = null;
        for (var group = actor.getParent(); group != null; group = group.getParent()) {
            if (group instanceof Layout layout) outermost = layout;
        }
        if (outermost != null) outermost.validate();
    }
}
