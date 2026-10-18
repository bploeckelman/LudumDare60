package lando.systems.ld60.game.components;

import com.zendogames.zenlib.ecs.Component;
import com.zendogames.zenlib.ecs.ComponentType;
import com.zendogames.zenlib.ecs.Entity;
import lando.systems.ld60.game.systems.GameInputSystem;

/**
 * The game's buttons beyond the library's one {@code action}: each is a key code plus the held
 * state with its pressed / released edges, tracked the way {@code Input} tracks its action.
 * {@link GameInputSystem} samples the bound buttons from the same held keys as the library's
 * {@code InputSystem}, right after it; a button left {@link #UNBOUND} is an AI's to write from a
 * {@code DECIDE} state ({@link Button#setHeld}), so the states read every fighter the same way.
 * The library's {@code Input.action} stays the motion's button (the brawler jumps on it); these
 * are for the behavior states only: {@link #attack} swings (or picks up what the fighter stands
 * over), {@link #special} throws the held weapon.
 */
public class GameInput implements Component {

    public static final ComponentType<GameInput> TYPE = ComponentType.register(GameInput.class);
    public static GameInput get(Entity e) { return TYPE.get(e); }
    public static boolean   has(Entity e) { return TYPE.has(e); }

    /** No key: the keyboard leaves this button alone. */
    public static final int UNBOUND = -1;

    public static class Button {
        public int key = UNBOUND;
        public boolean held, pressed, released;

        /** Sets the held state and derives the pressed / released edges for this tick. */
        public void setHeld(boolean h) {
            pressed  = !held &&  h;
            released =  held && !h;
            held     =  h;
        }

        void clearFrameEvents() {
            pressed  = false;
            released = false;
        }
    }

    public final Button attack  = new Button();
    public final Button special = new Button();
    // more buttons: add a field here and to the array below

    /** Every button, for the system. */
    public final Button[] buttons = { attack, special };

    public GameInput() {}

    public GameInput(int attackKey, int specialKey) {
        attack.key  = attackKey;
        special.key = specialKey;
    }

    /** Clears the one-tick edges on every button; held state is kept. {@link GameInputSystem} calls it at the top of every tick. */
    public void clearFrameEvents() {
        for (var button : buttons) button.clearFrameEvents();
    }

    /** Clears everything, bindings included. */
    public void reset() {
        for (var button : buttons) {
            button.key      = UNBOUND;
            button.held     = false;
            button.pressed  = false;
            button.released = false;
        }
    }
}
