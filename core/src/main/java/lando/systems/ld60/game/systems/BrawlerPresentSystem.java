package lando.systems.ld60.game.systems;

import com.zendogames.zenlib.ecs.Entity;
import com.zendogames.zenlib.ecs.Family;
import com.zendogames.zenlib.ecs.IteratingSystem;
import com.zendogames.zenlib.ecs.SystemOrder;
import com.zendogames.zenlib.ecs.components.Animator;
import com.zendogames.zenlib.ecs.components.Mover;
import com.zendogames.zenlib.ecs.components.Position;
import com.zendogames.zenlib.ecs.components.SpineAnimator;
import lando.systems.ld60.game.components.Hitboxes;
import lando.systems.ld60.game.movement.BrawlerMotion;

public class BrawlerPresentSystem extends IteratingSystem {

    public static final int PRIORITY = SystemOrder.PRESENT - 1;

    public BrawlerPresentSystem() {
        super(Family.all(Position.TYPE, Mover.TYPE), PRIORITY);
    }

    @Override
    protected void process(Entity e, float dt) {
        if (!(Mover.get(e).motion instanceof BrawlerMotion motion)) return;

        if (Hitboxes.has(e)) Hitboxes.get(e).lift = motion.z;

        var order = -Position.get(e).y; // lower on the street draws in front of higher
        if (SpineAnimator.has(e)) {
            var anim = SpineAnimator.get(e);
            anim.facing   = motion.facing;
            anim.origin.y = -motion.z / anim.scale.y; // lift the skeleton by its height
            anim.order    = order;
        } else if (Animator.has(e)) {
            var anim = Animator.get(e);
            anim.facing   = motion.facing;
            anim.origin.y = -motion.z / anim.scale.y; // lift the sprite by its height
            anim.order    = order;
        }
    }
}
