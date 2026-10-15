package lando.systems.ld60.game;

import com.zendogames.zenlib.ecs.World;

public class Factory {

    /** Tile size: the unit every tuning is authored in. */
    public static final int UNIT = 16;

    private final World world;

    public Factory(final World world) {
        this.world = world;
    }
}
