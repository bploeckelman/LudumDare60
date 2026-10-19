package lando.systems.ld60.game.ai;

import com.zendogames.zenlib.ecs.Entity;
import com.zendogames.zenlib.ecs.components.Behavior;
import com.zendogames.zenlib.ecs.components.Input;
import com.zendogames.zenlib.state.State;
import lando.systems.ld60.game.input.GameControls;
import lando.systems.ld60.game.state.BrawlerState;

/** The minds that are one line each; a brain with rules of its own is a class ({@link ThugMind}). */
public final class Minds {

    private Minds() {}

    /** The keyboard (or pad) through {@code BrawlerState}. */
    public static Mind player() {
        return (e, kind, senses) -> e
                .add(new Input(GameControls.PLAYER))
                .add(GameControls.playerButtons())
                .add(new Behavior(BrawlerState.NORMAL));
    }

    /**
     * A state machine of its own, for a boss or any enemy whose behavior is more than "walk up and
     * swing": the states decide, {@code Input} is how they steer the motion, {@code GameInput} is
     * there unbound so states written for the player's buttons do not NPE.
     */
    public static Mind states(State<Entity> initial) {
        return (e, kind, senses) -> e
                .add(new Input())
                .add(GameControls.none())
                .add(new Behavior(initial));
    }

    /** Stands there: a target dummy. */
    public static Mind none() {
        return (e, kind, senses) -> e.add(new Input());
    }
}
