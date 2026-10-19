package lando.systems.ld60.game.input;

import com.badlogic.gdx.InputProcessor;
import com.zendogames.zenlib.input.HeldKeys;

import java.util.Arrays;

/**
 * Turns a {@link HeldKeys} into the {@code keyDown} / {@code keyUp} events a keyboard would have sent
 * to an {@link InputProcessor}: {@link #update()} once a frame sends one event per key whose held state
 * changed since the last call. The ECS polls a {@link Gamepad} as held keys; a scene2d stage only
 * listens to events, and this is the adapter between the two.
 * <pre>{@code
 * var padKeys = new KeyEvents(pad, uiStage, pad.boundKeys());
 * ...
 * padKeys.update(); // in the screen's update, before the stage acts
 * }</pre>
 */
public class KeyEvents {

    private final HeldKeys source;
    private final InputProcessor target;
    private final int[] keycodes;
    private final boolean[] held;

    public KeyEvents(HeldKeys source, InputProcessor target, int... keycodes) {
        this.source = source;
        this.target = target;
        this.keycodes = keycodes;
        this.held = new boolean[keycodes.length];
    }

    /** Sends a key event for every watched key whose held state changed. */
    public void update() {
        for (int i = 0; i < keycodes.length; i++) {
            var now = source.isHeld(keycodes[i]);
            if (now == held[i]) continue;
            held[i] = now;
            if (now) target.keyDown(keycodes[i]);
            else     target.keyUp(keycodes[i]);
        }
    }

    /** Forgets what was held without sending releases, for a screen that goes away mid-press. */
    public void clear() {
        Arrays.fill(held, false);
    }
}
