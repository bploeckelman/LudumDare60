package lando.systems.ld60.game.movement;

import com.github.tommyettinger.gdcrux.PointI2;
import com.zendogames.zenlib.collision.Block;
import com.zendogames.zenlib.collision.shapes.CollisionShape;
import com.zendogames.zenlib.ecs.components.Mover;
import com.zendogames.zenlib.ecs.movement.Motion;
import com.zendogames.zenlib.ecs.movement.MoveIntent;
import com.zendogames.zenlib.ecs.movement.MoveWorld;
import com.zendogames.zenlib.utils.Calc;

/**
 * Side-scrolling beat-em-up movement (Double Dragon, Golden Axe): the entity's {@code Position}
 * is its spot on the floor, the shadow. {@code x} runs along the street and {@code y} up and down
 * it, each at its own top speed, 8-way with no gravity like the library's {@code TopDownMotion}.
 * Jumps and knockbacks rise on a third axis, {@link #z}, that this motion integrates itself:
 * the position and so the collider stay on the floor, so walls block an airborn mover the same
 * way and a leap never lands inside one. Whatever draws the entity lifts it by {@code z}.
 * <p>
 * Facing is left/right only, from the last horizontal steer. Steering only works on the floor;
 * the take-off velocity carries through the air unchanged and friction resumes on landing. A hit
 * calls {@link #launch} to throw the mover back along x; it ignores intent until it lands.
 * {@link #wallX}/{@link #wallY} are probed after every sweep like the library's top-down motion,
 * and steering into an adjacent wall holds that axis at rest so animation checks do not flicker.
 */
public class BrawlerMotion implements Motion {

    private static final float DIAGONAL = (float) (1.0 / Math.sqrt(2.0));

    public BrawlerTuning tuning;

    /** -1/+1, left / right only; follows the horizontal steer while on the floor. */
    public int facing = 1;

    /** Height above the floor in pixels, and its velocity in px/s. The {@code Position} never leaves the floor. */
    public float z, zVel;

    /** On the floor ({@code z == 0}). */
    public boolean grounded = true;

    /** Events for the tick just stepped: took off this tick, landed this tick. */
    public boolean jumped, landed;

    /** Thrown by {@link #launch}: intent is ignored until the landing. */
    public boolean launched;

    /** -1/+1 when a SOLID is one pixel left/right (below/above), else 0; refreshed after every sweep. */
    public int wallX, wallY;

    public BrawlerMotion(BrawlerTuning tuning) {
        this.tuning = tuning;
    }

    @Override
    public void control(Mover mover, MoveIntent intent, float dt) {
        jumped = false;
        landed = false;
        mover.xAccel = 0;
        mover.yAccel = 0;

        // The height axis is ours alone: the sweep only knows x and y
        if (!grounded) {
            var g = tuning.gravity();
            z += (zVel + 0.5f * g * dt) * dt;
            zVel += g * dt;
            if (z <= 0) {
                z = 0;
                zVel = 0;
                grounded = true;
                landed   = true;
                launched = false;
            }
        }

        // Airborn (or thrown): the take-off velocity carries, no steering, no friction
        if (!grounded) return;

        if (intent.actionPressed() && !launched) {
            zVel     = tuning.jumpImpulse();
            grounded = false;
            jumped   = true;
        }

        int ix = intent.moveX();
        int iy = intent.moveY();

        // Steering into an adjacent wall holds that axis at rest, otherwise velocity
        if (ix != 0 && ix == wallX) { ix = 0; mover.xVel = 0; mover.xRem = 0; }
        if (iy != 0 && iy == wallY) { iy = 0; mover.yVel = 0; mover.yRem = 0; }

        // Target velocity: each axis at its own top speed, a diagonal scaled so it is no faster than a straight walk
        var scale   = (ix != 0 && iy != 0) ? DIAGONAL : 1f;
        var targetX = ix * tuning.maxSpeedX() * scale;
        var targetY = iy * tuning.maxSpeedY() * scale;

        // Accelerate toward the target on a steered axis, decelerate to rest on an unsteered one
        mover.xVel = Calc.approach(mover.xVel, targetX, ((ix != 0) ? tuning.accelX() : tuning.frictionX()) * dt);
        mover.yVel = Calc.approach(mover.yVel, targetY, ((iy != 0) ? tuning.accelY() : tuning.frictionY()) * dt);

        if (ix != 0) facing = ix;
    }

    @Override
    public void after(Mover mover, MoveIntent intent, PointI2 pos, CollisionShape shape, MoveWorld world) {
        if (shape == null) { wallX = 0; wallY = 0; return; }
        wallX = (world.probe(shape, pos.x, pos.y, -1,  0) == Block.SOLID) ? -1
              : (world.probe(shape, pos.x, pos.y, +1,  0) == Block.SOLID) ? +1 : 0;
        wallY = (world.probe(shape, pos.x, pos.y,  0, -1) == Block.SOLID) ? -1
              : (world.probe(shape, pos.x, pos.y,  0, +1) == Block.SOLID) ? +1 : 0;
    }

    /**
     * Throws a mover {@code dist} units along x in direction {@code dir} (-1/+1), peaking
     * {@code height} units up, and lands it at rest after {@link BrawlerTuning#hangTime}; steering is
     * ignored until then. Facing is left alone, so a hit fighter keeps facing the attacker.
     */
    public void launch(Mover mover, int dir, float dist, float height) {
        zVel       = tuning.impulseForHeight(height);
        grounded   = false;
        launched   = true;
        mover.xVel = dir * dist * tuning.unit() / tuning.hangTime(height);
        mover.yVel = 0;
        mover.xRem = 0;
        mover.yRem = 0;
    }

    /** Straight up from where it stands, for a hop that keeps the current walk. */
    public void jump(Mover mover) {
        zVel     = tuning.jumpImpulse();
        grounded = false;
        jumped   = true;
    }
}
