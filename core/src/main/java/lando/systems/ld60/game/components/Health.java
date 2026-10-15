package lando.systems.ld60.game.components;

import com.zendogames.zenlib.ecs.Component;
import com.zendogames.zenlib.ecs.ComponentType;
import com.zendogames.zenlib.ecs.Entity;

public class Health implements Component {

    public static final ComponentType<Health> TYPE = ComponentType.register(Health.class);
    public static Health  get(Entity e) { return TYPE.get(e); }
    public static boolean has(Entity e) { return TYPE.has(e); }

    public int max;
    public int current;

    public Health(int max) {
        this.max = max;
        this.current = max;
    }

    public void restore() { current = max; }
    public void kill()    { current = 0; }

    public boolean isAlive() { return current > 0; }
    public boolean isDead()  { return current <= 0; }
}
