package lando.systems.ld60.game.input;

import com.badlogic.gdx.Input;
import com.zendogames.zenlib.input.InputControls;
import lando.systems.ld60.game.components.GameInput;

/**
 * The keyboard layout for the player, in one place: the library's five steering keys for the
 * {@code Input} component and the game's buttons for the {@link GameInput}. {@link Gamepad#player()}
 * maps a pad onto these same key codes, so every system reads on set of keys whichever device
 * is in hand.
 */
public final class GameControls {

    private GameControls() {}

    public static final InputControls PLAYER = InputControls.wasd(Input.Keys.SPACE);

    public static final int PLAYER_ATTACK  = Input.Keys.ENTER;
    public static final int PLAYER_SPECIAL = Input.Keys.SHIFT_LEFT;

    public static GameInput playerButtons() {
        return new GameInput(PLAYER_ATTACK, PLAYER_SPECIAL);
    }

    public static GameInput none() {
        return new GameInput(Input.Keys.UNKNOWN, Input.Keys.UNKNOWN);
    }
}
