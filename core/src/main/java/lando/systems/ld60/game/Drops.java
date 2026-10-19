package lando.systems.ld60.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.zendogames.zenlib.Log;
import com.zendogames.zenlib.ecs.Entity;
import com.zendogames.zenlib.ecs.World;
import com.zendogames.zenlib.ecs.components.Collider;
import com.zendogames.zenlib.ecs.components.Image;
import com.zendogames.zenlib.ecs.components.Name;
import com.zendogames.zenlib.ecs.components.Position;
import lando.systems.ld60.Main;
import lando.systems.ld60.assets.GameAtlas;
import lando.systems.ld60.game.combat.Strike;
import lando.systems.ld60.game.components.Health;
import lando.systems.ld60.game.components.HealthPickup;

/**
 * What a dead enemy leaves on the floor. {@link #health} is a {@code Hurtable.onDeath} callback
 * ({@code Roles.thug}); {@link #heal} is what {@code BrawlContacts} does when the player steps on one.
 */
public final class Drops {

    private static final String TAG = Drops.class.getSimpleName();

    /** Chance a dying enemy drops health, and how much it heals. */
    public static float HEALTH_CHANCE = 0.33f;
    public static int   HEALTH_AMOUNT = 2;

    private Drops() {}

    /** Rolls for a health drop at the body's feet; wire as {@code new Hurtable().onDeath(Drops::health)}. */
    public static void health(Entity dead, Strike strike) {
        if (MathUtils.random() >= HEALTH_CHANCE) return;
        var pos = Position.get(dead);
        spawnHealth(dead.world(), HEALTH_AMOUNT, pos.x, pos.y);
    }

    /** Heals {@code fighter} by the pickup and removes it; false when there was nothing to heal. */
    public static boolean heal(Entity fighter, Entity item) {
        var pickup = HealthPickup.get(item);
        var health = Health.get(fighter);
        if (pickup == null || health == null || !item.isAlive() || item.isRemoving()) return false;
        health.current = Math.min(health.max, health.current + pickup.amount());
        item.world().despawn(item);
        Log.info(TAG, "%s healed %d, %d/%d", Name.of(fighter), pickup.amount(), health.current, health.max);
        return true;
    }

    /** A health pickup at {@code (x, y)}: the same tile-wide PICKUP zone as a weapon on the floor. */
    public static Entity spawnHealth(World world, int amount, int x, int y) {
        var e = world.createEntity()
                .add(new Name("pickup:health"))
                .add(new Position(x, y))
                .add(new HealthPickup(amount))
                .add(Collider.rect(GameMask.PICKUP, -Factory.UNIT / 2f, -4, Factory.UNIT, 8));
        var image = image();
        if (image != null) e.add(image);
        return e;
    }

    /** {@code sprites/pickups/health.png} once it exists, else a green block; null headless. */
    private static Image image() {
        if (GameAtlas.SPRITES.asset().isLoaded()) {
            var region = GameAtlas.SPRITES.get().findRegion("pickups/health");
            if (region != null) {
                var image = new Image(region);
                image.origin.set(region.getRegionWidth() / 2f, 0);
                return image;
            }
        }
        if (Main.game == null || Main.game.pixel == null) return null;
        var image = new Image(Main.game.pixel, 8, 8);
        image.origin.set(4, 0);
        image.tint.set(Color.GREEN);
        return image;
    }
}
