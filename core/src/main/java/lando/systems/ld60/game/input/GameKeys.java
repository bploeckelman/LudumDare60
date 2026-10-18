package lando.systems.ld60.game.input;

import com.zendogames.zenlib.input.HeldKeys;

/**
 * The keyboard and {@link Gamepad} as one set of held keys: a key is held when either holds it.
 * This is the seam the pad enters through, before any edge detection, so a jump pressed on the
 * pad and released on the keyboard is still one press and one release.
 */
public record GameKeys(HeldKeys keyboard, HeldKeys pad) implements HeldKeys {

    @Override
    public boolean isHeld(int keycode) {
        return keyboard.isHeld(keycode) || pad.isHeld(keycode);
    }
}
