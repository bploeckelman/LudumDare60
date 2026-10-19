package lando.systems.ld60.game.ai;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.zendogames.zenlib.ecs.Entity;
import com.zendogames.zenlib.ecs.components.Input;
import com.zendogames.zenlib.ecs.components.Mover;
import com.zendogames.zenlib.ecs.components.Position;
import com.zendogames.zenlib.ecs.components.Timers;
import lando.systems.ld60.game.characters.Kind;
import lando.systems.ld60.game.combat.Strike;
import lando.systems.ld60.game.combat.Weapons;
import lando.systems.ld60.game.components.BrawlerAnimClips;
import lando.systems.ld60.game.components.Fighter;
import lando.systems.ld60.game.components.Hurtable;
import lando.systems.ld60.game.movement.BrawlerMotion;

/**
 * The rank and file's brain, a {@code Timers} beat four times a second: pick up what it stands
 * on, walk to its spot near the nearest player (the front enemy on its side stands a reach away
 * and swings; the ones behind queue up one spacing back, see {@link Crowd#rank}), keep off the
 * others ({@link Crowd#separation}), and when in reach face the player and start a move with a
 * rest between swings. Nothing here is per entity: the beat's closure and the components are.
 * <p>
 * A kind that should fight differently gets its own {@link Mind}; a boss gets a state machine
 * ({@link Minds#states}). This one is the default for {@code Roles.THUG}.
 */
public final class ThugMind implements Mind {

    /** How far other fighters push, and how far back each rank queues. */
    public static final float SPACING = 32f;

    /** Street depth the brain is happy to attack from; within the hit boxes' depth. */
    public static final int DEPTH = 8;

    /** Close enough to its spot to stop. */
    public static final float SETTLE = 12f;

    private static final Vector2 push = new Vector2();

    private final float reach, cooldown;

    public ThugMind(float reach, float cooldown) {
        this.reach = reach;
        this.cooldown = cooldown;
    }

    @Override
    public void attach(Entity e, Kind kind, Senses senses) {
        e.add(new Input());
        // which lane to stand in, from the id, so a group fans out across the street a little
        int lane = ((e.id % 3) - 1) * DEPTH / 2;
        Timers.get(e).every("think", 0.25f, think -> think(think.entity(), senses, lane));
    }

    /**
     * A hit cuts the swing (the thug's {@code Hurtable.onHurt}): the move is dropped so the next beat
     * starts over, and if its clip is still playing the hurt clip replaces it, else the present
     * system would let the swing play out through the knockback. A hit that leaves the thug on
     * the ground already started the flinch in {@code GameStrikes}; a launch leaves the ground at
     * once, so that one is ours.
     */
    public static void cut(Entity e, Strike strike) {
        var fighter = Fighter.get(e);
        if (fighter == null || fighter.move == null) return;
        if (fighter.move.playing(e)) {
            var clips = BrawlerAnimClips.get(e);
            if (clips != null) clips.hurtOrJump().start(e, false);
        }
        fighter.move = null;
    }


    private void think(Entity e, Senses senses, int lane) {
        var input    = Input.get(e);
        var motion   = Mover.get(e).motion(BrawlerMotion.class);
        var hurtable = Hurtable.get(e);
        var fighter  = Fighter.get(e);
        if (fighter.move != null && !fighter.move.playing(e)) {
            fighter.move = null; // the move played through (or a hit cut it short): the present system animates again
        }
        if (hurtable != null && hurtable.dead) {
            input.reset(); return;
        }

        var target = senses.nearestPlayer(e);
        if (target == null) { input.moveX = 0; input.moveY = 0; return; }

        var item = Weapons.under(e);
        if (item != null) Weapons.pickUp(e, item);

        var pos  = Position.get(e);
        var tPos = Position.get(target);
        int dx = tPos.x - pos.x;
        int dy = tPos.y - pos.y;

        // my spot: a reach away on the side I am on, one spacing further back per enemy ahead of me
        int side = (dx == 0) ? 1 : -Integer.signum(dx);
        int rank = Crowd.rank(e, target, side, senses.fighters());
        float goalX = tPos.x + side * (reach + rank * SPACING);
        float goalY = tPos.y + lane;

        // steer toward it, shoved off anyone standing too close
        Crowd.separation(e, senses.fighters(), SPACING, push);
        float wantX = (goalX - pos.x) + push.x * SPACING;
        float wantY = (goalY - pos.y) + push.y * SPACING;
        // a dead band wider than one beat's travel (80 px/s * 0.25 s = 20 px) so it settles instead of pacing on its spot
        input.moveX = (Math.abs(wantX) > SETTLE) ? (int) Math.signum(wantX) : 0;
        input.moveY = (Math.abs(wantY) > DEPTH / 2f) ? (int) Math.signum(wantY) : 0;

        // front of the queue and in reach: stand, face, swing
        if (rank == 0 && Math.abs(dx) <= reach + 8 && Math.abs(dy) <= DEPTH) {
            input.moveX = 0;
            input.moveY = 0;
            if (dx != 0) motion.facing = Integer.signum(dx);
            var wield = Fighter.wieldOf(e);
            var timers = Timers.get(e);
            if (wield == null || timers.has("cooldown") || !motion.grounded || motion.launched) return;
            var move = (wield.low() != null && MathUtils.randomBoolean()) ? wield.low() : wield.attack();
            if (move != null && !move.playing(e)) {
                fighter.move = move;
                move.start(e);
                timers.after("cooldown", cooldown, rest -> {}); // the wind-up is the tell; the rest is the opening
            }
        }
    }
}
