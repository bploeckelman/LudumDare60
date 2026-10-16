package lando.systems.ld60.game.movement;

import com.badlogic.gdx.math.MathUtils;
import com.github.tommyettinger.gdcrux.PointI2;
import com.zendogames.zenlib.collision.shapes.CollisionShape;
import com.zendogames.zenlib.ecs.components.Mover;
import com.zendogames.zenlib.ecs.movement.Motion;
import com.zendogames.zenlib.ecs.movement.MoveIntent;
import com.zendogames.zenlib.ecs.movement.MoveWorld;

/**
 * A flying enemy's path as a {@link Motion}, the way the library's genres plug into a
 * {@link Mover}: constant horizontal speed that reverses after travelling {@code range}
 * pixels on either side of {@code centerX}, and a vertical bob along {@code amplitude * sin(2*pi*t / period)}.
 * Ignores intent; holds its own clock and direction, so one instance per mover.
 */
public class FlightMotion implements Motion {

    /** Units: speed(px / sec), range(px), amp(px), period(sec). */
    public final float speed, range, amplitude, period;
    public final int centerX;

    /** -1/+1, the x direction. Sets facing when drawing. */
    public int dir;

    private float time;

    public FlightMotion(int centerX, int dir, float speed, float range, float amplitude, float period) {
        this.centerX   = centerX;
        this.dir       = (dir < 0) ? -1 : +1;
        this.speed     = speed;
        this.range     = range;
        this.amplitude = amplitude;
        this.period    = period;
    }

    @Override
    public void control(Mover mover, MoveIntent moveIntent, float dt) {
        var angle = MathUtils.PI2 / period;
        var prevY = amplitude * MathUtils.sin(angle * time);

        time += dt;
        var nextY = amplitude * MathUtils.sin(angle * time);

        mover.xVel   = dir * speed;
        mover.yVel   = (nextY - prevY) / dt;
        mover.xAccel = 0;
        mover.yAccel = 0;
    }

    @Override
    public void after(Mover mover, MoveIntent intent, PointI2 pos, CollisionShape shape, MoveWorld world) {
        if ((pos.x - centerX) * dir >= range) {
            dir = -dir;
        }
    }
}
