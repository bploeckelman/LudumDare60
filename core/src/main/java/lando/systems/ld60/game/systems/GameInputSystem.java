package lando.systems.ld60.game.systems;

import com.zendogames.zenlib.ecs.Entity;
import com.zendogames.zenlib.ecs.Family;
import com.zendogames.zenlib.ecs.IteratingSystem;
import com.zendogames.zenlib.ecs.systems.InputSystem;
import com.zendogames.zenlib.input.HeldKeys;
import lando.systems.ld60.game.components.GameInput;

/**
 * Writes every {@link GameInput} from the keyboard once per tick, right after the library's
 * {@link InputSystem} and from the same {@link HeldKeys} (the screen's input processor, so the
 * imgui capture is already applied). The one-tick edges are cleared on every {@code GameInput},
 * then the bound buttons are samples; unbound ones are left for a {@code DECIDE} state to write.
 */
public class GameInputSystem extends IteratingSystem {

    public static final int PRIORITY = InputSystem.PRIORITY + 1;

    private final HeldKeys keys;

    public GameInputSystem(HeldKeys keys) {
        super(Family.all(GameInput.TYPE), PRIORITY);
        this.keys = keys;
    }

    @Override
    protected void process(Entity e, float dt) {
        var input = GameInput.get(e);
        input.clearFrameEvents();
        for (var button : input.buttons) {
            if (button.key == GameInput.UNBOUND) continue; // written by something else this tick
            button.setHeld(keys.isHeld(button.key), dt);
        }
    }
}
