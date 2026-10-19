package lando.systems.ld60.game.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerMapping;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.utils.IntMap;
import com.zendogames.zenlib.input.HeldKeys;
import com.zendogames.zenlib.input.InputControls;

import java.util.function.Supplier;

import static com.badlogic.gdx.Application.ApplicationType;

/**
 * A controller read as if it were keys: each binding says which pad input stands in for which
 * key code, and {@link #isHeld} answers for that key code from the pad's current state. Merged
 * with the keyboard by {@link GameKeys}, so the library's {@code InputSystem} and the game's
 * {@code GameInputSystem} read a pad without knowing one exists, and every edge (pressed,
 * released, double tap) is derived once from the merged held state.
 * <pre>{@code
 * var pad = Gamepad.player1();                  // the first connected controller, the P1 layout
 * var keys = new GameKeys(inputProcessor, pad); // keyboard or pad, either holds a key
 * world.addSystem(new InputSystem(keys));
 * }</pre>
 * Sticks count as held past {@link #deadzone}. Buttons and axes go through the controller's
 * {@link ControllerMapping}, so an Xbox and a PlayStation pad bind the same. Desktop only: on
 * web and anywhere else {@link #first()} yields a pad that holds nothing, and the
 * {@code Controllers} class is never initialized there.
 */
public class Gamepad implements HeldKeys {

    /** What a key code can stand for on the pad. Directions are the d-pad or the left stick past the deadzone. */
    public enum Pad {
        LEFT, RIGHT, UP, DOWN,
        A, B, X, Y,
        L1, R1, L2, R2,
        START, BACK, LEFT_STICK, RIGHT_STICK
    }

    /** Holds nothing: the pad for platforms without controllers, or no controller plugged in. */
    public static final Gamepad NONE = new Gamepad(() -> null);

    private final Supplier<Controller> source;
    private final IntMap<Pad> bindings = new IntMap<>(16);

    /** Stick deflection at and past which a direction counts as held. */
    public float deadzone = 0.5f;

    public Gamepad(Supplier<Controller> source) {
        this.source = source;
    }

    /**
     * Follows whatever controller is first in the list each query, so plugging one in mid-game just works;
     * Desktop (Jamepad) and web (gdx-controllers-web in the teavm module); anywhere else the Controllers
     * lookup would throw, so those get {@link #NONE}.
     */
    public static Gamepad first() {
        if (Gdx.app == null) return NONE;
        var type = Gdx.app.getType();
        if (type != ApplicationType.Desktop && type != ApplicationType.WebGL) return NONE;
        return new Gamepad(() -> {
            var pads = Controllers.getControllers();
            return pads.isEmpty() ? null : pads.first();
        });
    }

    /** {@link #first()} bound to the {@link GameControls} P1 layout: stick / d-pad steer, A jumps, X attacks, B throws. */
    public static Gamepad player() {
        return first().bind(GameControls.PLAYER)
                .bind(GameControls.PLAYER_ATTACK,  Pad.X)
                .bind(GameControls.PLAYER_SPECIAL, Pad.B);
    }

    public Gamepad bind(int keycode, Pad pad) {
        bindings.put(keycode, pad);
        return this;
    }

    /** The five steering keys: directions on the d-pad or left stick, the action on A. */
    public Gamepad bind(InputControls controls) {
        return bind(controls.left(), Pad.LEFT).bind(controls.right(), Pad.RIGHT)
                .bind(controls.up(),   Pad.UP)  .bind(controls.down(),  Pad.DOWN)
                .bind(controls.action(), Pad.A);
    }

    @Override
    public boolean isHeld(int keycode) {
        var pad = bindings.get(keycode);
        if (pad == null) return false;
        var controller = source.get();
        if (controller == null) return false;
        return held(controller, pad);
    }

    private boolean held(Controller c, Pad pad) {
        var map = c.getMapping();
        return switch (pad) {
            case LEFT  -> button(c, map.buttonDpadLeft)  || axis(c, map.axisLeftX) <= -deadzone;
            case RIGHT -> button(c, map.buttonDpadRight) || axis(c, map.axisLeftX) >=  deadzone;
            case UP    -> button(c, map.buttonDpadUp)    || axis(c, map.axisLeftY) <= -deadzone; // libGDX sticks: up is negative
            case DOWN  -> button(c, map.buttonDpadDown)  || axis(c, map.axisLeftY) >=  deadzone;
            case A     -> button(c, map.buttonA);
            case B     -> button(c, map.buttonB);
            case X     -> button(c, map.buttonX);
            case Y     -> button(c, map.buttonY);
            case L1    -> button(c, map.buttonL1);
            case R1    -> button(c, map.buttonR1);
            case L2    -> button(c, map.buttonL2);
            case R2    -> button(c, map.buttonR2);
            case START -> button(c, map.buttonStart);
            case BACK  -> button(c, map.buttonBack);
            case LEFT_STICK  -> button(c, map.buttonLeftStick);
            case RIGHT_STICK -> button(c, map.buttonRightStick);
        };
    }

    private static boolean button(Controller c, int code) {
        return code != ControllerMapping.UNDEFINED && c.getButton(code);
    }

    private static float axis(Controller c, int code) {
        return (code == ControllerMapping.UNDEFINED) ? 0f : c.getAxis(code);
    }
}
