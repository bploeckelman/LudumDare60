package lando.systems.ld60.game.state;

import com.zendogames.zenlib.ecs.Entity;
import com.zendogames.zenlib.ecs.components.Input;
import com.zendogames.zenlib.ecs.components.Mover;
import com.zendogames.zenlib.ecs.components.Sensor;
import com.zendogames.zenlib.ecs.movement.platformer.PlatformerMotion;
import com.zendogames.zenlib.state.State;
import com.zendogames.zenlib.state.StateMachine;
import lando.systems.ld60.game.Factory;

/**
 * Enemy behaviors that need a decision every tick. A state reads the entity's components and
 * writes its {@link Input}, the same fields the keyboard writes for a player, so the mover never
 * knows who is steering. Enemies that act on a beat use {@code Timers} in {@link Factory} instead.
 */
public enum ExampleEnemyState implements State<Entity> {
    /**
     * Walks back and forth along the platform it stands on. The direction is {@code Input.moveX}
     * and it flips when the last tick's sweep was stopped sideways by a wall, or the sensor's one
     * whisker, cast down from just past the leading edge, finds no floor (ie. a platform's edge).
     * The turn is immediate, so it never coasts past an edge while decelerating. The whisker is
     * mirrored by the sensor's {@code facing}, kept equal to direction so 'ahead' follows the turn.
     */
    WALK {
        @Override
        public State<Entity> update(StateMachine<Entity> m, Entity e, float dt) {
            var input  = Input.get(e);
            var sensor = Sensor.get(e);
            var mover  = Mover.get(e);
            var motion = mover.motion(PlatformerMotion.class);

            var ahead = sensor.whiskers[0];
            var wall  = (mover.xHit != 0);
            var edge  = motion.grounded && !ahead.hits(sensor.terrain);
            if (edge || wall) {
                // turn on the spot instead of decelerating past the edge
                input.moveX = -input.moveX;
                mover.xVel = 0;
                mover.xRem = 0;
            }
            sensor.facing = input.moveX;
            return this;
        }
    };
}
