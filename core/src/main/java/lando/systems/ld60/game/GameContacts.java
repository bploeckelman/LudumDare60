package lando.systems.ld60.game;

import com.badlogic.gdx.graphics.Color;
import com.zendogames.zenlib.Log;
import com.zendogames.zenlib.collision.Contact;
import com.zendogames.zenlib.collision.ContactResponder;
import com.zendogames.zenlib.ecs.Entity;
import com.zendogames.zenlib.ecs.World;
import com.zendogames.zenlib.ecs.components.*;
import com.zendogames.zenlib.ecs.movement.platformer.PlatformerMotion;
import com.zendogames.zenlib.ecs.movement.topdown.TopDownMotion;
import lando.systems.ld60.game.components.Trigger;
import lando.systems.ld60.game.state.ExamplePlayerState;

public class GameContacts implements ContactResponder {

    private static final String TAG = GameContacts.class.getSimpleName();

    private final World world;
    private final Factory factory;

    public GameContacts(World world, Factory factory) {
        this.world   = world;
        this.factory = factory;
    }

    @Override
    public void respond(Contact contact) {
        var mover = Collider.get(contact.mover()).mask();
        if (mover != GameMask.PLAYER) return;
        if (!contact.other().isAlive()) return; // an EXIT can name a despawned entity

        // mask() is the CollisionMask interface, so compare rather than switch on the enum
        var other = Collider.get(contact.other()).mask();
        if      (other == GameMask.ENEMY)   hurt(contact);
        else if (other == GameMask.TRIGGER) trigger(contact);
        else if (other == GameMask.HAZARD)  hurt(contact);
    }

    // ------------------------------------------------------------------------
    // Triggers
    // ------------------------------------------------------------------------

    private void trigger(Contact contact) {
        var hero     = contact.mover();
        var animator = Animator.get(hero);
        var trigger  = Trigger.get(contact.other());
        switch (contact.phase()) {
            case ENTER -> {
                animator.tint.set(Color.MAGENTA);
                if (trigger != null) {
                    enter(hero, trigger.effect());
                }
            }
            case EXIT -> {
                animator.tint.set(Color.WHITE);
                if (trigger != null && trigger.effect() instanceof Trigger.Teleport && Timers.has(hero)) {
                    Timers.get(hero).cancel("teleport");
                }
            }
            case STAY -> { return; }
        }
        Log.info(TAG, "%s %s %s", Name.of(hero), contact.phase(), Name.of(contact.other()));
    }

    /** The map's trigger effects: a bounce is a straight-up launch, a teleport waits out its dwell on the hero's own timer. */
    private void enter(Entity hero, Trigger.Effect effect) {
        switch (effect) {
            case Trigger.Log    log    -> {}
            case Trigger.Bounce bounce -> {
                var mover = Mover.get(hero);
                if (mover.is(PlatformerMotion.class)) {
                    var motion = mover.motion(PlatformerMotion.class);
                    motion.launch(mover, motion.facing, /*dist*/ 0f, bounce.height(), /*lock*/ 0f);
                    motion.coyote = 0f;
                }
            }
            case Trigger.Teleport teleport -> {
                var timers = Timers.get(hero);
                if (timers == null) {
                    timers = new Timers();
                    hero.add(timers);
                }
                timers.after("teleport", teleport.dwell(), tele -> {
                    var pos = Position.get(tele.entity());
                    pos.teleport(teleport.x(), teleport.y());
                    Log.info(TAG, "%s teleported to (%d, %d)", Name.of(tele.entity()), teleport.x(), teleport.y());
                });
            }
        }
    }

    // ------------------------------------------------------------------------
    // Enemies
    // ------------------------------------------------------------------------

    /**
     * Touching an enemy: face it, then ask for a transition to the HURT state; the state does the
     * knockback away from {@code facing}. Only a NORMAL state hero is asked, so a hero flying through
     * a second enemy while hurt neither turns nor logs a refusal (remove the guard to watch HURT refuse
     * on LOG_STATE).
     */
    private void hurt(Contact contact) {
        if (contact.kind() != Contact.Kind.OVERLAP || !contact.isEnter()) {
            return;
        }

        var hero = contact.mover();
        var behavior = Behavior.get(hero);
        if (behavior == null || behavior.isNot(ExamplePlayerState.NORMAL)) {
            return;
        }

        var mover = Mover.get(hero);
        if (mover.is(TopDownMotion.class)) {
            return;
        }

        var other      = contact.other();
        var otherPos   = Position.get(other);
        var heroPos    = Position.get(hero);
        var heroMotion = Mover.get(hero).motion(PlatformerMotion.class);
        var toward     = Integer.signum(otherPos.x - heroPos.x);
        if (toward != 0) {
            heroMotion.facing = toward;
        }
        Behavior.get(hero).request(ExamplePlayerState.HURT);
        Log.info(TAG, "%s hurt by %s", Name.of(hero), Name.of(other));

        if (other.hasTag(GameTags.PROJECTILE)) {
            var renderable = (Renderable) null;
            if      (Image.has(other))    renderable = Image.get(other);
            else if (Animator.has(other)) renderable = Animator.get(other);

            int xOffset = 0;
            int yOffset = 0;
            if (renderable != null) {
                xOffset = (int) renderable.origin.x;
                yOffset = (int) renderable.origin.y;
            }
            factory.pop(otherPos.x - xOffset, otherPos.y - yOffset);
            world.despawn(other);
        }
    }
}
