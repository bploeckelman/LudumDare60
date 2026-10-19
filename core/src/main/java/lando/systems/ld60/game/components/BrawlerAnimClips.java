package lando.systems.ld60.game.components;

import com.zendogames.zenlib.ecs.Component;
import com.zendogames.zenlib.ecs.ComponentType;
import com.zendogames.zenlib.ecs.Entity;
import lando.systems.ld60.game.anim.AnimClip;
import lando.systems.ld60.game.anim.Move;
import lando.systems.ld60.game.systems.BrawlerPresentSystem;

/**
 * Which clips a brawler plays for what its {@code BrawlerMotion} is doing, so the
 * {@link BrawlerPresentSystem} animates every fighter the same way and a state only has to play
 * what the motion cannot know (an attack). Idle, walk and run are the locomotion; {@link #jump}
 * covers the air, {@link #hurt} a knockback (falls back to {@code jump}), {@link #dead} plays once
 * when a {@code Hurtable} dies and holds (falls back to {@code hurt}). {@link #stop} is optional:
 * a run-to-idle transition played, then idle queued, when a run ends or the fighter lands.
 * {@link #attack} is the basic attack for a fighter with no weapon component.
 * <p>
 * The system only touches a clip it owns (one of these, or nothing / a finished clip), so a
 * state's {@code Move} plays through untouched and locomotion resumes when it is done.
 */
public class BrawlerAnimClips implements Component {

    public static final ComponentType<BrawlerAnimClips> TYPE = ComponentType.register(BrawlerAnimClips.class);
    public static BrawlerAnimClips get(Entity e) { return TYPE.get(e); }
    public static boolean          has(Entity e) { return TYPE.has(e); }

    public final AnimClip idle, walk, run, jump;

    public AnimClip hurt, dead, stop, throwing;
    public Move attack;

    /** Speed in px/s at and above which the run clip plays instead of walk. */
    public float runSpeed;

    public BrawlerAnimClips(AnimClip idle, AnimClip walk, AnimClip run, AnimClip jump, float runSpeed) {
        this.idle = idle;
        this.walk = walk;
        this.run  = run;
        this.jump = jump;
        this.runSpeed = runSpeed;
    }

    public BrawlerAnimClips hurt(AnimClip clip)     { this.hurt     = clip; return this; }
    public BrawlerAnimClips dead(AnimClip clip)     { this.dead     = clip; return this; }
    public BrawlerAnimClips stop(AnimClip clip)     { this.stop     = clip; return this; }
    public BrawlerAnimClips throwing(AnimClip clip) { this.throwing = clip; return this; }
    public BrawlerAnimClips attack(Move move)       { this.attack   = move; return this; }

    public AnimClip hurtOrJump() { return (hurt != null) ? hurt : jump; }
    public AnimClip deadOrHurt() { return (dead != null) ? dead : hurtOrJump(); }

    /** True when {@code name} is one of these clips: the system may replace it. */
    public boolean owns(String name) {
        if (name == null) return true;
        return name.equals(idle.name()) || name.equals(walk.name()) || name.equals(run.name()) || name.equals(jump.name())
                || (hurt != null && name.equals(hurt.name()))
                || (dead != null && name.equals(dead.name()))
                || (stop != null && name.equals(stop.name()));
    }
}
