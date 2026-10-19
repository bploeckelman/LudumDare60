package lando.systems.ld60.game.components;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.zendogames.zenlib.ecs.Component;
import com.zendogames.zenlib.ecs.ComponentType;
import com.zendogames.zenlib.ecs.Entity;

/**
 * A floating bob for a fighter that hovers instead of standing: drifts between random heights
 * in {@link #min}..{@link #max}, each leg taking a random {@link #periodMin}..{@link #periodMax}
 * seconds with a sine ease, so no two bobs look alike. Whoever owns the fighter's height axis
 * (a boss state) calls {@link #advance} once a tick and writes the result to {@code BrawlerMotion.z}.
 */
public class Hover implements Component {

    public static final ComponentType<Hover> TYPE = ComponentType.register(Hover.class);
    public static Hover   get(Entity e) { return TYPE.get(e); }
    public static boolean has(Entity e) { return TYPE.has(e); }

    /** Height range above the floor line, pixels. */
    public float min, max;

    /** Seconds one leg (from one height to the next) can take. */
    public float periodMin, periodMax;

    /** The current leg: from one height to the next over {@code period} seconds, {@code phase} 0..1 along it. */
    public float from, to, period, phase;

    public Hover(float min, float max, float periodMin, float periodMax) {
        this.min = min;
        this.max = max;
        this.periodMin = periodMin;
        this.periodMax = periodMax;
        this.from = MathUtils.random(min, max);
        nextLeg();
    }

    /** The height after {@code dt} more seconds. */
    public float advance(float dt) {
        phase += dt / period;
        if (phase >= 1f) {
            phase -= 1f;
            from = to;
            nextLeg();
        }
        return MathUtils.lerp(from, to, Interpolation.sine.apply(phase));
    }

    /** The height now, without advancing. */
    public float height() {
        return MathUtils.lerp(from, to, Interpolation.sine.apply(phase));
    }

    private void nextLeg() {
        to = MathUtils.random(min, max);
        period = MathUtils.random(periodMin, periodMax);
    }
}
