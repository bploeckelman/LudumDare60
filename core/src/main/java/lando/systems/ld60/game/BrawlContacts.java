package lando.systems.ld60.game;

import com.zendogames.zenlib.Log;
import com.zendogames.zenlib.collision.Contact;
import com.zendogames.zenlib.collision.ContactResponder;
import com.zendogames.zenlib.ecs.Entity;
import com.zendogames.zenlib.ecs.components.Mover;
import com.zendogames.zenlib.ecs.components.Name;
import com.zendogames.zenlib.ecs.components.Position;
import com.zendogames.zenlib.ecs.components.Timers;
import lando.systems.ld60.game.combat.Weapons;
import lando.systems.ld60.game.components.HealthPickup;
import lando.systems.ld60.game.components.Pickup;
import lando.systems.ld60.game.components.Trigger;
import lando.systems.ld60.game.movement.BrawlerMotion;

/**
 * The brawler's contact rules, for any mover that carries {@code Contacts}, player or enemy:
 * dispatched on what the <em>other</em> entity carries, so a new kind of thing to stand on is a
 * component in a factory method, not a branch here. A {@link Pickup} marked on-touch is taken on
 * ENTER; a {@link Trigger}'s effect fires on ENTER (a bounce is a hop on the height axis, a
 * teleport waits out its dwell on the mover's own timer) and a pending teleport is canceled on
 * EXIT. Touching another fighter does nothing: in a brawler only hits hurt ({@code HitSystem}).
 */
public class BrawlContacts implements ContactResponder {

    private static final String TAG = BrawlContacts.class.getSimpleName();

    @Override
    public void respond(Contact contact) {
        if (contact.kind() != Contact.Kind.OVERLAP) return;
        var other = contact.other();
        if (!other.isAlive()) return; // an EXIT can name a despawned entity

        var pickup = Pickup.get(other);
        if (pickup != null) {
            if (pickup.onTouch() && contact.isEnter()) {
                Weapons.pickUp(contact.mover(), other);
            }
            return;
        }

        var health = HealthPickup.get(other);
        if (health != null) {
            if (contact.isEnter() && contact.mover().hasTag(GameTags.PLAYER)) {
                Drops.heal(contact.mover(), other);
                return;
            }
        }

        var trigger = Trigger.get(other);
        if (trigger != null) {
            trigger(contact, trigger.effect());
        }
    }

    private void trigger(Contact contact, Trigger.Effect effect) {
        var mover = contact.mover();
        switch (contact.phase()) {
            case ENTER -> enter(mover, effect);
            case EXIT  -> { if (effect instanceof Trigger.Teleport && Timers.has(mover)) Timers.get(mover).cancel("teleport"); }
            case STAY  -> { return; }
        }
        Log.info(TAG, "%s %s %s", Name.of(mover), contact.phase(), Name.of(contact.other()));
    }

    private static void enter(Entity mover, Trigger.Effect effect) {
        switch (effect) {
            case Trigger.Log log -> {}
            case Trigger.Bounce bounce -> {
                var m = Mover.get(mover);
                if (m != null && m.motion instanceof BrawlerMotion motion) {
                    motion.zVel     = motion.tuning.impulseForHeight(bounce.height());
                    motion.grounded = false;
                    motion.jumped   = true;
                }
            }
            case Trigger.Teleport teleport -> {
                var timers = Timers.get(mover);
                if (timers == null) {
                    timers = new Timers();
                    mover.add(timers);
                }
                timers.after("teleport", teleport.dwell(), tele -> {
                    Position.get(tele.entity()).teleport(teleport.x(), teleport.y());
                    Log.info(TAG, "%s teleported to (%d, %d)", Name.of(tele.entity()), teleport.x(), teleport.y());
                });
            }
        }
    }
}
