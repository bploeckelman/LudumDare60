package lando.systems.ld60.game;

import com.zendogames.zenlib.ecs.Tag;

public final class GameTags {

    private GameTags() {}

    public static final Tag PLAYER     = Tag.register("Player");
    public static final Tag ENEMY      = Tag.register("Enemy");
    public static final Tag PROJECTILE = Tag.register("Projectile");
    public static final Tag BOSS       = Tag.register("Boss");
}
